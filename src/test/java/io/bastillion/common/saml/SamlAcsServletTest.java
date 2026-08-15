/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.saml;

import io.bastillion.common.util.AuthSessionUtil;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.SamlAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * doPost() is the one place that decides where a SAML login attempt ends up - these tests
 * pin down every AuthSessionUtil.Status branch's redirect target. It's package-private
 * (protected on HttpServlet), and this test lives in the same package deliberately so it can
 * call it directly rather than going through a full servlet-container request dispatch.
 */
@ExtendWith(MockitoExtension.class)
class SamlAcsServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private final SamlAcsServlet servlet = new SamlAcsServlet();

    private static User namedUser() {
        User user = new User();
        user.setUsername("alice");
        return user;
    }

    private void stubContextPath() {
        when(request.getContextPath()).thenReturn("");
    }

    @Test
    void invalidAssertionRedirectsToLoginWithInvalidSsoError() throws Exception {
        stubContextPath();
        try (MockedStatic<SamlAuthUtil> samlAuthUtil = mockStatic(SamlAuthUtil.class)) {
            samlAuthUtil.when(() -> SamlAuthUtil.login(request)).thenReturn(null);

            servlet.doPost(request, response);

            verify(response).sendRedirect("/login.ktrl?ssoError=invalid");
        }
    }

    @Test
    void successRedirectsToMenu() throws Exception {
        stubContextPath();
        try (MockedStatic<SamlAuthUtil> samlAuthUtil = mockStatic(SamlAuthUtil.class);
             MockedStatic<AuthSessionUtil> authSessionUtil = mockStatic(AuthSessionUtil.class)) {
            samlAuthUtil.when(() -> SamlAuthUtil.login(request)).thenReturn("tok-123");
            authSessionUtil.when(() -> AuthSessionUtil.establishSession(request, response, "tok-123", null, true))
                    .thenReturn(new AuthSessionUtil.Result(AuthSessionUtil.Status.SUCCESS, namedUser()));

            servlet.doPost(request, response);

            verify(response).sendRedirect("/admin/menu.html");
        }
    }

    @Test
    void firstTimeOtpEnrollmentRedirectsToOtpSetup() throws Exception {
        stubContextPath();
        try (MockedStatic<SamlAuthUtil> samlAuthUtil = mockStatic(SamlAuthUtil.class);
             MockedStatic<AuthSessionUtil> authSessionUtil = mockStatic(AuthSessionUtil.class)) {
            samlAuthUtil.when(() -> SamlAuthUtil.login(request)).thenReturn("tok-123");
            authSessionUtil.when(() -> AuthSessionUtil.establishSession(request, response, "tok-123", null, true))
                    .thenReturn(new AuthSessionUtil.Result(AuthSessionUtil.Status.OTP_ENROLLMENT_REQUIRED, namedUser()));

            servlet.doPost(request, response);

            verify(response).sendRedirect("/admin/viewOTP.ktrl");
        }
    }

    @Test
    void noProfilesRedirectsToLoginWithNoprofileSsoError() throws Exception {
        stubContextPath();
        try (MockedStatic<SamlAuthUtil> samlAuthUtil = mockStatic(SamlAuthUtil.class);
             MockedStatic<AuthSessionUtil> authSessionUtil = mockStatic(AuthSessionUtil.class)) {
            samlAuthUtil.when(() -> SamlAuthUtil.login(request)).thenReturn("tok-123");
            authSessionUtil.when(() -> AuthSessionUtil.establishSession(request, response, "tok-123", null, true))
                    .thenReturn(new AuthSessionUtil.Result(AuthSessionUtil.Status.NO_PROFILES, namedUser()));

            servlet.doPost(request, response);

            verify(response).sendRedirect("/login.ktrl?ssoError=noprofile");
        }
    }

    @Test
    void expiredAccountRedirectsToLoginWithExpiredSsoError() throws Exception {
        stubContextPath();
        try (MockedStatic<SamlAuthUtil> samlAuthUtil = mockStatic(SamlAuthUtil.class);
             MockedStatic<AuthSessionUtil> authSessionUtil = mockStatic(AuthSessionUtil.class)) {
            samlAuthUtil.when(() -> SamlAuthUtil.login(request)).thenReturn("tok-123");
            authSessionUtil.when(() -> AuthSessionUtil.establishSession(request, response, "tok-123", null, true))
                    .thenReturn(new AuthSessionUtil.Result(AuthSessionUtil.Status.EXPIRED, namedUser()));

            servlet.doPost(request, response);

            verify(response).sendRedirect("/login.ktrl?ssoError=expired");
        }
    }

    @Test
    void contextPathIsPrependedToEveryRedirect() throws Exception {
        when(request.getContextPath()).thenReturn("/bastillion");
        try (MockedStatic<SamlAuthUtil> samlAuthUtil = mockStatic(SamlAuthUtil.class)) {
            samlAuthUtil.when(() -> SamlAuthUtil.login(request)).thenReturn(null);

            servlet.doPost(request, response);

            verify(response).sendRedirect("/bastillion/login.ktrl?ssoError=invalid");
        }
    }
}
