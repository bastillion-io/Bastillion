/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.common.util;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Utility to look up configurable commands and resources
 */
public class AppConfigLkup {

    private static ResourceBundle prop = ResourceBundle.getBundle("KeyBoxConfig");

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
     * gets the property from config and replaces placeholders
     *
     * @param name property name
     * @param replacementMap name value pairs of place holders to replace
     * @return configuration property
     */
    public static String getProperty(String name, Map<String, String> replacementMap) {

        String value = prop.getString(name);
        //iterate through map to replace text
        Set<String> keySet = replacementMap.keySet();
        for(String key :keySet){
            //replace values in string
            String rVal=replacementMap.get(key);
            value=value.replace("${"+key+"}",rVal);
        }
        return value;
    }

}
