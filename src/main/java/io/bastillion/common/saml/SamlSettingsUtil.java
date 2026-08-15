/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.saml;

import com.onelogin.saml2.settings.IdPMetadataParser;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import io.bastillion.common.util.AppConfig;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
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

    private SamlSettingsUtil() {
    }

    public static Saml2Settings getSettings() throws Exception {
        String baseUrl = AppConfig.getProperty("samlBaseUrl");

        Properties props = new Properties();
        props.setProperty(SettingsBuilder.SP_ENTITYID_PROPERTY_KEY, AppConfig.getProperty("samlSpEntityId", baseUrl));
        props.setProperty(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_URL_PROPERTY_KEY, baseUrl + ACS_PATH);
        props.setProperty(SettingsBuilder.STRICT_PROPERTY_KEY, "true");
        props.setProperty(SettingsBuilder.SECURITY_WANT_ASSERTIONS_SIGNED, "true");
        //no SP_PRIVATEKEY/SP_X509CERT set, so the AuthnRequest stays unsigned - the common,
        //working default for enterprise SAML SPs: the IdP validates nothing about the SP's
        //request, the SP validates the IdP's signed response, which is the
        //security-critical direction. Signing the request is a documented v2 add-on for IdPs
        //that require it.

        String metadataUrl = AppConfig.getProperty("samlIdpMetadataUrl");
        if (StringUtils.isNotEmpty(metadataUrl)) {
            Saml2Settings settings = new SettingsBuilder().fromProperties(props).build();
            Map<String, Object> idpMetadata = IdPMetadataParser.parseRemoteXML(new URL(metadataUrl));
            return IdPMetadataParser.injectIntoSettings(settings, idpMetadata);
        }

        props.setProperty(SettingsBuilder.IDP_ENTITYID_PROPERTY_KEY, StringUtils.defaultString(AppConfig.getProperty("samlIdpEntityId")));
        props.setProperty(SettingsBuilder.IDP_SINGLE_SIGN_ON_SERVICE_URL_PROPERTY_KEY, StringUtils.defaultString(AppConfig.getProperty("samlIdpSsoUrl")));
        props.setProperty(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, StringUtils.defaultString(AppConfig.getProperty("samlIdpCert")));
        return new SettingsBuilder().fromProperties(props).build();
    }
}
