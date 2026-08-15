/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.util;

import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.ExternalAuthUtil;
import io.bastillion.manage.util.OTPUtil;
import io.bastillion.manage.util.SamlAuthUtil;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

/**
 * Establishes an authenticated Bastillion session for an already-minted auth token - the
 * single choke point for OTP/profile/expiration enforcement, shared by LoginKtrl (local +
 * LDAP login) and SamlAcsServlet (SAML login), so a fix to any of these checks can't be
 * applied to one call site and missed on the other. Never renders anything - callers own
 * their own success/failure UX.
 */
public class AuthSessionUtil {

    //mirrors LoginKtrl's otpEnabled - kept as its own copy rather than reaching into
    //io.bastillion.manage.control from this common-util package
    private static final boolean otpEnabled = ("required".equals(AppConfig.getProperty("oneTimePassword"))
            || "optional".equals(AppConfig.getProperty("oneTimePassword")));

    //true iff any non-local auth mechanism is configured (JAAS/LDAP, SAML, ...) - one flag
    //for anything that only cares "is there some external identity source" (e.g. hiding the
    //local password form for users authenticated that way) rather than which one; adding a
    //third external mechanism later means adding one more `|| XyzAuthUtil.xyzAuthEnabled`
    //term here, not touching every place that currently checks two flags separately.
    public static final boolean externalAuthEnabled = ExternalAuthUtil.externalAuthEnabled || SamlAuthUtil.samlAuthEnabled;

    public enum Status { SUCCESS, OTP_ENROLLMENT_REQUIRED, OTP_INVALID, NO_PROFILES, EXPIRED, NOT_FOUND }

    public record Result(Status status, User user) {
    }

    private AuthSessionUtil() {
    }

    /**
     * @param otpToken            OTP code submitted alongside this login attempt, or null if
     *                            the auth mechanism has nowhere to carry one
     * @param otpAlreadySatisfied true to skip Bastillion's own OTP-code check entirely, for
     *                            auth mechanisms that assert their own equivalent upstream
     *                            (SAML - the IdP is expected to enforce its own MFA/Conditional
     *                            Access policy); false for local/LDAP, where the login form's
     *                            OTP field is the only thing standing in for it. First-time OTP
     *                            *enrollment* still applies either way - only the "already
     *                            enrolled, must supply a matching code this request" branch is
     *                            skipped.
     */
    public static Result establishSession(HttpServletRequest request, HttpServletResponse response,
                                           String authToken, Long otpToken, boolean otpAlreadySatisfied)
            throws SQLException, GeneralSecurityException {

        User user = AuthDB.getUserByAuthToken(authToken);
        if (user == null) {
            return new Result(Status.NOT_FOUND, null);
        }

        String sharedSecret = null;
        if (otpEnabled && !otpAlreadySatisfied) {
            sharedSecret = AuthDB.getSharedSecret(user.getId());
            if (StringUtils.isNotEmpty(sharedSecret) && (otpToken == null || !OTPUtil.verifyToken(sharedSecret, otpToken))) {
                return new Result(Status.OTP_INVALID, user);
            }
        }
        //check to see if admin has any assigned profiles
        if (!User.MANAGER.equals(user.getUserType()) && (user.getProfileList() == null || user.getProfileList().size() <= 0)) {
            return new Result(Status.NO_PROFILES, user);
        }
        //check to see if account has expired
        if (user.isExpired()) {
            return new Result(Status.EXPIRED, user);
        }

        AuthUtil.setAuthToken(request.getSession(), authToken);
        AuthUtil.setUserId(request.getSession(), user.getId());
        AuthUtil.setAuthType(request.getSession(), user.getAuthType());
        AuthUtil.setTimeout(request.getSession());
        AuthUtil.setUsername(request.getSession(), user.getUsername());

        AuthDB.updateLastLogin(user);
        ThemeUtil.setThemeCookie(request, response, user.getUiTheme());

        //for first time login redirect to set OTP
        if (otpEnabled && !otpAlreadySatisfied && StringUtils.isEmpty(sharedSecret)) {
            return new Result(Status.OTP_ENROLLMENT_REQUIRED, user);
        }
        return new Result(Status.SUCCESS, user);
    }
}
