/**
 * Copyright (C) 2013 Loophole, LLC
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    // Bastillion secret key material for app-level encryption
    private static byte[] key = new byte[0];

    static {
        try {
            key = KeyStoreUtil.getSecretBytes(KeyStoreUtil.ENCRYPTION_KEY_ALIAS);
        } catch (GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
    }

    // Algorithms / transforms
    public static final String CRYPT_ALGORITHM = "AES"; // key alg
    private static final String T_GCM = "AES/GCM/NoPadding";
    private static final String T_CBC = "AES/CBC/PKCS5Padding";         // legacy PEM wrapper
    private static final String T_ECB = "AES/ECB/PKCS5Padding";          // legacy app encrypt (Cipher.getInstance("AES"))
    public static final String HASH_ALGORITHM = "SHA-256";

    // GCM params
    private static final int GCM_IV_LEN = 12;         // 96-bit IV recommended
    private static final int GCM_TAG_LEN_BITS = 128;  // 16-byte tag

    // Version markers for serialized ciphertexts
    private static final String V2_PREFIX = "v2:";    // GCM
    private static final String PEM_BEGIN_V2 = "-----BEGIN ENCRYPTED PRIVATE KEY (GCM v2)-----";
    private static final String PEM_END_V2   = "-----END ENCRYPTED PRIVATE KEY (GCM v2)-----";
    // Legacy (kept for read-compat)
    private static final String PEM_BEGIN_V1 = "-----BEGIN ENCRYPTED PRIVATE KEY-----";
    private static final String PEM_END_V1   = "-----END ENCRYPTED PRIVATE KEY-----";

    private EncryptionUtil() {}

    // --------------------------------------------------------------------
    // Salt & Hash (unchanged)
    // --------------------------------------------------------------------
    public static String generateSalt() {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        return new String(Base64.encodeBase64(salt));
    }

    public static String hash(String str, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        if (StringUtils.isNotEmpty(salt)) {
            md.update(Base64.decodeBase64(salt.getBytes(StandardCharsets.UTF_8)));
        }
        md.update(str.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.encodeBase64(md.digest()), StandardCharsets.UTF_8);
    }

    public static String hash(String str) throws NoSuchAlgorithmException {
        return hash(str, null);
    }

    // --------------------------------------------------------------------
    // Application-level encryption (now GCM v2 with legacy fallback)
    // --------------------------------------------------------------------
    public static String encrypt(byte[] rawKey, String plaintext) throws GeneralSecurityException {
        if (StringUtils.isEmpty(plaintext)) return null;

        byte[] iv = new byte[GCM_IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher c = Cipher.getInstance(T_GCM);
        SecretKeySpec sk = new SecretKeySpec(rawKey, CRYPT_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LEN_BITS, iv);
        c.init(Cipher.ENCRYPT_MODE, sk, spec);

        byte[] ct = c.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Serialize: v2: base64( iv || ct )
        ByteBuffer bb = ByteBuffer.allocate(iv.length + ct.length);
        bb.put(iv).put(ct);
        String b64 = java.util.Base64.getEncoder().encodeToString(bb.array());
        return V2_PREFIX + b64;
    }

    public static String decrypt(byte[] rawKey, String serialized) throws GeneralSecurityException {
        if (StringUtils.isEmpty(serialized)) return null;

        // Prefer v2 GCM
        if (serialized.startsWith(V2_PREFIX)) {
            String b64 = serialized.substring(V2_PREFIX.length());
            byte[] all = java.util.Base64.getDecoder().decode(b64);
            if (all.length < GCM_IV_LEN + 16) { // need room for tag too
                throw new GeneralSecurityException("ciphertext too short");
            }
            byte[] iv = new byte[GCM_IV_LEN];
            byte[] ct = new byte[all.length - GCM_IV_LEN];
            System.arraycopy(all, 0, iv, 0, GCM_IV_LEN);
            System.arraycopy(all, GCM_IV_LEN, ct, 0, ct.length);

            Cipher c = Cipher.getInstance(T_GCM);
            SecretKeySpec sk = new SecretKeySpec(rawKey, CRYPT_ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, sk, new GCMParameterSpec(GCM_TAG_LEN_BITS, iv));
            return new String(c.doFinal(ct), StandardCharsets.UTF_8);
        }

        // Legacy fallback #1: old default "AES" (i.e., AES/ECB/PKCS5Padding)
        try {
            Cipher c = Cipher.getInstance(T_ECB);
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(rawKey, CRYPT_ALGORITHM));
            byte[] decodedVal = Base64.decodeBase64(serialized.getBytes(StandardCharsets.UTF_8));
            return new String(c.doFinal(decodedVal), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException legacy1) {
            // fall through to next
        }

        // Legacy fallback #2 (rare): if someone stored CBC without the v2 prefix
        try {
            // Not enough info to recover IV unless it was concatenated externally,
            // so we only attempt ECB fallback above. CBC without metadata is unrecoverable.
            throw new GeneralSecurityException("Unsupported legacy format without metadata");
        } catch (GeneralSecurityException legacy2) {
            throw legacy2;
        }
    }

    public static String encrypt(String str) throws GeneralSecurityException {
        return encrypt(key, str);
    }

    public static String decrypt(String str) throws GeneralSecurityException {
        return decrypt(key, str);
    }

    // --------------------------------------------------------------------
    // PEM private key envelope (PBKDF2-HMAC-SHA256 + AES-GCM, with legacy CBC read)
    // --------------------------------------------------------------------

    /**
     * Encrypts a PEM-formatted private key using a user-supplied passphrase.
     * v2 format: PBKDF2-HMAC-SHA256 (65536 iters, 256-bit key) + AES-GCM(128) with 12-byte IV.
     * Serialized as:
     *   -----BEGIN ENCRYPTED PRIVATE KEY (GCM v2)-----
     *   base64( 0x02 || salt(16) || iv(12) || ciphertext+tag )
     *   -----END ENCRYPTED PRIVATE KEY (GCM v2)-----
     */
    public static String encryptPrivateKeyPEM(String privateKeyPEM, String passphrase)
            throws GeneralSecurityException, IOException {
        if (StringUtils.isEmpty(passphrase) || StringUtils.isEmpty(privateKeyPEM)) {
            return privateKeyPEM;
        }

        SecureRandom rnd = new SecureRandom();
        byte[] salt = new byte[16];
        byte[] iv = new byte[GCM_IV_LEN];
        rnd.nextBytes(salt);
        rnd.nextBytes(iv);

        SecretKeySpec aesKey = deriveAesKey(passphrase, salt);

        Cipher cipher = Cipher.getInstance(T_GCM);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LEN_BITS, iv));
        byte[] ct = cipher.doFinal(privateKeyPEM.getBytes(StandardCharsets.UTF_8));

        // versioned blob: 0x02 || salt || iv || ct
        ByteBuffer bb = ByteBuffer.allocate(1 + salt.length + iv.length + ct.length);
        bb.put((byte) 0x02).put(salt).put(iv).put(ct);

        String body = java.util.Base64.getMimeEncoder(70, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(bb.array());

        StringBuilder sb = new StringBuilder();
        sb.append(PEM_BEGIN_V2).append("\n")
                .append(body).append("\n")
                .append(PEM_END_V2).append("\n");
        return sb.toString();
    }

    /**
     * Decrypts either v2 (GCM) or legacy v1 (CBC) envelopes.
     */
    public static String decryptPrivateKeyPEM(String pem, String passphrase)
            throws GeneralSecurityException {
        if (StringUtils.isEmpty(passphrase) || StringUtils.isEmpty(pem)) {
            return pem;
        }

        if (pem.contains(PEM_BEGIN_V2)) {
            String b64 = between(pem, PEM_BEGIN_V2, PEM_END_V2);
            byte[] blob = java.util.Base64.getMimeDecoder().decode(b64);

            if (blob.length < 1 + 16 + GCM_IV_LEN + 16) {
                throw new GeneralSecurityException("Invalid v2 envelope");
            }
            ByteBuffer bb = ByteBuffer.wrap(blob);
            byte ver = bb.get();
            if (ver != 0x02) throw new GeneralSecurityException("Unsupported envelope version");

            byte[] salt = new byte[16];
            byte[] iv = new byte[GCM_IV_LEN];
            bb.get(salt).get(iv);
            byte[] ct = new byte[bb.remaining()];
            bb.get(ct);

            SecretKeySpec aesKey = deriveAesKey(passphrase, salt);
            Cipher cipher = Cipher.getInstance(T_GCM);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LEN_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        }

        // Legacy CBC (v1) with serialized salt+iv+ciphertext under the classic headers.
        if (pem.contains(PEM_BEGIN_V1)) {
            try {
                String b64 = between(pem, PEM_BEGIN_V1, PEM_END_V1);
                byte[] all = java.util.Base64.getMimeDecoder().decode(b64);
                if (all.length < 16 + 16 + 16) throw new GeneralSecurityException("Invalid v1 envelope");

                byte[] salt = new byte[16];
                byte[] iv = new byte[16];
                byte[] ct = new byte[all.length - 32];

                System.arraycopy(all, 0, salt, 0, 16);
                System.arraycopy(all, 16, iv, 0, 16);
                System.arraycopy(all, 32, ct, 0, ct.length);

                SecretKeySpec aesKey = deriveAesKey(passphrase, salt);
                Cipher cipher = Cipher.getInstance(T_CBC);
                cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
                byte[] pt = cipher.doFinal(ct);
                return new String(pt, StandardCharsets.UTF_8);
            } catch (GeneralSecurityException e) {
                throw new GeneralSecurityException("Failed to decrypt legacy CBC PEM", e);
            }
        }

        throw new GeneralSecurityException("Unrecognized PEM envelope");
    }

    // --------------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------------
    private static SecretKeySpec deriveAesKey(String passphrase, byte[] salt) throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // Same work factor as before; you can raise to, say, 200k if perf allows.
        PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private static String between(String s, String begin, String end) {
        int i = s.indexOf(begin);
        int j = s.indexOf(end);
        if (i < 0 || j < 0 || j <= i) return "";
        i += begin.length();
        return s.substring(i, j).replace("\r", "").trim();
    }
}
