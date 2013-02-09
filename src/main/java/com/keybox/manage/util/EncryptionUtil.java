/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;

/**
 * Utility to encrypt, decrypt, and hash
 */
public class EncryptionUtil {

    //secret key
    private static final byte[] key = new byte[]{'D', '3', '3', 'm', 'p', 'd', 'M', 'o', 'I', '8', 'x', 'z', 'a', 'P', 'o', 'd'};


    /**
     * return hash value of string
     *
     * @param str unhashed string
     * @return hash value of string
     */
    public static String hash(String str) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes("UTF-8"));
            hash = (new BASE64Encoder()).encode(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hash;
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
                retVal = new BASE64Encoder().encode(encVal);
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
                byte[] decodedVal = new BASE64Decoder().decodeBuffer(str);
                retVal = new String(c.doFinal(decodedVal));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        return retVal;
    }


}
