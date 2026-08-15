/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.util;

import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * establishSession() is the single choke point LoginKtrl (local + LDAP) and SamlAcsServlet
 * (SAML) both funnel through after minting an auth token - these tests pin down every Status
 * branch, and specifically that otpAlreadySatisfied=true (the SAML case) skips the "supply a
 * matching code" check while OTP_ENROLLMENT_REQUIRED still fires identically either way.
 * otpEnabled here mirrors LoginKtrlTest's note: it's a private static field baked in at
 * class-load time from the bundled default (oneTimePassword=optional), so it's true for the
 * whole test JVM.
 */
@ExtendWith(MockitoExtension.class)
class AuthSessionUtilTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private static User managerWithProfile() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setUserType(Auth.MANAGER);
        user.setAuthType(Auth.AUTH_BASIC);
        return user;
    }

    private void stubSession() {
        lenient().when(request.getSession()).thenReturn(session);
    }

    @Test
    void unknownAuthTokenReturnsNotFoundWithoutTouchingSession() throws Exception {
        stubSession();
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(null);

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", null, false);

            assertEquals(AuthSessionUtil.Status.NOT_FOUND, result.status());
            assertNull(result.user());
            verify(session, never()).setAttribute(eq(AuthUtil.AUTH_TOKEN), any());
        }
    }

    @Test
    void wrongOtpTokenIsRejectedWhenNotAlreadySatisfied() throws Exception {
        stubSession();
        User user = managerWithProfile();
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(user);
            authDB.when(() -> AuthDB.getSharedSecret(1L)).thenReturn("JBSWY3DPEHPK3PXP");

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", 1L, false);

            assertEquals(AuthSessionUtil.Status.OTP_INVALID, result.status());
            verify(session, never()).setAttribute(eq(AuthUtil.AUTH_TOKEN), any());
        }
    }

    @Test
    void otpAlreadySatisfiedSkipsTheOtpCheckEvenWithNoTokenSupplied() throws Exception {
        stubSession();
        User user = managerWithProfile();
        user.setUiTheme("light");
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(user);
            //otpAlreadySatisfied=true - getSharedSecret must never even be consulted
            authDB.when(() -> AuthDB.getSharedSecret(any())).thenThrow(new AssertionError("should not be called"));

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", null, true);

            assertEquals(AuthSessionUtil.Status.SUCCESS, result.status());
            verify(session).setAttribute(eq(AuthUtil.AUTH_TOKEN), any());
            verify(response).addHeader(eq("Set-Cookie"), contains("bastillion_theme=light"));
        }
    }

    @Test
    void firstTimeEnrollmentStillFiresEvenWhenOtpAlreadySatisfied() throws Exception {
        // SAML's own trusted authentication doesn't excuse a brand-new user from ever setting
        // up a local OTP fallback credential - only the "supply a matching code" check is
        // bypassed for otpAlreadySatisfied=true, not the enrollment nudge itself.
        stubSession();
        User user = managerWithProfile();
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(user);

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", null, true);

            assertEquals(AuthSessionUtil.Status.SUCCESS, result.status());
            authDB.verify(() -> AuthDB.getSharedSecret(any()), never());
        }
    }

    @Test
    void firstLoginWithNoSharedSecretRequiresOtpEnrollmentWhenNotAlreadySatisfied() throws Exception {
        stubSession();
        User user = managerWithProfile();
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(user);
            authDB.when(() -> AuthDB.getSharedSecret(1L)).thenReturn(null);

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", null, false);

            assertEquals(AuthSessionUtil.Status.OTP_ENROLLMENT_REQUIRED, result.status());
            //enrollment is still a session-establishing outcome - attributes get set
            verify(session).setAttribute(eq(AuthUtil.AUTH_TOKEN), any());
        }
    }

    @Test
    void administratorWithNoProfilesIsRejected() throws Exception {
        stubSession();
        User user = new User();
        user.setId(2L);
        user.setUsername("bob");
        user.setUserType(Auth.ADMINISTRATOR);
        user.setProfileList(Collections.emptyList());
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(user);

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", null, true);

            assertEquals(AuthSessionUtil.Status.NO_PROFILES, result.status());
            verify(session, never()).setAttribute(eq(AuthUtil.AUTH_TOKEN), any());
        }
    }

    @Test
    void managerBypassesTheProfileCheck() throws Exception {
        stubSession();
        User user = managerWithProfile();
        user.setProfileList(Collections.emptyList());
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(user);

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", null, true);

            assertEquals(AuthSessionUtil.Status.SUCCESS, result.status());
        }
    }

    @Test
    void expiredAccountIsRejected() throws Exception {
        stubSession();
        User user = managerWithProfile();
        user.setExpired(true);
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(user);

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", null, true);

            assertEquals(AuthSessionUtil.Status.EXPIRED, result.status());
            verify(session, never()).setAttribute(eq(AuthUtil.AUTH_TOKEN), any());
        }
    }

    @Test
    void successResultCarriesTheUser() throws Exception {
        stubSession();
        User user = managerWithProfile();
        try (MockedStatic<AuthDB> authDB = mockStatic(AuthDB.class)) {
            authDB.when(() -> AuthDB.getUserByAuthToken("tok")).thenReturn(user);

            AuthSessionUtil.Result result = AuthSessionUtil.establishSession(request, response, "tok", null, true);

            assertEquals(user, result.user());
            authDB.verify(() -> AuthDB.updateLastLogin(user));
        }
    }
}
