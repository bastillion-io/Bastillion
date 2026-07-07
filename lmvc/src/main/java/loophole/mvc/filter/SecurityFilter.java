/**
 * Copyright (C) 2018 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package loophole.mvc.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter that prevents click jacking, enforces transport security, etc..
 */
@WebFilter(urlPatterns = {"/*"})
public class SecurityFilter implements Filter {

    // csrf parameter and session name
    public static final String _CSRF = "_csrf";

    // x-frame-options header
    private static final String CSP_HEADER = "Content-Security-Policy";
    private static final String CSP_VALUE = "frame-ancestors 'self';";

    // disable MIME sniffing
    private static final String X_CONTENT_TYPE_HEADER = "X-Content-Type-Options";
    private static final String X_CONTENT_TYPE_VALUE = "nosniff";

    // prevent cross-site scripting
    private static final String X_XSS_PROTECT_HEADER = "X-XSS-Protection";
    private static final String X_XSS_PROTECT_VALUE = "1; mode=block";

    // strict-transport-security header
    private static final String TRANSPORT_SECURITY_HEADER = "Strict-Transport-Security";
    private static final String TRANSPORT_SECURITY_VALUE = "max-age=31536000";

    public void init(FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // click jacking header
        httpServletResponse.addHeader(CSP_HEADER, CSP_VALUE);

        // disable MIME sniffing
        httpServletResponse.addHeader(X_CONTENT_TYPE_HEADER, X_CONTENT_TYPE_VALUE);

        // block cross-site scripting
        httpServletResponse.addHeader(X_XSS_PROTECT_HEADER, X_XSS_PROTECT_VALUE);

        // transport security header
        httpServletResponse.addHeader(TRANSPORT_SECURITY_HEADER, TRANSPORT_SECURITY_VALUE);

        filterChain.doFilter(request, response);

    }

    public void destroy() {
    }
}
