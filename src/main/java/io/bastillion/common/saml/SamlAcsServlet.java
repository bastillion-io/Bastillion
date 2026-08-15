/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.saml;

import io.bastillion.common.util.AuthSessionUtil;
import io.bastillion.manage.util.SamlAuthUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

/**
 * SAML Assertion Consumer Service - the endpoint the IdP POSTs the signed assertion back to.
 * A plain servlet, registered explicitly in web.xml at a path outside *.ktrl, *.html,
 * /manage/*, and /admin/*, not a @Kontrol controller - deliberately, not a style choice:
 * <p>
 * - CSRFFilter (mapped to /, *.ktrl, *.html) validates a session-bound _csrf param and
 *   invalidates the session on mismatch; the IdP's cross-origin auto-submit POST has no way
 *   to carry Bastillion's _csrf value.
 * - The session cookie is SameSite=Lax (see Main.java), which browsers don't send on a
 *   cross-site top-level POST - so even without CSRFFilter, no pre-redirect session state
 *   would be visible here anyway. This handler treats every incoming POST as having no
 *   prior session: it validates the assertion first, and only then calls
 *   request.getSession() (inside AuthSessionUtil.establishSession), minting a brand-new
 *   session + fresh Set-Cookie that rides fine on the subsequent same-site redirect.
 * <p>
 * A path outside those filters' url-patterns sidesteps both by construction, mirroring
 * exactly how DBInitServlet's /config registration already works.
 */
@WebServlet(name = "SamlAcsServlet", urlPatterns = {SamlSettingsUtil.ACS_PATH})
public class SamlAcsServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SamlAcsServlet.class);
    private static final Logger loginAuditLogger = LoggerFactory.getLogger("io.bastillion.manage.control.LoginAudit");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirectTo = "/login.ktrl?ssoError=invalid";
        try {
            String authToken = SamlAuthUtil.login(request);
            if (StringUtils.isNotEmpty(authToken)) {
                AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, authToken, null, true);
                switch (result.status()) {
                    case SUCCESS -> redirectTo = "/admin/menu.html";
                    case OTP_ENROLLMENT_REQUIRED -> redirectTo = "/admin/viewOTP.ktrl";
                    case NO_PROFILES -> redirectTo = "/login.ktrl?ssoError=noprofile";
                    case EXPIRED -> redirectTo = "/login.ktrl?ssoError=expired";
                    default -> redirectTo = "/login.ktrl?ssoError=invalid";
                }
                loginAuditLogger.info("SAML - " + result.user().getUsername() + " - " + result.status());
            } else {
                loginAuditLogger.info("SAML - Authentication Failed : Invalid assertion");
            }
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
        response.sendRedirect(request.getContextPath() + redirectTo);
    }
}
