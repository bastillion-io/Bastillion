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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
	 * returns the key type which is used in the public key
	 *
	 * @param publicKey ssh-key to parse
	 * @return key type of ssh-key
	 */
	public static String generateKeyType(String publicKey) {
		//Public ssh-keys look like this:
		//SSH-1 RSA
		//[BIT_LENGTH] 37 [SOME_KEY_VALUE] [COMMENT]
		//SSH-2 RSA
		//ssh-rsa [SOME_KEY_VALUE] [COMMENT]
		//SSH-2 DSA
		//ssh-dss [SOME_KEY_VALUE] [COMMENT] 
		
		publicKey = publicKey.toLowerCase();
		
		String strSshKeyType = "[]";
		
		//Check the starting chars to make sure the key is a correct key!
		if (publicKey.startsWith("ssh-rsa")) {
			strSshKeyType = "[SSH-2 RSA]";
		}
		
		else if (publicKey.startsWith("ssh-dss")) {
			strSshKeyType = "[SSH-2 DSA]";
		}
		
		//SSH1 Check
		else if (publicKey.contains("37")) {
			String [] strSplitted = publicKey.split(" ");
			
			if (strSplitted.length > 0) {
				if (strSplitted[1].equals("37")) {
					strSshKeyType = "[SSH-1 RSA]";
				}
				
				else {
					strSshKeyType = "[None]";
				}
			}
			
		}
		
		else {
			strSshKeyType = "[None]";
		}
		
		return strSshKeyType;
	}
	
	/**
	 * returns a readable fingerprint of a public ssh-key
	 *
	 * @param publicKey ssh-key to convert
	 * @return fingerprint of ssh-key
	 */
	public static String generateFingerprint(String publicKey) throws IOException, NoSuchAlgorithmException {
		String strSshKeyType = generateKeyType(publicKey);
		
		SSH_KEY_TYPE pKeyType = SSH_KEY_TYPE.NONE;
		
		//Check the starting chars to make sure the key is a correct key!
		if (strSshKeyType.equals("[SSH-2 RSA]")) {
			pKeyType = SSH_KEY_TYPE.SSH2_RSA;
		}
		
		else if (strSshKeyType.equals("[SSH-2 DSA]")) {
			pKeyType = SSH_KEY_TYPE.SSH2_DSA;
		}
		
		else if (strSshKeyType.equals("[SSH-1 RSA]")) {
			pKeyType = SSH_KEY_TYPE.SSH1_RSA;
			return "Fingerprint for SSH-1 not supported!";
		}
		
		//Grab the actual key
		String strGoodKey = "";
		if (publicKey.contains(" ")) {
			String[] strKeyItems = publicKey.split(" ");
			
			if (pKeyType == SSH_KEY_TYPE.SSH1_RSA) {
				strGoodKey = strKeyItems[2];
			}
			
			else {
				strGoodKey = strKeyItems[1];
			}
		}
			
		//SSH-2 public keys are multiples of 4!
		if (strGoodKey.length() % 4 != 0 &&
			pKeyType != SSH_KEY_TYPE.SSH1_RSA &&
			pKeyType != SSH_KEY_TYPE.NONE) {
			return "Key invalid - length corrupted!";
		}
		
		byte[] bDecodedKey = Base64.decodeBase64(strGoodKey);
		
		//Generate md5 hash
		MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bMd5Fingerprint = md.digest(bDecodedKey);
		
		String strFingerprint = "";
        for (int i = 0; i < bMd5Fingerprint.length; i++) {
            strFingerprint += String.format("%02x", bMd5Fingerprint[i]);
            
            if (i+1 < bMd5Fingerprint.length)
                strFingerprint += ":";
        }

		//Change fingerprint because it's wrong anyway!
		if (pKeyType == SSH_KEY_TYPE.NONE) {
			strFingerprint = "Invalid key - SSH Type missing!";
		}
		

		return strFingerprint;		
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
	
	/**
	 * returns a SSH_TYPE_STATE to distinguish between keys.
	 *
	 * @return the needed enum
	 */
	public static enum SSH_KEY_TYPE {
		SSH1_RSA, SSH2_RSA, SSH2_DSA, NONE
	}


}
