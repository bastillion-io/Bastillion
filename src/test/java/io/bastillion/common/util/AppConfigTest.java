package io.bastillion.common.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @Test
    void toScreamingSnakeCaseConvertsCamelCase() {
        assertEquals("LICENSE_KEY", AppConfig.toScreamingSnakeCase("licenseKey"));
        assertEquals("DB_USER", AppConfig.toScreamingSnakeCase("dbUser"));
        assertEquals("SSH_KEY_TYPE", AppConfig.toScreamingSnakeCase("sshKeyType"));
        assertEquals("A", AppConfig.toScreamingSnakeCase("a"));
        // No case transitions -> no underscores inserted, just uppercased.
        assertEquals("ALLLOWERCASE", AppConfig.toScreamingSnakeCase("alllowercase"));
    }

    @Test
    void getPropertyFallsBackToBundledDefaultWhenUnset() {
        // deleteAuditLogAfter=90 in the bundled BastillionConfig.properties, and there's no
        // CONFIG_DIR-persisted override or env var for it in a fresh test run.
        assertEquals("90", AppConfig.getProperty("deleteAuditLogAfter"));
    }

    @Test
    void getPropertyReturnsNullForCompletelyUnknownName() {
        assertNull(AppConfig.getProperty("thisPropertyDoesNotExistAnywhere"));
    }

    @Test
    void getPropertyWithDefaultValueUsesDefaultOnlyWhenUnset() {
        assertEquals("fallback", AppConfig.getProperty("thisPropertyDoesNotExistAnywhere", "fallback"));
        // A property that does resolve (via bundled defaults) should win over the fallback.
        assertEquals("90", AppConfig.getProperty("deleteAuditLogAfter", "fallback"));
    }

    @Test
    void getPropertyWithReplacementMapSubstitutesPlaceholders() {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("randomPassphrase", "generated-value");
        // defaultSSHPassphrase=${randomPassphrase} in the bundled defaults.
        assertEquals("generated-value", AppConfig.getProperty("defaultSSHPassphrase", replacements));
    }

    @Test
    void encryptDecryptPropertyRoundTrips() throws Exception {
        AppConfig.encryptProperty("appConfigTestRoundTrip", "s3cr3t-value");
        assertTrue(AppConfig.isPropertyEncrypted("appConfigTestRoundTrip"));
        assertEquals("s3cr3t-value", AppConfig.decryptProperty("appConfigTestRoundTrip"));
    }

    @Test
    void isPropertyEncryptedFalseForPlainValues() {
        assertFalse(AppConfig.isPropertyEncrypted("dbDriver"));
    }
}
