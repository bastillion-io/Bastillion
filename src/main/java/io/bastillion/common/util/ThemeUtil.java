package io.bastillion.common.util;

import io.bastillion.manage.model.UserSettings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Applies the validated interface-theme preference for the browser. */
public final class ThemeUtil {

    public static final String COOKIE_NAME = "bastillion_theme";
    private static final int COOKIE_MAX_AGE = 365 * 24 * 60 * 60;

    private ThemeUtil() {
    }

    public static String normalize(String theme) {
        return UserSettings.LIGHT.equalsIgnoreCase(theme)
                ? UserSettings.LIGHT : UserSettings.DARK;
    }

    public static void setThemeCookie(HttpServletRequest request, HttpServletResponse response,
                                      String requestedTheme) {
        String contextPath = request.getContextPath();
        String path = contextPath == null || contextPath.isBlank() ? "/" : contextPath + "/";
        StringBuilder cookie = new StringBuilder(COOKIE_NAME)
                .append('=').append(normalize(requestedTheme))
                .append("; Path=").append(path)
                .append("; Max-Age=").append(COOKIE_MAX_AGE)
                .append("; SameSite=Lax");
        if (request.isSecure()) {
            cookie.append("; Secure");
        }
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
