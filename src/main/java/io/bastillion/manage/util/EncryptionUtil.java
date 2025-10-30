/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility to encrypt, decrypt, and hash
 */
public class EncryptionUtil {

    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    //secret key
    private static byte[] key = new byte[0];

    static {
        try {
            key = KeyStoreUtil.getSecretBytes(KeyStoreUtil.ENCRYPTION_KEY_ALIAS);
        } catch (GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
    }

    public static final String CRYPT_ALGORITHM = "AES";
    public static final String HASH_ALGORITHM = "SHA-256";

    private EncryptionUtil() {
    }

    /**
     * generate salt for hash
     *
     * @return salt
     */
    public static String generateSalt() {
        byte[] salt = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return new String(Base64.encodeBase64(salt));
    }

    /**
     * return hash value of string
     *
     * @param str  unhashed string
     * @param salt salt for hash
     * @return hash value of string
     */
    public static String hash(String str, String salt) throws NoSuchAlgorithmException {
        String hash;
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        if (StringUtils.isNotEmpty(salt)) {
            md.update(Base64.decodeBase64(salt.getBytes()));
        }
        md.update(str.getBytes(StandardCharsets.UTF_8));
        hash = new String(Base64.encodeBase64(md.digest()));
        return hash;
    }

    /**
     * return hash value of string
     *
     * @param str unhashed string
     * @return hash value of string
     */
    public static String hash(String str) throws NoSuchAlgorithmException {
        return hash(str, null);
    }

    /**
     * return encrypted value of string
     *
     * @param key secret key
     * @param str unencrypted string
     * @return encrypted string
     */
    public static String encrypt(byte[] key, String str) throws GeneralSecurityException {
        String retVal = null;
        if (str != null && str.length() > 0) {
            Cipher c = Cipher.getInstance(CRYPT_ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, CRYPT_ALGORITHM));
            byte[] encVal = c.doFinal(str.getBytes());
            retVal = new String(Base64.encodeBase64(encVal));
        }
        return retVal;
    }

    /**
     * return decrypted value of encrypted string
     *
     * @param key secret key
     * @param str encrypted string
     * @return decrypted string
     */
    public static String decrypt(byte[] key, String str) throws GeneralSecurityException {
        String retVal = null;
        if (str != null && str.length() > 0) {
            Cipher c = Cipher.getInstance(CRYPT_ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, CRYPT_ALGORITHM));
            byte[] decodedVal = Base64.decodeBase64(str.getBytes());
            retVal = new String(c.doFinal(decodedVal));
        }
        return retVal;
    }

    /**
     * return encrypted value of string
     *
     * @param str unencrypted string
     * @return encrypted string
     */
    public static String encrypt(String str) throws GeneralSecurityException {
        return encrypt(key, str);
    }

    /**
     * return decrypted value of encrypted string
     *
     * @param str encrypted string
     * @return decrypted string
     */
    public static String decrypt(String str) throws GeneralSecurityException {
        return decrypt(key, str);
    }

    /**
     * Encrypts a PEM-formatted private key using a user-supplied passphrase.
     * This is not Bastillion's internal encryption, but an optional wrapper
     * to protect exported private keys with AES-CBC and PBKDF2-HMAC-SHA256.
     *
     * @param privateKeyPEM PEM text to encrypt
     * @param passphrase    user-supplied passphrase
     * @return PEM with AES-encrypted contents
     */
    public static String encryptPrivateKeyPEM(String privateKeyPEM, String passphrase)
            throws GeneralSecurityException, IOException {

        if (passphrase == null || passphrase.isEmpty()) {
            return privateKeyPEM;
        }

        // Generate random salt and IV
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);

        // Derive AES key from passphrase
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec aesKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        // Encrypt the PEM contents
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(privateKeyPEM.getBytes(StandardCharsets.UTF_8));

        // Combine salt + iv + ciphertext for storage
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("-----BEGIN ENCRYPTED PRIVATE KEY-----\n".getBytes(StandardCharsets.US_ASCII));
        out.write(java.util.Base64.getMimeEncoder(70, "\n".getBytes(StandardCharsets.US_ASCII))
                .encode(concat(salt, iv, ciphertext)));
        out.write("\n-----END ENCRYPTED PRIVATE KEY-----\n".getBytes(StandardCharsets.US_ASCII));

        return out.toString(StandardCharsets.US_ASCII);
    }

    private static byte[] concat(byte[]... arrays) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] arr : arrays) {
            out.write(arr);
        }
        return out.toByteArray();
    }
}
