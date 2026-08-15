/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.saml;

import com.onelogin.saml2.authn.AuthnRequest;
import com.onelogin.saml2.model.KeyStoreSettings;
import com.onelogin.saml2.settings.IdPMetadataParser;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.onelogin.saml2.util.Util;
import io.bastillion.common.util.AppConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Properties;

/**
 * Builds a Saml2Settings from AppConfig properties. Deliberately not cached in a static
 * final field: when samlIdpMetadataUrl is set, building settings means a live HTTP fetch of
 * the IdP's metadata, which can transiently fail at cold boot (container network not up
 * yet, IdP hiccup) in a way a config-string read never can - a failed static initializer
 * would poison this class for the JVM's lifetime, whereas a plain rebuild-on-every-call
 * lets a transient fetch error self-heal on the next login attempt.
 */
public class SamlSettingsUtil {

    public static final String ACS_PATH = "/saml/acs";
    public static final String METADATA_PATH = "/saml/metadata";

    private static final String SP_KEYSTORE_ALIAS = "bastillion-saml-sp";

    private SamlSettingsUtil() {
    }

    public static Saml2Settings getSettings() throws Exception {
        String baseUrl = AppConfig.getProperty("samlBaseUrl");

        Properties props = new Properties();
        props.setProperty(SettingsBuilder.SP_ENTITYID_PROPERTY_KEY, AppConfig.getProperty("samlSpEntityId", baseUrl));
        props.setProperty(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_URL_PROPERTY_KEY, baseUrl + ACS_PATH);
        props.setProperty(SettingsBuilder.STRICT_PROPERTY_KEY, "true");
        props.setProperty(SettingsBuilder.SECURITY_WANT_ASSERTIONS_SIGNED, "true");
        //signing AuthnRequests is safe to always turn on, unlike requiring encrypted
        //assertions below: an IdP that doesn't check the SP's signature just ignores it, so
        //there's no existing-deployment behavior to preserve here the way there is for
        //encryption - see the property comment on samlWantEncryptedAssertions.
        props.setProperty(SettingsBuilder.SECURITY_AUTHREQUEST_SIGNED, "true");
        boolean wantEncryptedAssertions = "true".equalsIgnoreCase(AppConfig.getProperty("samlWantEncryptedAssertions", "false"));
        props.setProperty(SettingsBuilder.SECURITY_WANT_ASSERTIONS_ENCRYPTED, String.valueOf(wantEncryptedAssertions));

        //loaded once, applied to whichever SettingsBuilder below actually gets used
        KeyStoreSettings spKeyStore = spKeyStoreSettings();

        String metadataUrl = AppConfig.getProperty("samlIdpMetadataUrl");
        if (StringUtils.isNotEmpty(metadataUrl)) {
            //fromProperties() reads props at call time, not lazily - so it must run after
            //every property this branch cares about is already set on props, same as the
            //manual-trio branch below. Each branch gets its own fresh SettingsBuilder for
            //exactly that reason - reusing one across both was the bug that shipped first.
            Saml2Settings settings = new SettingsBuilder().fromProperties(props).fromValues(null, spKeyStore).build();
            Map<String, Object> idpMetadata = IdPMetadataParser.parseRemoteXML(new URL(metadataUrl));
            return IdPMetadataParser.injectIntoSettings(settings, idpMetadata);
        }

        props.setProperty(SettingsBuilder.IDP_ENTITYID_PROPERTY_KEY, StringUtils.defaultString(AppConfig.getProperty("samlIdpEntityId")));
        props.setProperty(SettingsBuilder.IDP_SINGLE_SIGN_ON_SERVICE_URL_PROPERTY_KEY, StringUtils.defaultString(AppConfig.getProperty("samlIdpSsoUrl")));
        props.setProperty(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, StringUtils.defaultString(AppConfig.getProperty("samlIdpCert")));
        return new SettingsBuilder().fromProperties(props).fromValues(null, spKeyStore).build();
    }

    /**
     * Builds the URL to redirect the browser to for an SP-initiated login: the IdP's SSO
     * URL with the (compressed, base64, URL-encoded) AuthnRequest attached, plus - since
     * getSettings() always turns on request signing once an SP key pair exists - a
     * SigAlg/Signature pair computed the same way the toolkit's own Auth.login() does for
     * the HTTP-Redirect binding. That computation isn't exposed on AuthnRequest itself
     * (redirect-binding signing works over the encoded query string, not by embedding a
     * &lt;ds:Signature&gt; in the XML the way POST-binding responses do), so it's
     * replicated here directly from java-saml-core primitives - Util.sign/urlEncoder/
     * base64encoder and Saml2Settings.getSPkey()/getSignatureAlgorithm() are all the same
     * core, framework-agnostic API surface used everywhere else in this package.
     */
    public static String buildLoginRedirectUrl(Saml2Settings settings) throws Exception {
        AuthnRequest authnRequest = new AuthnRequest(settings);
        String encodedSamlRequest = Util.urlEncoder(authnRequest.getEncodedAuthnRequest());

        StringBuilder query = new StringBuilder("SAMLRequest=").append(encodedSamlRequest);
        if (settings.getAuthnRequestsSigned()) {
            String sigAlg = settings.getSignatureAlgorithm();
            String encodedSigAlg = Util.urlEncoder(sigAlg);
            String signedMessage = "SAMLRequest=" + encodedSamlRequest + "&SigAlg=" + encodedSigAlg;
            String signature = Util.base64encoder(Util.sign(signedMessage, settings.getSPkey(), sigAlg));
            query.append("&SigAlg=").append(encodedSigAlg)
                    .append("&Signature=").append(Util.urlEncoder(signature));
        }
        return settings.getIdpSingleSignOnServiceUrl() + "?" + query;
    }

    /**
     * Bastillion's own SP signing/encryption key pair, auto-generated (self-signed, like the
     * TLS certificate Main.java generates the same way) on first use and reused after that -
     * signing every AuthnRequest from then on needs no separate opt-in. Publish the resulting
     * certificate to your IdP via the /saml/metadata endpoint if it should verify signed
     * requests or encrypt assertions for Bastillion; point samlSpKeystorePath/
     * samlSpKeystorePassword at a real one to use a CA-issued key pair instead.
     */
    private static KeyStoreSettings spKeyStoreSettings() throws Exception {
        File keystoreFile = new File(AppConfig.getProperty("samlSpKeystorePath",
                AppConfig.CONFIG_DIR + "keystore" + File.separator + "bastillion-saml-sp.p12"));
        String password = AppConfig.getProperty("samlSpKeystorePassword");

        if (StringUtils.isEmpty(password)) {
            if (keystoreFile.exists() && StringUtils.isEmpty(AppConfig.getProperty("samlSpKeystoreGeneratedPassword"))) {
                throw new IllegalStateException("samlSpKeystorePath is set to an existing file (" + keystoreFile
                        + ") but samlSpKeystorePassword is not set");
            }
            password = generatedSpKeystorePassword();
        }

        if (!keystoreFile.exists()) {
            generateSelfSignedSpKeystore(keystoreFile, password);
        }

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(keystoreFile)) {
            keyStore.load(in, password.toCharArray());
        }
        return new KeyStoreSettings(keyStore, SP_KEYSTORE_ALIAS, password);
    }

    /**
     * Mirrors Main.keystorePassword() exactly, under its own property name so the SAML SP
     * keystore and the TLS keystore - two unrelated certificates - don't share a password.
     */
    private static String generatedSpKeystorePassword() throws Exception {
        if (StringUtils.isNotEmpty(AppConfig.getProperty("samlSpKeystoreGeneratedPassword"))) {
            return AppConfig.isPropertyEncrypted("samlSpKeystoreGeneratedPassword")
                    ? AppConfig.decryptProperty("samlSpKeystoreGeneratedPassword")
                    : AppConfig.getProperty("samlSpKeystoreGeneratedPassword");
        }
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder generatedBuilder = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            generatedBuilder.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        }
        String generated = generatedBuilder.toString();
        AppConfig.encryptProperty("samlSpKeystoreGeneratedPassword", generated);
        return generated;
    }

    private static void generateSelfSignedSpKeystore(File keystoreFile, String password) throws Exception {
        File parent = keystoreFile.getParentFile();
        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }
        String keytool = System.getProperty("java.home") + File.separator + "bin" + File.separator + "keytool";
        Process process = new ProcessBuilder(
                keytool, "-genkeypair",
                "-alias", SP_KEYSTORE_ALIAS,
                "-keyalg", "RSA", "-keysize", "2048", "-sigalg", "SHA256withRSA",
                "-validity", "3650",
                "-keystore", keystoreFile.getAbsolutePath(),
                "-storetype", "PKCS12",
                "-storepass", password,
                "-dname", "CN=Bastillion SAML SP")
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes());
        if (process.waitFor() != 0) {
            throw new IllegalStateException("keytool failed to generate the SAML SP keystore: " + output);
        }
    }
}
