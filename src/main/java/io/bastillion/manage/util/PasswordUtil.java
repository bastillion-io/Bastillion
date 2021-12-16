/**
 *    Copyright (C) 2015 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import io.bastillion.common.util.AppConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to validate password strength
 */
public class PasswordUtil {



        public static final String PASSWORD_REGEX= AppConfig.getProperty("passwordComplexityRegEx");
        public static final String PASSWORD_REQ_ERROR_MSG=AppConfig.getProperty("passwordComplexityMsg");

        private static Pattern pattern = Pattern.compile(PASSWORD_REGEX);

    private PasswordUtil() {
    }

    /**
         * Validation to ensure strong password
         *
         * @param password password 
         * @return true if strong password
         */
        public static boolean isValid(final String password){

            Matcher matcher = pattern.matcher(password);
            
            return matcher.matches();

        }
}
