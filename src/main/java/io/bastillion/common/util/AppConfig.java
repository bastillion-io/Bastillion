package io.bastillion.common.util;

import io.bastillion.manage.util.EncryptionUtil;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * Utility to look up configurable commands and resources
 */
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static PropertiesConfiguration prop;

    public static final String CONFIG_DIR = StringUtils.isNotEmpty(System.getProperty("CONFIG_DIR"))
            ? System.getProperty("CONFIG_DIR").trim()
            : AppConfig.class.getClassLoader().getResource(".").getPath();

    private static FileBasedConfigurationBuilder<PropertiesConfiguration> builder;

    static {
        try {
            // Move configuration files to specified dir if needed
            if (StringUtils.isNotEmpty(System.getProperty("CONFIG_DIR"))) {
                moveIfAbsent("BastillionConfig.properties");
                moveIfAbsent("jaas.conf");
            }

            // Build Commons Configuration 2.x-compatible builder
            Parameters params = new Parameters();
            builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                    .configure(params.properties()
                            .setFileName(CONFIG_DIR + "BastillionConfig.properties")
                            .setEncoding("UTF-8"));

            prop = builder.getConfiguration();

        } catch (ConfigurationException | IOException ex) {
            log.error("Error loading configuration: " + ex, ex);
        }
    }

    private static void moveIfAbsent(String filename) throws IOException {
        File newFile = new File(CONFIG_DIR, filename);
        if (!newFile.exists()) {
            File oldFile = new File(AppConfig.class.getClassLoader().getResource(".").getPath(), filename);
            if (oldFile.exists()) {
                FileUtils.moveFile(oldFile, newFile);
            }
        }
    }

    private AppConfig() {
    }

    public static String getProperty(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        // First check environment variables
        String property = System.getenv(name);
        if (StringUtils.isEmpty(property)) {
            property = System.getenv(name.toUpperCase());
        }

        // Fallback to properties file
        if (StringUtils.isEmpty(property)) {
            property = prop.getString(name);
        }

        return property;
    }

    public static String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    public static String getProperty(String name, Map<String, String> replacementMap) {
        String value = getProperty(name);
        if (StringUtils.isNotEmpty(value)) {
            for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
                value = value.replace("${" + entry.getKey() + "}", entry.getValue());
            }
        }
        return value;
    }

    public static void removeProperty(String name) throws ConfigurationException {
        prop.clearProperty(name);
        builder.save();
    }

    public static void updateProperty(String name, String value) throws ConfigurationException {
        if (StringUtils.isNotEmpty(value)) {
            prop.setProperty(name, value);
            builder.save();
        }
    }

    public static boolean isPropertyEncrypted(String name) {
        String property = prop.getString(name);
        return StringUtils.isNotEmpty(property) && property.matches("^" + EncryptionUtil.CRYPT_ALGORITHM + "\\{.*\\}$");
    }

    public static String decryptProperty(String name) throws GeneralSecurityException {
        String retVal = prop.getString(name);
        if (StringUtils.isNotEmpty(retVal)) {
            retVal = retVal.replaceAll("^" + EncryptionUtil.CRYPT_ALGORITHM + "\\{", "")
                    .replaceAll("\\}$", "");
            retVal = EncryptionUtil.decrypt(retVal);
        }
        return retVal;
    }

    public static void encryptProperty(String name, String value)
            throws ConfigurationException, GeneralSecurityException {
        if (StringUtils.isNotEmpty(value)) {
            prop.setProperty(name, EncryptionUtil.CRYPT_ALGORITHM + "{" + EncryptionUtil.encrypt(value) + "}");
            builder.save();
        }
    }
}
