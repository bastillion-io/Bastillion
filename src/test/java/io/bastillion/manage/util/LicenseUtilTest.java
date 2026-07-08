/**
 * Copyright (C) 2013 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.LocalDate;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers the malformed/tampered-input paths of {@link LicenseUtil#verifyAndParse}, and the
 * pure field-parsing logic of {@link LicenseUtil#parsePayload} directly. The "successfully
 * verified, correctly signed license" path can't be exercised here: it requires the real
 * private signing key, which deliberately never ships with the source (see
 * tools/licensing/LicenseGenerator.java) - a garbage/wrong-key signature is expected to (and
 * for security must) fail verification against the real product public key baked into
 * LicenseUtil, so that path is covered below as a rejection case instead.
 */
class LicenseUtilTest {

    private static String payload(String licenseId, String licensee, String maxSystems, String issuedDate, String expiry) {
        return String.join("|", licenseId, licensee, maxSystems, issuedDate, expiry);
    }

    private static String signWithUnrelatedKey(String payload) throws Exception {
        // Deliberately NOT Bastillion's real signing key - simulates a tampered/forged blob.
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = kpg.generateKeyPair();
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(keyPair.getPrivate());
        signer.update(payloadBytes);
        byte[] sig = signer.sign();

        return Base64.getEncoder().encodeToString(payloadBytes) + "." + Base64.getEncoder().encodeToString(sig);
    }

    @Test
    void verifyAndParseRejectsBlobMissingSignatureSeparator() throws Exception {
        assertNull(LicenseUtil.verifyAndParse("nodothere"));
    }

    @Test
    void verifyAndParseRejectsInvalidBase64Payload() throws Exception {
        assertNull(LicenseUtil.verifyAndParse("not-valid-base64!!!.alsoNotValid!!!"));
    }

    @Test
    void verifyAndParseRejectsBlobNotSignedByTheRealKey() throws Exception {
        String blob = signWithUnrelatedKey(payload("id-1", "Acme Corp", "10", "2026-01-01", "none"));
        assertNull(LicenseUtil.verifyAndParse(blob));
    }

    @Test
    void parsePayloadHandlesPerpetualLicense() {
        byte[] bytes = payload("id-1", "Acme Corp", "10", "2026-01-01", "none").getBytes(StandardCharsets.UTF_8);
        LicenseUtil.ParsedLicense parsed = LicenseUtil.parsePayload(bytes);

        assertEquals("id-1", parsed.licenseId);
        assertEquals("Acme Corp", parsed.licensee);
        assertEquals(10, parsed.maxSystems);
        assertNull(parsed.expiryDate);
    }

    @Test
    void parsePayloadHandlesLicenseWithExpiry() {
        byte[] bytes = payload("id-2", "Acme Corp", "25", "2026-01-01", "2027-01-01").getBytes(StandardCharsets.UTF_8);
        LicenseUtil.ParsedLicense parsed = LicenseUtil.parsePayload(bytes);

        assertEquals(25, parsed.maxSystems);
        assertEquals(LocalDate.of(2027, 1, 1), parsed.expiryDate);
    }

    @Test
    void parsePayloadRejectsWrongFieldCount() {
        byte[] bytes = "id-1|Acme Corp|10".getBytes(StandardCharsets.UTF_8);
        assertNull(LicenseUtil.parsePayload(bytes));
    }

    @Test
    void parsePayloadRejectsNonNumericMaxSystems() {
        byte[] bytes = payload("id-1", "Acme Corp", "unlimited", "2026-01-01", "none").getBytes(StandardCharsets.UTF_8);
        assertNull(LicenseUtil.parsePayload(bytes));
    }

    @Test
    void parsePayloadRejectsUnparsableExpiryDate() {
        byte[] bytes = payload("id-1", "Acme Corp", "10", "2026-01-01", "not-a-date").getBytes(StandardCharsets.UTF_8);
        assertNull(LicenseUtil.parsePayload(bytes));
    }
}
