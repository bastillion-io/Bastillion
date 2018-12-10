/**
 *    Copyright (C) 2016 Loophole, LLC
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

import io.bastillion.common.util.AppConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;

/**
 * Utility class for creating entries in keystore
 */
public class KeyStoreUtil {

	private static Logger log = LoggerFactory.getLogger(KeyStoreUtil.class);

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
	private static int KEYLENGTH = AppConfig.getProperty("use256EncryptionKey").equals("true") ? 256 :128;

	//Alias for encryption keystore
	public static final String ENCRYPTION_KEY_ALIAS = "KEYBOX-ENCRYPTION_KEY";

	static {
		File f = new File(keyStoreFile);
		//load or create keystore
		if (f.isFile() && f.canRead()) {
			try {
				//load existing keystore
				keyStore = KeyStore.getInstance("JCEKS");
				FileInputStream keyStoreInputStream = new FileInputStream(f);
				keyStore.load(keyStoreInputStream, KEYSTORE_PASS);
			} catch (Exception ex) {
				log.error(ex.toString(), ex);
			}
			//create keystore
		} else {
			initializeKeyStore();
		}
	}

	/**
	 * get secret entry for alias
	 *
	 * @param alias keystore secret alias
	 * @return secret byte array
	 */
	public static byte[] getSecretBytes(String alias) {
		byte[] value = null;
		try {
			KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, new KeyStore.PasswordProtection(KEYSTORE_PASS));
			value = entry.getSecretKey().getEncoded();
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
		return value;
	}

	/**
	 * get secret entry for alias
	 *
	 * @param alias keystore secret alias
	 * @return secret string
	 */
	public static String getSecretString(String alias) {
		String value = null;
		try {
			KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, new KeyStore.PasswordProtection(KEYSTORE_PASS));
			value = new String(entry.getSecretKey().getEncoded());
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
		return value;
	}

	/**
	 * set secret in keystore
	 *
	 * @param alias  keystore secret alias
	 * @param secret keystore entry
	 */
	public static void setSecret(String alias, byte[] secret) {

		KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(KEYSTORE_PASS);
		try {
			SecretKeySpec secretKey = new SecretKeySpec(secret, 0, secret.length, "AES");
			KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
			keyStore.setEntry(alias, secretKeyEntry, protectionParameter);
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
	}


	/**
	 * set secret in keystore
	 *
	 * @param alias  keystore secret alias
	 * @param secret keystore entry
	 */
	public static void setSecret(String alias, String secret) {
		setSecret(alias, secret.getBytes());
	}

	/**
	 * delete existing and create new keystore
	 */
	public static void resetKeyStore() {
		File file = new File(keyStoreFile);
		try {
			if (file.exists()) {
				FileUtils.forceDelete(file);
			}
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
		//create new keystore
		initializeKeyStore();
	}

	/**
	 * create new keystore
	 */
	private static void initializeKeyStore() {
		try {
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
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
	}

}
