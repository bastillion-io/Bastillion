/**
 * Copyright (C) 2013 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import com.jcraft.jsch.ChannelShell;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.model.SchSession;
import io.bastillion.manage.model.UserSchSessions;
import io.bastillion.manage.model.UserSettings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecureShellKtrlTest {

    @AfterEach
    void resetSessions() {
        SecureShellKtrl.setUserSchSessionMap(new ConcurrentHashMap<>());
    }

    @Test
    void setPtyTypeAppliesExactBrowserTerminalDimensions() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession httpSession = mock(HttpSession.class);
        when(request.getSession()).thenReturn(httpSession);

        ChannelShell channel = mock(ChannelShell.class);
        SchSession schSession = new SchSession();
        schSession.setChannel(channel);
        UserSchSessions sessions = new UserSchSessions();
        sessions.getSchSessionMap().put(7, schSession);
        SecureShellKtrl.getUserSchSessionMap().put(42L, sessions);

        UserSettings dimensions = new UserSettings();
        dimensions.setPtyColumns(132);
        dimensions.setPtyRows(41);
        dimensions.setPtyWidth(1056);
        dimensions.setPtyHeight(656);

        SecureShellKtrl controller = new SecureShellKtrl(request, response);
        controller.id = 7;
        controller.userSettings = dimensions;

        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            authUtil.when(() -> AuthUtil.getSessionId(httpSession)).thenReturn(42L);
            controller.setPtyType();
        }

        verify(channel).setPtySize(132, 41, 1056, 656);
    }
}
