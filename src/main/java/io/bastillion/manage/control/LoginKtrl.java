/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

public class LoginKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(LoginKtrl.class);

    //check if otp is enabled
    @Model(name = "otpEnabled")
    static final Boolean otpEnabled = ("required".equals(AppConfig.getProperty("oneTimePassword")) || "optional".equals(AppConfig.getProperty("oneTimePassword")));
    private static final Logger loginAuditLogger = LoggerFactory.getLogger("io.bastillion.manage.control.LoginAudit");
    private final String AUTH_ERROR = "Authentication Failed : Login credentials are invalid";
    private final String AUTH_ERROR_NO_PROFILE = "Authentication Failed : There are no profiles assigned to this account";
    private final String AUTH_ERROR_EXPIRED_ACCOUNT = "Authentication Failed : Account has expired";
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
    public String loginSubmit() throws ServletException {
        String retVal = "redirect:/admin/menu.html";

        String authToken = null;
        try {
            authToken = AuthDB.login(auth);

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

                    //check to see if account has expired
                    if (user.isExpired()) {
                        loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - " + AUTH_ERROR_EXPIRED_ACCOUNT);
                        addError(AUTH_ERROR_EXPIRED_ACCOUNT);
                        return "/login.html";
                    }

                    AuthUtil.setAuthToken(getRequest().getSession(), authToken);
                    AuthUtil.setUserId(getRequest().getSession(), user.getId());
                    AuthUtil.setAuthType(getRequest().getSession(), user.getAuthType());
                    AuthUtil.setTimeout(getRequest().getSession());
                    AuthUtil.setUsername(getRequest().getSession(), user.getUsername());

                    AuthDB.updateLastLogin(user);

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
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
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
