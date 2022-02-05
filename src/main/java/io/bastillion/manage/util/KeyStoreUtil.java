/**
 * Copyright (C) 2016 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import io.bastillion.common.util.AppConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * Utility class for creating entries in keystore
 */
public class KeyStoreUtil {

    private static final Logger log = LoggerFactory.getLogger(KeyStoreUtil.class);

    private static KeyStore keyStore = null;
    private static final String keyStoreFile = AppConfig.CONFIG_DIR
            + "/bastillion.jceks";
    private static final char[] KEYSTORE_PASS = new char[]{
            'G', '~', 'r', 'x', 'Z', 'E', 'w', 'f', 'a', '[', '!', 'f', 'Z', 'd', '*', 'L', '8', 'm', 'h', 'u', '#',
            'j', '9', ':', '~', ';', 'U', '>', 'O', 'i', '8', 'r', 'C', '}', 'f', 't', '%', '[', 'H', 'h', 'M', '&',
            'K', ':', 'l', '5', 'c', 'H', '6', 'r', 'A', 'E', '.', 'F', 'Y', 'W', '}', '{', '*', '8', 'd', 'E', 'C',
            'A', '6', 'F', 'm', 'j', 'u', 'A', 'Q', '%', '{', '/', '@', 'm', '&', '5', 'S', 'q', '4', 'Q', '+', 'Y',
            '|', 'X', 'W', 'z', '8', '<', 'j', 'd', 'a', '}', '`', '0', 'N', 'B', '3', 'i', 'v', '5', 'U', ' ', '2',
            'd', 'd', '(', '&', 'J', '_', '9', 'o', '(', '2', 'I', '`', ';', '>', '#', '$', 'X', 'j', '&', '&', '%',
            '>', '#', '7', 'q', '>', ')', 'L', 'A', 'v', 'h', 'j', 'i', '8', '~', ')', 'a', '~', 'W', '/', 'l', 'H',
            'L', 'R', '+', '\\', 'i', 'R', '_', '+', 'y', 's', '0', 'n', '\'', '=', '{', 'B', ':', 'l', '1', '%', '^',
            'd', 'n', 'H', 'X', 'B', '$', 'f', '"', '#', ')', '{', 'L', '/', 'q', '\'', 'O', '%', 's', 'M', 'Q', ']',
            'D', 'v', ';', 'L', 'C', 'd', '?', 'D', 'l', 'h', 'd', 'i', 'N', '4', 'R', '>', 'O', ';', '$', '(', '4',
            '-', '0', '^', 'Y', ')', '5', 'V', 'M', '7', 'S', 'a', 'c', 'D', 'C', 'w', 'A', 'o', 'n', 's', 'r', '*',
            'G', '[', 'l', 'h', '$', 'U', 's', '_', 'D', 'f', 'X', '~', '.', '7', 'B', 'A', 'E', '(', '#', ']', ':',
            '`', ',', 'k', 'y'};
    private static final int KEYLENGTH = 256;

    //Alias for encryption keystore
    public static final String ENCRYPTION_KEY_ALIAS = "KEYBOX-ENCRYPTION_KEY";

    static {
        File f = new File(keyStoreFile);
        //load or create keystore
        try {
            if (f.isFile() && f.canRead()) {
                keyStore = KeyStore.getInstance("JCEKS");
                FileInputStream keyStoreInputStream = new FileInputStream(f);
                keyStore.load(keyStoreInputStream, KEYSTORE_PASS);
            }
            //create keystore
            else {
                initializeKeyStore();
            }
        } catch (IOException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
    }

    /**
     * get secret entry for alias
     *
     * @param alias keystore secret alias
     * @return secret byte array
     */
    public static byte[] getSecretBytes(String alias) throws GeneralSecurityException {
        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, new KeyStore.PasswordProtection(KEYSTORE_PASS));
        return entry.getSecretKey().getEncoded();
    }

    /**
     * get secret entry for alias
     *
     * @param alias keystore secret alias
     * @return secret string
     */
    public static String getSecretString(String alias) throws GeneralSecurityException {
        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, new KeyStore.PasswordProtection(KEYSTORE_PASS));
        return new String(entry.getSecretKey().getEncoded());
    }

    /**
     * set secret in keystore
     *
     * @param alias  keystore secret alias
     * @param secret keystore entry
     */
    public static void setSecret(String alias, byte[] secret) throws KeyStoreException {

        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(KEYSTORE_PASS);
        SecretKeySpec secretKey = new SecretKeySpec(secret, 0, secret.length, "AES");
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        keyStore.setEntry(alias, secretKeyEntry, protectionParameter);

    }


    /**
     * set secret in keystore
     *
     * @param alias  keystore secret alias
     * @param secret keystore entry
     */
    public static void setSecret(String alias, String secret) throws KeyStoreException {
        setSecret(alias, secret.getBytes());
    }

    /**
     * delete existing and create new keystore
     */
    public static void resetKeyStore() throws IOException, GeneralSecurityException {
        File file = new File(keyStoreFile);
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }

        //create new keystore
        initializeKeyStore();
    }

    /**
     * create new keystore
     */
    private static void initializeKeyStore() throws GeneralSecurityException, IOException {
        keyStore = KeyStore.getInstance("JCEKS");
        //create keystore
        keyStore.load(null, KEYSTORE_PASS);

        //set encryption key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(KEYLENGTH);
        KeyStoreUtil.setSecret(KeyStoreUtil.ENCRYPTION_KEY_ALIAS, keyGenerator.generateKey().getEncoded());

        //write keystore
        FileOutputStream fos = new FileOutputStream(keyStoreFile);
        keyStore.store(fos, KEYSTORE_PASS);
        fos.close();
    }

}
