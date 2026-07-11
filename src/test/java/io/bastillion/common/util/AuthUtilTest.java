/**
 * Copyright (C) 2013 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthUtil is the layer between the servlet session and everything AuthFilter/login trust -
 * sessionId, userId, and authToken are stored encrypted (not plaintext) specifically so a
 * session-fixation or session-dump attack can't just read/forge them; these tests pin that
 * behavior down.
 */
@ExtendWith(MockitoExtension.class)
class AuthUtilTest {

    @Mock
    private HttpSession session;

    @Test
    void sessionIdIsStoredEncryptedAndRoundTrips() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        AuthUtil.setSessionId(session, 42L);
        verify(session).setAttribute(eq(AuthUtil.SESSION_ID), captor.capture());
        assertNotEquals("42", captor.getValue());

        when(session.getAttribute(AuthUtil.SESSION_ID)).thenReturn(captor.getValue());
        assertEquals(42L, AuthUtil.getSessionId(session));
    }

    @Test
    void getSessionIdReturnsNullWhenNothingStored() throws Exception {
        assertNull(AuthUtil.getSessionId(session));
    }

    @Test
    void userIdIsStoredEncryptedAndRoundTrips() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        AuthUtil.setUserId(session, 7L);
        verify(session).setAttribute(eq(AuthUtil.USER_ID), captor.capture());
        assertNotEquals("7", captor.getValue());

        when(session.getAttribute(AuthUtil.USER_ID)).thenReturn(captor.getValue());
        assertEquals(7L, AuthUtil.getUserId(session));
    }

    @Test
    void authTokenIsStoredEncryptedAndRoundTrips() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        AuthUtil.setAuthToken(session, "secret-token");
        verify(session).setAttribute(eq(AuthUtil.AUTH_TOKEN), captor.capture());
        assertNotEquals("secret-token", captor.getValue());

        when(session.getAttribute(AuthUtil.AUTH_TOKEN)).thenReturn(captor.getValue());
        assertEquals("secret-token", AuthUtil.getAuthToken(session));
    }

    @Test
    void blankAuthTokenIsNotStored() throws Exception {
        AuthUtil.setAuthToken(session, "   ");
        verify(session, never()).setAttribute(eq(AuthUtil.AUTH_TOKEN), any());
    }

    @Test
    void otpSecretIsStoredEncryptedAndRoundTrips() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        AuthUtil.setOTPSecret(session, "JBSWY3DPEHPK3PXP");
        verify(session).setAttribute(eq("otp_secret"), captor.capture());
        assertNotEquals("JBSWY3DPEHPK3PXP", captor.getValue());

        when(session.getAttribute("otp_secret")).thenReturn(captor.getValue());
        assertEquals("JBSWY3DPEHPK3PXP", AuthUtil.getOTPSecret(session));
    }

    @Test
    void usernameAndUserTypeAndAuthTypeAreStoredAsIs() {
        AuthUtil.setUsername(session, "alice");
        verify(session).setAttribute(AuthUtil.USERNAME, "alice");

        AuthUtil.setUserType(session, "M");
        verify(session).setAttribute("userType", "M");

        AuthUtil.setAuthType(session, "BASIC");
        verify(session).setAttribute("authType", "BASIC");
    }

    @Test
    void deleteAllSessionClearsEveryTrackedAttributeAndInvalidates() {
        AuthUtil.deleteAllSession(session);

        verify(session).setAttribute(AuthUtil.TIMEOUT, null);
        verify(session).setAttribute(AuthUtil.AUTH_TOKEN, null);
        verify(session).setAttribute(AuthUtil.USER_ID, null);
        verify(session).setAttribute(AuthUtil.SESSION_ID, null);
        verify(session).invalidate();
    }

    @Test
    void setTimeoutStoresAFutureTimestampInTheExpectedFormat() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        AuthUtil.setTimeout(session);
        verify(session).setAttribute(eq(AuthUtil.TIMEOUT), captor.capture());

        Date timeout = new SimpleDateFormat("MMddyyyyHHmmss").parse(captor.getValue());
        assertTrue(timeout.after(new Date()));
    }

    @Test
    void getClientIPAddressFallsBackToRemoteAddrByDefault() {
        // No clientIPHeader configured in the bundled defaults, so this always goes through
        // the getRemoteAddr() fallback rather than trusting a spoofable header.
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("203.0.113.5");

        assertEquals("203.0.113.5", AuthUtil.getClientIPAddress(request));
    }
}
