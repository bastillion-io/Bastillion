/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.common.util;

import com.keybox.manage.util.EncryptionUtil;
import org.apache.struts2.util.TokenHelper;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Utility to obtain the authentication token from the http session and the user id from the auth token
 */
public class AuthUtil {

    public static final String SESSION_ID = "sessionId";
    public static final String USER_ID = "userId";
    public static final String AUTH_TOKEN = "authToken";
    public static final String TIMEOUT = "timeout";
    public static final String CSRF_TOKEN_NM = "_csrf";

    private AuthUtil() {
    }

    /**
     * query session for OTP shared secret
     *
     * @param session http session
     * @return shared secret
     */
    public static String getOTPSecret(HttpSession session) {
        String secret = (String) session.getAttribute("otp_secret");
        secret = EncryptionUtil.decrypt(secret);
        return secret;
    }

    /**
     * set authentication type
     *
     * @param session http session
     * @param authType authentication type
     */
    public static void setAuthType(HttpSession session, String authType) {
        if (authType != null) {
            session.setAttribute("authType", authType);
        }
    }

    /**
     * query authentication type
     *
     * @param session http session
     * @return authentication type
     */
    public static String getAuthType(HttpSession session) {
        String authType = (String) session.getAttribute("authType");
        return authType;
    }

    /**
     * set user type
     *
     * @param session http session
     * @param userType user type
     */
    public static void setUserType(HttpSession session, String userType) {
        if (userType != null) {
            session.setAttribute("userType", userType);
        }
    }

    /**
     * query user type
     *
     * @param session http session
     * @return user type
     */
    public static String getUserType(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        return userType;
    }

    /**
     * set session id
     *
     * @param session http session
     * @param sessionId session id
     */
    public static void setSessionId(HttpSession session, Long sessionId) {
        if (sessionId != null) {
            session.setAttribute(SESSION_ID, EncryptionUtil.encrypt(sessionId.toString()));
        }
    }

    /**
     * query session id
     *
     * @param session http session
     * @return session id
     */
    public static Long getSessionId(HttpSession session) {
        Long sessionId = null;
        String sessionIdStr = EncryptionUtil.decrypt((String) session.getAttribute(SESSION_ID));
        if (sessionIdStr != null && !sessionIdStr.trim().equals("")) {
            sessionId = Long.parseLong(sessionIdStr);
        }
        return sessionId;
    }

    /**
     * query session for user id
     *
     * @param session http session
     * @return user id
     */
    public static Long getUserId(HttpSession session) {
        Long userId = null;
        String userIdStr = EncryptionUtil.decrypt((String) session.getAttribute(USER_ID));
        if (userIdStr != null && !userIdStr.trim().equals("")) {
            userId = Long.parseLong(userIdStr);
        }
        return userId;
    }

    /**
     * query session for authentication token
     *
     * @param session http session
     * @return authentication token
     */
    public static String getAuthToken(HttpSession session) {
        String authToken = (String) session.getAttribute(AUTH_TOKEN);
        authToken = EncryptionUtil.decrypt(authToken);
        return authToken;
    }

    /**
     * query session for timeout
     *
     * @param session http session
     * @return timeout string
     */
    public static String getTimeout(HttpSession session) {
        String timeout = (String) session.getAttribute(TIMEOUT);
        return timeout;
    }

    /**
     * query csrf token for session
     *
     * @param session http session
     * @return token string
     */
    public static String getCSRFToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_NM);
        return token;
    }

    /**
     * set session OTP shared secret
     *
     * @param session http session
     * @param secret shared secret
     */
    public static void setOTPSecret(HttpSession session, String secret) {
        if (secret != null && !secret.trim().equals("")) {
            session.setAttribute("otp_secret", EncryptionUtil.encrypt(secret));
        }
    }


    /**
     * set session user id
     *
     * @param session http session
     * @param userId user id
     */
    public static void setUserId(HttpSession session, Long userId) {
        if (userId != null) {
            session.setAttribute(USER_ID, EncryptionUtil.encrypt(userId.toString()));
        }
    }

    /**
     * set session authentication token
     *
     * @param session http session
     * @param authToken authentication token
     */
    public static void setAuthToken(HttpSession session, String authToken) {
        if (authToken != null && !authToken.trim().equals("")) {
            session.setAttribute(AUTH_TOKEN, EncryptionUtil.encrypt(authToken));
        }
    }

    /**
     * set session timeout
     *
     * @param session http session
     */
    public static void setTimeout(HttpSession session) {
        //set session timeout
        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss");
        Calendar timeout = Calendar.getInstance();
        timeout.add(Calendar.MINUTE, Integer.parseInt(AppConfig.getProperty("sessionTimeout", "15")));
        session.setAttribute(TIMEOUT, sdf.format(timeout.getTime()));
    }

    /**
     * generate csrf token for session
     *
     * @param session http session
     * @return _csrf token
     */
    public static String generateCSRFToken(HttpSession session) {
        String _csrf = TokenHelper.generateGUID();
        session.setAttribute(CSRF_TOKEN_NM, _csrf);
        return _csrf;
    }

    /**
     * delete all session information
     *
     * @param session
     */
    public static void deleteAllSession(HttpSession session) {

        session.setAttribute(CSRF_TOKEN_NM, null);
        session.setAttribute(TIMEOUT, null);
        session.setAttribute(AUTH_TOKEN, null);
        session.setAttribute(USER_ID, null);
        session.setAttribute(SESSION_ID, null);

        session.invalidate();
    }

}
