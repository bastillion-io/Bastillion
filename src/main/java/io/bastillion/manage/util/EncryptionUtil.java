/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
 */
package io.bastillion.manage.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to encrypt, decrypt, and hash
 */
public class EncryptionUtil {

    private static Logger log = LoggerFactory.getLogger(EncryptionUtil.class);

    //secret key
    private static final byte[] key = KeyStoreUtil.getSecretBytes(KeyStoreUtil.ENCRYPTION_KEY_ALIAS);

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
    public static String hash(String str, String salt) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            if (StringUtils.isNotEmpty(salt)) {
                md.update(Base64.decodeBase64(salt.getBytes()));
            }
            md.update(str.getBytes("UTF-8"));
            hash = new String(Base64.encodeBase64(md.digest()));
        } catch (Exception e) {
            log.error(e.toString(), e);
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
     * @param key secret key
     * @param str unencrypted string
     * @return encrypted string
     */
    public static String encrypt(byte[] key, String str) {

        String retVal = null;
        if (str != null && str.length() > 0) {
            try {
                Cipher c = Cipher.getInstance(CRYPT_ALGORITHM);
                c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, CRYPT_ALGORITHM));
                byte[] encVal = c.doFinal(str.getBytes());
                retVal = new String(Base64.encodeBase64(encVal));
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }

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
    public static String decrypt(byte[] key, String str) {
        String retVal = null;
        if (str != null && str.length() > 0) {
            try {
                Cipher c = Cipher.getInstance(CRYPT_ALGORITHM);
                c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, CRYPT_ALGORITHM));
                byte[] decodedVal = Base64.decodeBase64(str.getBytes());
                retVal = new String(c.doFinal(decodedVal));
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }

        }
        return retVal;
    }

    /**
     * return encrypted value of string
     *
     * @param str unencrypted string
     * @return encrypted string
     */
    public static String encrypt(String str) {
        return encrypt(key, str);
    }

    /**
     * return decrypted value of encrypted string
     *
     * @param str encrypted string
     * @return decrypted string
     */
    public static String decrypt(String str) {
        return decrypt(key, str);
    }


}
