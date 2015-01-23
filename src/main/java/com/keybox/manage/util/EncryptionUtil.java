/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.manage.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Utility to encrypt, decrypt, and hash
 */
public class EncryptionUtil {

    //secret key
    private static final byte[] key = new byte[]{'d', '3', '2', 't', 'p', 'd', 'M', 'o', 'I', '8', 'x', 'z', 'a', 'P', 'o', 'd'};

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
    public static String hash(String str, String salt) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            if (StringUtils.isNotEmpty(salt)) {
                md.update(Base64.decodeBase64(salt.getBytes()));
            }
            md.update(str.getBytes("UTF-8"));
            hash = new String(Base64.encodeBase64(md.digest()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
    }

    /**
     * return hash value of string
     *
     * @param str unhashed string
     * @return hash value of string
     */
    public static String hash(String str) {
        return hash(str, null);
    }

    /**
     * return encrypted value of string
     *
     * @param str unencrypted string
     * @return encrypted string
     */
    public static String encrypt(String str) {

        String retVal = null;
        if (str != null && str.length() > 0) {
            try {
                Cipher c = Cipher.getInstance("AES");
                c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
                byte[] encVal = c.doFinal(str.getBytes());
                retVal = new String(Base64.encodeBase64(encVal));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return retVal;
    }

    /**
     * return decrypted value of encrypted string
     *
     * @param str encrypted string
     * @return decrypted string
     */
    public static String decrypt(String str) {
        String retVal = null;
        if (str != null && str.length() > 0) {
            try {
                Cipher c = Cipher.getInstance("AES");
                c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
                byte[] decodedVal = Base64.decodeBase64(str.getBytes());
                retVal = new String(c.doFinal(decodedVal));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return retVal;
    }


}
