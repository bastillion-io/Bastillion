/**
 * Copyright 2015 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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

import com.keybox.common.util.AppConfig;

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
