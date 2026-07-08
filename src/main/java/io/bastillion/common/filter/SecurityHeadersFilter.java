/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Sets hardening response headers not already covered by the lmvc framework's own
 * {@code loophole.mvc.filter.SecurityFilter} (which already adds X-Content-Type-Options,
 * X-XSS-Protection, Strict-Transport-Security, and a minimal frame-ancestors-only CSP).
 * <p>
 * Deliberately does NOT re-set Strict-Transport-Security or X-Content-Type-Options here:
 * per RFC 6797 section 8.1, a UA that receives more than one Strict-Transport-Security header on a
 * response must ignore all of them, so duplicating it would silently disable HSTS entirely.
 * <p>
 * The Content-Security-Policy header IS repeated here (browsers enforce multiple CSP headers
 * as an intersection, which is spec-defined and safe) to add real restrictions beyond
 * frame-ancestors; the app's templates rely on inline &lt;script&gt;/&lt;style&gt; blocks
 * (Thymeleaf-injected CSRF tokens, per-user terminal theme colors), so script-src/style-src
 * allow 'unsafe-inline' rather than blocking them outright.
 */
public class SecurityHeadersFilter implements Filter {

    private static final String CSP =
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self'; " +
            "font-src 'self'; " +
            "connect-src 'self' wss:; " +
            "object-src 'none'; " +
            "base-uri 'self'; " +
            "form-action 'self'; " +
            "frame-ancestors 'self'";

    public void init(FilterConfig config) {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;

        response.addHeader("Content-Security-Policy", CSP);
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("Referrer-Policy", "same-origin");

        chain.doFilter(req, resp);
    }
}
