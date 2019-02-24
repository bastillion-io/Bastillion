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
package io.bastillion.manage.control;

import io.bastillion.common.util.AppConfig;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.OTPUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginKtrl extends BaseKontroller {

    //check if otp is enabled
    @Model(name = "otpEnabled")
    static final Boolean otpEnabled = ("required".equals(AppConfig.getProperty("oneTimePassword")) || "optional".equals(AppConfig.getProperty("oneTimePassword")));
    private static Logger loginAuditLogger = LoggerFactory.getLogger("io.bastillion.manage.control.LoginAudit");
    private final String AUTH_ERROR = "Authentication Failed : Login credentials are invalid";
    private final String AUTH_ERROR_NO_PROFILE = "Authentication Failed : There are no profiles assigned to this account";
    @Model(name = "auth")
    Auth auth;


    public LoginKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/login", method = MethodType.GET)
    public String login() {
        return "/login.html";
    }

    @Kontrol(path = "/loginSubmit", method = MethodType.POST)
    public String loginSubmit() {
        String retVal = "redirect:/admin/menu.html";

        String authToken = AuthDB.login(auth);

        //get client IP
        String clientIP = AuthUtil.getClientIPAddress(getRequest());
        if (authToken != null) {

            User user = AuthDB.getUserByAuthToken(authToken);
            if (user != null) {
                String sharedSecret = null;
                if (otpEnabled) {
                    sharedSecret = AuthDB.getSharedSecret(user.getId());
                    if (StringUtils.isNotEmpty(sharedSecret) && (auth.getOtpToken() == null || !OTPUtil.verifyToken(sharedSecret, auth.getOtpToken()))) {
                        loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - " + AUTH_ERROR);
                        addError(AUTH_ERROR);
                        return "/login.html";
                    }
                }
                //check to see if admin has any assigned profiles
                if (!User.MANAGER.equals(user.getUserType()) && (user.getProfileList() == null || user.getProfileList().size() <= 0)) {
                    loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - " + AUTH_ERROR_NO_PROFILE);
                    addError(AUTH_ERROR_NO_PROFILE);
                    return "/login.html";
                }

                AuthUtil.setAuthToken(getRequest().getSession(), authToken);
                AuthUtil.setUserId(getRequest().getSession(), user.getId());
                AuthUtil.setAuthType(getRequest().getSession(), user.getAuthType());
                AuthUtil.setTimeout(getRequest().getSession());
                AuthUtil.setUsername(getRequest().getSession(), user.getUsername());

                //for first time login redirect to set OTP
                if (otpEnabled && StringUtils.isEmpty(sharedSecret)) {
                    retVal = "redirect:/admin/viewOTP.ktrl";
                } else if ("changeme".equals(auth.getPassword()) && Auth.AUTH_BASIC.equals(user.getAuthType())) {
                    retVal = "redirect:/admin/userSettings.ktrl";
                }
                loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - Authentication Success");
            }

        } else {
            loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - " + AUTH_ERROR);
            addError(AUTH_ERROR);
            retVal = "/login.html";
        }


        return retVal;
    }


    @Kontrol(path = "/logout", method = MethodType.GET)
    public String logout() {
        AuthUtil.deleteAllSession(getRequest().getSession());
        return "redirect:/";
    }


    /**
     * Validates fields for auth submit
     */
    @Validate(input = "/login.html")
    public void validateLoginSubmit() {
        if (auth.getUsername() == null ||
                auth.getUsername().trim().equals("")) {
            addFieldError("auth.username", "Required");
        }
        if (auth.getPassword() == null ||
                auth.getPassword().trim().equals("")) {
            addFieldError("auth.password", "Required");
        }

    }
}
