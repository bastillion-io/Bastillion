/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * As a special exception, the copyright holders give permission to link the
 * code of portions of this program with the OpenSSL library under certain
 * conditions as described in each individual source file and distribute
 * linked combinations including the program with the OpenSSL library. You
 * must comply with the GNU Affero General Public License in all respects for
 * all of the code used other than as permitted herein. If you modify file(s)
 * with this exception, you may extend this exception to your version of the
 * file(s), but you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version. If you delete this
 * exception statement from all source files in the program, then also delete
 * it in the license file.
 */
package io.bastillion.common.util;

import io.bastillion.manage.util.EncryptionUtil;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to look up configurable commands and resources
 */
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);
	private static PropertiesConfiguration prop;
	public static final String CONFIG_DIR = StringUtils.isNotEmpty(System.getProperty("CONFIG_DIR")) ? System.getProperty("CONFIG_DIR").trim() : AppConfig.class.getClassLoader().getResource(".").getPath();

	static {
		try {
			//move configuration to specified dir
			if (StringUtils.isNotEmpty(System.getProperty("CONFIG_DIR"))) {
				File configFile = new File(CONFIG_DIR + "BastillionConfig.properties");
				if (!configFile.exists()) {
					File oldConfig = new File(AppConfig.class.getClassLoader().getResource(".").getPath() + "BastillionConfig.properties");
					FileUtils.moveFile(oldConfig, configFile);
				}
				configFile = new File(CONFIG_DIR + "jaas.conf");
				if (!configFile.exists()) {
					File oldConfig = new File(AppConfig.class.getClassLoader().getResource(".").getPath() + "jaas.conf");
					FileUtils.moveFile(oldConfig, configFile);
				}
			}
			prop = new PropertiesConfiguration(CONFIG_DIR + "BastillionConfig.properties");
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
	}

	private AppConfig() {
	}

	/**
	 * gets the property from config
	 *
	 * @param name property name
	 * @return configuration property
	 */
	public static String getProperty(String name) {
		String property = null;
		if (StringUtils.isNotEmpty(name)) {
			if (StringUtils.isNotEmpty(System.getenv(name))) {
				property = System.getenv(name);
			} else if (StringUtils.isNotEmpty(System.getenv(name.toUpperCase()))) {
				property = System.getenv(name.toUpperCase());
			} else {
				property = prop.getString(name);
			}
		}
		return property;

	}

	/**
	 * gets the property from config
	 *
	 * @param name         property name
	 * @param defaultValue default value if property is empty
	 * @return configuration property
	 */
	public static String getProperty(String name, String defaultValue) {
		String value = getProperty(name);
		if (StringUtils.isEmpty(value)) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * gets the property from config and replaces placeholders
	 *
	 * @param name           property name
	 * @param replacementMap name value pairs of place holders to replace
	 * @return configuration property
	 */
	public static String getProperty(String name, Map<String, String> replacementMap) {

		String value = getProperty(name);
		if (StringUtils.isNotEmpty(value)) {
			//iterate through map to replace text
			Set<String> keySet = replacementMap.keySet();
			for (String key : keySet) {
				//replace values in string
				String rVal = replacementMap.get(key);
				value = value.replace("${" + key + "}", rVal);
			}
		}
		return value;
	}

	/**
	 * removes property from the config
	 *
	 * @param name property name
	 */
	public static void removeProperty(String name) {

		//remove property
		try {
			prop.clearProperty(name);
			prop.save();
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
	}

	/**
	 * updates the property in the config
	 *
	 * @param name  property name
	 * @param value property value
	 */
	public static void updateProperty(String name, String value) {

		//remove property
		if (StringUtils.isNotEmpty(value)) {
			try {
				prop.setProperty(name, value);
				prop.save();
			} catch (Exception ex) {
				log.error(ex.toString(), ex);
			}
		}
	}


	/**
	 * checks if property is encrypted
	 *
	 * @param name property name
	 * @return true if property is encrypted
	 */
	public static boolean isPropertyEncrypted(String name) {
		String property = prop.getString(name);
		if (StringUtils.isNotEmpty(property)) {
			return property.matches("^" + EncryptionUtil.CRYPT_ALGORITHM + "\\{.*\\}$");
		} else {
			return false;
		}
	}

	/**
	 * decrypts and returns the property from config
	 *
	 * @param name property name
	 * @return configuration property
	 */
	public static String decryptProperty(String name) {
		String retVal = prop.getString(name);
		if (StringUtils.isNotEmpty(retVal)) {
			retVal = retVal.replaceAll("^" + EncryptionUtil.CRYPT_ALGORITHM + "\\{", "").replaceAll("\\}$", "");
			retVal = EncryptionUtil.decrypt(retVal);
		}
		return retVal;
	}

	/**
	 * encrypts and updates the property in the config
	 *
	 * @param name  property name
	 * @param value property value
	 */
	public static void encryptProperty(String name, String value) {
		//remove property
		if (StringUtils.isNotEmpty(value)) {
			try {
				prop.setProperty(name, EncryptionUtil.CRYPT_ALGORITHM + "{" + EncryptionUtil.encrypt(value) + "}");
				prop.save();
			} catch (Exception ex) {
				log.error(ex.toString(), ex);
			}
		}
	}


}