/**
 * Copyright (C) 2018 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package loophole.mvc.filter;

import loophole.mvc.base.DispatcherServlet;
import loophole.mvc.base.TemplateServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Filter to check for a CSRF token and protect application from cross-site request forgery.
 */
@WebFilter(urlPatterns = {"/", "*" + DispatcherServlet.CTR_EXT, "*" + TemplateServlet.VIEW_EXT})
public class CSRFFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(CSRFFilter.class);

    private static final SecureRandom random = new SecureRandom();

    public void init(FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // csrf check
        String _csrf = (String) httpServletRequest.getSession().getAttribute(SecurityFilter._CSRF);
        if (_csrf == null || constantTimeEquals(_csrf, request.getParameter(SecurityFilter._CSRF))) {
            log.debug("CSRF token is valid for " + httpServletRequest.getRequestURL());
            if (_csrf == null || httpServletRequest.getMethod().equalsIgnoreCase("POST")) {
                _csrf = (new BigInteger(165, random)).toString(36).toUpperCase();
                httpServletRequest.getSession().setAttribute(SecurityFilter._CSRF, _csrf);
            }
            filterChain.doFilter(request, response);
            return;
        }
        log.debug("CSRF token is invalid for " + httpServletRequest.getRequestURL());
        httpServletRequest.getSession().invalidate();
        log.debug("Session invalidated");
        httpServletResponse.sendRedirect(httpServletRequest.getContextPath());
    }

    public void destroy() {
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
