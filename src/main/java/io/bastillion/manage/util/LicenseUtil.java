/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import io.bastillion.common.util.AppConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * Verifies the offline-signed Bastillion license key (see licensing/LicenseGenerator.java,
 * a vendor-side tool that is not part of the deployed application). License validity gates
 * how many systems can be registered under Manage &gt; Systems; with no valid license the
 * app still works, just capped at {@link #FREE_TIER_MAX_SYSTEMS}.
 * <p>
 * License blob format: base64(licenseId|licensee|maxSystems|issuedDate|expiryDate) + "." +
 * base64(Ed25519 signature over the decoded payload bytes). expiryDate is the literal string
 * "none" for a perpetual license.
 */
public class LicenseUtil {

    private static final Logger log = LoggerFactory.getLogger(LicenseUtil.class);

    // Public half of the offline signing keypair. The private key never ships with the product.
    private static final String PUBLIC_KEY_B64 = "MCowBQYDK2VwAyEAe+6TgNZGJ3rKiCiVqpK+soD6m91H6mh9ktHauNWwl6Y=";

    public static final int FREE_TIER_MAX_SYSTEMS = 5;

    private static final boolean VALID;
    private static final String LICENSEE;
    private static final int MAX_SYSTEMS;
    private static final LocalDate EXPIRY_DATE; // null == perpetual

    static {
        boolean valid = false;
        String licensee = null;
        int maxSystems = FREE_TIER_MAX_SYSTEMS;
        LocalDate expiryDate = null;

        try {
            String blob = AppConfig.getProperty("licenseKey");
            if (StringUtils.isNotEmpty(blob)) {
                ParsedLicense parsed = verifyAndParse(blob.trim());
                if (parsed != null) {
                    valid = parsed.expiryDate == null || !parsed.expiryDate.isBefore(LocalDate.now());
                    licensee = parsed.licensee;
                    expiryDate = parsed.expiryDate;
                    if (valid) {
                        maxSystems = parsed.maxSystems;
                    } else {
                        log.warn("Bastillion license for '{}' expired on {}", parsed.licensee, parsed.expiryDate);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to verify Bastillion license: {}", ex.toString());
        }

        VALID = valid;
        LICENSEE = licensee;
        MAX_SYSTEMS = maxSystems;
        EXPIRY_DATE = expiryDate;
    }

    private LicenseUtil() {
    }

    /**
     * True if a currently-valid (correctly signed, not expired) license is installed.
     */
    public static boolean isLicensed() {
        return VALID;
    }

    /**
     * Maximum number of registered systems allowed: the license's limit if one is valid,
     * otherwise the unlicensed free-tier cap.
     */
    public static int getMaxSystems() {
        return MAX_SYSTEMS;
    }

    /**
     * Licensee name from the currently installed license, or null if unlicensed/invalid.
     */
    public static String getLicensee() {
        return LICENSEE;
    }

    /**
     * License expiry date, or null if unlicensed or perpetual.
     */
    public static LocalDate getExpiryDate() {
        return EXPIRY_DATE;
    }

    /**
     * Days remaining until the license expires (negative if already expired), or null if
     * unlicensed or perpetual.
     */
    public static Long getDaysUntilExpiry() {
        return EXPIRY_DATE == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), EXPIRY_DATE);
    }

    /**
     * True once a validly-signed license's expiry date has passed. (An unlicensed install or
     * a tampered/malformed key both report false here - they're "unlicensed", not "expired".)
     */
    public static boolean isExpired() {
        return EXPIRY_DATE != null && !VALID;
    }

    /**
     * True if the license has a set expiry date within the next {@code days} days
     * (inclusive) and has not already expired.
     */
    public static boolean isExpiringWithinDays(int days) {
        Long remaining = getDaysUntilExpiry();
        return remaining != null && remaining >= 0 && remaining <= days;
    }

    /**
     * Verifies the Ed25519 signature on a license blob and parses its payload.
     * Returns null (rather than throwing) for a structurally malformed blob so a corrupt
     * config value degrades to the free tier instead of preventing startup.
     */
    private static ParsedLicense verifyAndParse(String blob) throws Exception {
        int dot = blob.indexOf('.');
        if (dot < 0) {
            log.warn("Bastillion license key is malformed (missing signature separator)");
            return null;
        }

        byte[] payloadBytes;
        byte[] sigBytes;
        try {
            payloadBytes = Base64.getDecoder().decode(blob.substring(0, dot));
            sigBytes = Base64.getDecoder().decode(blob.substring(dot + 1));
        } catch (IllegalArgumentException ex) {
            log.warn("Bastillion license key is not valid base64");
            return null;
        }

        PublicKey publicKey = KeyFactory.getInstance("Ed25519")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(PUBLIC_KEY_B64)));
        Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(publicKey);
        verifier.update(payloadBytes);
        if (!verifier.verify(sigBytes)) {
            log.warn("Bastillion license key signature is invalid");
            return null;
        }

        String[] fields = new String(payloadBytes, StandardCharsets.UTF_8).split("\\|", -1);
        if (fields.length != 5) {
            log.warn("Bastillion license key payload is malformed");
            return null;
        }

        ParsedLicense parsed = new ParsedLicense();
        parsed.licenseId = fields[0];
        parsed.licensee = fields[1];
        try {
            parsed.maxSystems = Integer.parseInt(fields[2]);
        } catch (NumberFormatException ex) {
            log.warn("Bastillion license key has a non-numeric system limit");
            return null;
        }
        if (!"none".equalsIgnoreCase(fields[4])) {
            try {
                parsed.expiryDate = LocalDate.parse(fields[4]);
            } catch (DateTimeParseException ex) {
                log.warn("Bastillion license key has an unparsable expiry date");
                return null;
            }
        }
        return parsed;
    }

    private static class ParsedLicense {
        String licenseId;
        String licensee;
        int maxSystems;
        LocalDate expiryDate;
    }
}
