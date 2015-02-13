package com.keybox.manage.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * utility to validate password strength 
 */
public class PasswordUtil {



        public static final String PASSWORD_REGEX="((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()]).{8,20})";
        public static final String PASSWORD_REQ_ERROR_MSG="Passwords must be 8 to 20 characters, contain one digit, one lowercase, one uppercase, and one special character";



        /**
         * Validation to ensure strong password
         *
         * @param password password 
         * @return true if strong password
         */
        public static boolean isValid(final String password){
            Pattern pattern = Pattern.compile(PASSWORD_REGEX);

            Matcher matcher = pattern.matcher(password);
            
            return matcher.matches();

        }
}
