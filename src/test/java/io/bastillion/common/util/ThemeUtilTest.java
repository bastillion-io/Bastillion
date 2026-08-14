package io.bastillion.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThemeUtilTest {

    @Test
    void defaultsUnknownValuesToDark() {
        assertEquals("dark", ThemeUtil.normalize(null));
        assertEquals("dark", ThemeUtil.normalize("system"));
        assertEquals("light", ThemeUtil.normalize("LIGHT"));
    }

    @Test
    void writesASecureAccountThemeCookieOnHttps() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("");
        when(request.isSecure()).thenReturn(true);

        ThemeUtil.setThemeCookie(request, response, "light");

        verify(response).addHeader("Set-Cookie",
                "bastillion_theme=light; Path=/; Max-Age=31536000; SameSite=Lax; Secure");
    }
}
