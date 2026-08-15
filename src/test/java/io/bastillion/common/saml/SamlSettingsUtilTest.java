/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.saml;

import com.onelogin.saml2.settings.Saml2Settings;
import io.bastillion.common.util.AppConfig;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the manual-trio (samlIdpEntityId/samlIdpSsoUrl/samlIdpCert) config path's
 * property-to-Saml2Settings-key routing - the metadata-URL path (IdPMetadataParser.
 * parseRemoteXML) is a live HTTP fetch and belongs in manual/integration verification
 * against a real IdP instead.
 * <p>
 * Deliberately does NOT set samlIdpSsoUrl or samlIdpMetadataUrl via AppConfig.updateProperty
 * anywhere in this class: both gate SamlAuthUtil.samlAuthEnabled, a `static final boolean`
 * computed exactly once per JVM the first time SamlAuthUtil's class initializer runs.
 * Surefire runs this whole module's tests in one shared JVM, so persisting a non-empty value
 * for either property here - even temporarily - risks SamlAuthUtilTest's "SAML is disabled"
 * assumption becoming false depending on class-load order, which is exactly the kind of
 * ordering-dependent flakiness worth avoiding rather than working around.
 * <p>
 * Also deliberately does not assert the "samlSpEntityId unset -> falls back to samlBaseUrl"
 * branch as its own scenario: AppConfig.updateProperty persists to a file under CONFIG_DIR
 * with no complementary "clear property" API, and that file isn't wiped between bare `mvn
 * test` re-runs (only `mvn clean`) - so a prior run (or JUnit's non-source-order method
 * execution within this very class) can leave samlSpEntityId set from a different test,
 * making "is it unset" an unsafe thing to depend on. spEntityIdOverrideWinsOverSamlBaseUrl
 * below covers the same property-routing code path without relying on absence of state.
 */
class SamlSettingsUtilTest {

    @Test
    void spEntityIdOverrideWinsOverSamlBaseUrl() throws Exception {
        AppConfig.updateProperty("samlBaseUrl", "https://bastillion.test");
        AppConfig.updateProperty("samlSpEntityId", "https://bastillion.test/custom-entity-id");

        Saml2Settings settings = SamlSettingsUtil.getSettings();

        assertEquals("https://bastillion.test/custom-entity-id", settings.getSpEntityId());
        //ACS URL is always the fixed path off samlBaseUrl regardless of the entity ID override
        assertEquals(new URL("https://bastillion.test/saml/acs"), settings.getSpAssertionConsumerServiceUrl());
    }

    @Test
    void manualIdpEntityIdIsRoutedIntoSettings() throws Exception {
        AppConfig.updateProperty("samlBaseUrl", "https://bastillion.test");
        AppConfig.updateProperty("samlIdpEntityId", "https://idp.test/entity");

        Saml2Settings settings = SamlSettingsUtil.getSettings();

        assertEquals("https://idp.test/entity", settings.getIdpEntityId());
    }

    @Test
    void requestSigningIsAlwaysOffAndAssertionSigningIsAlwaysRequired() throws Exception {
        // Hardcoded, not exposed as config - a toggle that could accidentally disable
        // signature validation is a foot-gun with no legitimate use case here.
        AppConfig.updateProperty("samlBaseUrl", "https://bastillion.test");

        Saml2Settings settings = SamlSettingsUtil.getSettings();

        assertTrue(settings.isStrict());
        assertTrue(settings.getWantAssertionsSigned());
        assertFalse(settings.getAuthnRequestsSigned());
    }
}
