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
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Properties;

/**
 * Utility to look up configurable commands and resources. Environment variables are
 * preferred (see getProperty) - BastillionConfig.properties / CONFIG_DIR exist mainly for
 * backward compatibility and for values that get persisted at runtime (e.g. a
 * DBInitServlet-generated dbPassword).
 */
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static PropertiesConfiguration prop;

    public static final String CONFIG_DIR = normalizeDir(
            StringUtils.isNotEmpty(System.getProperty("CONFIG_DIR"))
                    ? System.getProperty("CONFIG_DIR").trim()
                    : defaultConfigDir());

    private static FileBasedConfigurationBuilder<PropertiesConfiguration> builder;

    // Bundled defaults (BastillionConfig.properties as shipped on the classpath - always
    // present, read-only). Final fallback in getProperty() so unset-anywhere properties
    // (e.g. maxActive) still resolve without requiring a file on disk.
    private static final Properties DEFAULTS = loadDefaults();

    private static Properties loadDefaults() {
        Properties defaults = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("BastillionConfig.properties")) {
            if (in != null) {
                defaults.load(in);
            }
        } catch (IOException ex) {
            log.error("Error loading bundled default configuration", ex);
        }
        return defaults;
    }

    static {
        try {
            // Move configuration files to specified dir if needed
            if (StringUtils.isNotEmpty(System.getProperty("CONFIG_DIR"))) {
                moveIfAbsent("BastillionConfig.properties");
                moveIfAbsent("jaas.conf");
            }

            // Build Commons Configuration 2.x-compatible builder. allowFailOnInit=true so a
            // missing BastillionConfig.properties (the expected case when running purely
            // off environment variables) starts an empty in-memory config instead of
            // throwing - getProperty() falls back to env vars regardless.
            Parameters params = new Parameters();
            builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class, null, true)
                    .configure(params.properties()
                            .setFileName(CONFIG_DIR + "BastillionConfig.properties")
                            .setEncoding("UTF-8"));

            prop = builder.getConfiguration();

        } catch (ConfigurationException | IOException ex) {
            log.error("Error loading configuration: " + ex, ex);
        }
    }

    /**
     * Always the current working directory, whether running from the packaged jar or
     * unshaded (mvn compile exec:java). Deliberately NOT the classpath's "." resource
     * (target/classes when unshaded) - that's build output Maven freely overwrites on every
     * compile, which silently wiped runtime-persisted values like a generated keystore
     * password. getProperty() prefers environment variables anyway; a properties file here
     * is opt-in.
     */
    private static String defaultConfigDir() {
        return System.getProperty("user.dir") + File.separator;
    }

    // CONFIG_DIR is concatenated directly with filenames (see the FileBasedConfigurationBuilder
    // setup below) - normalize here so a user-supplied -DCONFIG_DIR without a trailing slash
    // doesn't silently merge into the filename.
    private static String normalizeDir(String dir) {
        return dir.endsWith(File.separator) ? dir : dir + File.separator;
    }

    private static void moveIfAbsent(String filename) throws IOException {
        File newFile = new File(CONFIG_DIR, filename);
        if (newFile.exists()) {
            return;
        }
        URL classpathDir = AppConfig.class.getClassLoader().getResource(".");
        if (classpathDir == null) {
            // Running from a jar: no on-disk "old location" to migrate from.
            return;
        }
        File oldFile = new File(classpathDir.getPath(), filename);
        if (oldFile.exists()) {
            FileUtils.moveFile(oldFile, newFile);
        }
    }

    private AppConfig() {
    }

    public static String getProperty(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        // First check environment variables: exact name, then camelCase converted to
        // SCREAMING_SNAKE_CASE (licenseKey -> LICENSE_KEY, dbUser -> DB_USER, ...)
        String property = System.getenv(name);
        if (StringUtils.isEmpty(property)) {
            property = System.getenv(toScreamingSnakeCase(name));
        }

        // Fallback to properties file, then to the bundled defaults
        if (StringUtils.isEmpty(property)) {
            property = prop.getString(name);
        }
        if (StringUtils.isEmpty(property)) {
            property = DEFAULTS.getProperty(name);
        }

        return property;
    }

    /**
     * Converts a camelCase property name to the SCREAMING_SNAKE_CASE convention used for
     * its environment variable override, e.g. licenseKey -> LICENSE_KEY, dbUser -> DB_USER.
     */
    static String toScreamingSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase();
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
