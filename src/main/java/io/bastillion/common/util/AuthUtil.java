/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
 */
package io.bastillion.common.util;

import io.bastillion.manage.util.EncryptionUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Utility to obtain the authentication token from the http session and the user id from the auth token
 */
public class AuthUtil {

    public static final String SESSION_ID = "sessionId";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String AUTH_TOKEN = "authToken";
    public static final String TIMEOUT = "timeout";

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
     * @param session  http session
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
     * @param session  http session
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
     * @param session   http session
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
     * query session for the username
     *
     * @param session http session
     * @return username
     */
    public static String getUsername(HttpSession session) {
        return (String) session.getAttribute(USERNAME);
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
     * set session OTP shared secret
     *
     * @param session http session
     * @param secret  shared secret
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
     * @param userId  user id
     */
    public static void setUserId(HttpSession session, Long userId) {
        if (userId != null) {
            session.setAttribute(USER_ID, EncryptionUtil.encrypt(userId.toString()));
        }
    }


    /**
     * set session username
     *
     * @param session  http session
     * @param username username
     */
    public static void setUsername(HttpSession session, String username) {
        if (username != null) {
            session.setAttribute(USERNAME, username);
        }
    }


    /**
     * set session authentication token
     *
     * @param session   http session
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
     * delete all session information
     *
     * @param session
     */
    public static void deleteAllSession(HttpSession session) {

        session.setAttribute(TIMEOUT, null);
        session.setAttribute(AUTH_TOKEN, null);
        session.setAttribute(USER_ID, null);
        session.setAttribute(SESSION_ID, null);

        session.invalidate();
    }

    /**
     * return client ip from servlet request
     *
     * @param servletRequest http servlet request
     * @return client ip
     */
    public static String getClientIPAddress(HttpServletRequest servletRequest) {
        String clientIP = null;
        if (StringUtils.isNotEmpty(AppConfig.getProperty("clientIPHeader"))) {
            clientIP = servletRequest.getHeader(AppConfig.getProperty("clientIPHeader"));
        }
        if (StringUtils.isEmpty(clientIP)) {
            clientIP = servletRequest.getRemoteAddr();
        }
        return clientIP;
    }

}
