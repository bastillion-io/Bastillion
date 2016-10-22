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
package com.keybox.common.util;

import com.keybox.manage.util.EncryptionUtil;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to look up configurable commands and resources
 */
public class AppConfig {

    private static Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static PropertiesConfiguration prop;

    static {
        try {
            prop = new PropertiesConfiguration(AppConfig.class.getClassLoader().getResource(".").getPath() + "/KeyBoxConfig.properties");
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

        return prop.getString(name);
    }

    /**
     * gets the property from config
     *
     * @param name property name
     * @param defaultValue default value if property is empty
     * @return configuration property
     */
    public static String getProperty(String name, String defaultValue) {
        String value = prop.getString(name);
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

        String value = prop.getString(name);
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
     * @param name property name
     * @param value property value
     */
    public static void updateProperty(String name, String value) {

        //remove property
        if(StringUtils.isNotEmpty(value)) {
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
        if(StringUtils.isNotEmpty(property)) {
            return property.matches("^" +  EncryptionUtil.CRYPT_ALGORITHM + "\\{.*\\}$");
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
        if(StringUtils.isNotEmpty(retVal)) {
            retVal = retVal.replaceAll("^" +  EncryptionUtil.CRYPT_ALGORITHM + "\\{", "").replaceAll("\\}$","");
            retVal = EncryptionUtil.decrypt(retVal);
        }
        return  retVal;
    }

    /**
     * encrypts and updates the property in the config
     *
     * @param name property name
     * @param value property value
     */
    public static void encryptProperty(String name, String value) {
        //remove property
        if(StringUtils.isNotEmpty(value)) {
            try {
                prop.setProperty(name, EncryptionUtil.CRYPT_ALGORITHM + "{" + EncryptionUtil.encrypt(value) + "}");
                prop.save();
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }
        }
    }


}