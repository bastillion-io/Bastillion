/**
 * Copyright (C) 2018 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package loophole.mvc.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private ServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private final SecurityFilter filter = new SecurityFilter();

    @Test
    void addsSecurityHeadersAndContinuesChain() throws Exception {
        filter.doFilter(request, response, filterChain);

        verify(response).addHeader("Content-Security-Policy", "frame-ancestors 'self';");
        verify(response).addHeader("X-Content-Type-Options", "nosniff");
        verify(response).addHeader("X-XSS-Protection", "1; mode=block");
        verify(response).addHeader("Strict-Transport-Security", "max-age=31536000");
        verify(filterChain).doFilter(request, response);
    }
}
