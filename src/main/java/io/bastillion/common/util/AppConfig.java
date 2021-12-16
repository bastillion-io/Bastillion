/**
 * Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
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