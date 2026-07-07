/**
 * Copyright (C) 2018 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package loophole.mvc.base;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import loophole.mvc.testctrl.DummyController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises {@link BaseKontroller#execute()} end to end against {@link DummyController},
 * which is scanned via the {@code MVC_CONTROLLER_PKGS} system property configured for
 * the surefire plugin in pom.xml.
 */
@ExtendWith(MockitoExtension.class)
class BaseKontrollerExecuteTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        DummyController.reset();
    }

    @Test
    void invokesControllerMethodMatchingPathAndHttpMethod() throws Exception {
        when(request.getRequestURI()).thenReturn("/app/hello.ktrl");
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());

        String forward = new BaseKontroller(request, response).execute();

        assertTrue(DummyController.helloExecuted);
        assertEquals("forward:/hello.html", forward);
    }

    @Test
    void doesNotInvokeControllerMethodWhenHttpMethodDoesNotMatch() throws Exception {
        when(request.getRequestURI()).thenReturn("/app/hello.ktrl");
        when(request.getMethod()).thenReturn("POST");

        String forward = new BaseKontroller(request, response).execute();

        assertFalse(DummyController.helloExecuted);
        assertEquals(null, forward);
    }

    @Test
    void populatesModelAnnotatedFieldsFromRequestParametersAndBack() throws Exception {
        when(request.getRequestURI()).thenReturn("/app/echo.ktrl");
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameterNames()).thenReturn(Collections.enumeration(java.util.List.of("name", "age")));
        when(request.getParameter("name")).thenReturn("world");
        when(request.getParameter("age")).thenReturn("42");

        String forward = new BaseKontroller(request, response).execute();

        assertTrue(DummyController.echoExecuted);
        assertEquals("forward:/echo.html", forward);
        verify(request).setAttribute("greeting", "hello world (42)");
    }

    @Test
    void skipsExecuteMethodAndReturnsValidationInputWhenValidateAddsFieldError() throws Exception {
        when(request.getRequestURI()).thenReturn("/app/validate.ktrl");
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());

        String forward = new BaseKontroller(request, response).execute();

        assertTrue(DummyController.validateSubmitExecuted);
        assertFalse(DummyController.submitExecuted);
        assertEquals("forward:/error.html", forward);

        ArgumentCaptor<Map> fieldErrorsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(request).setAttribute(eq("fieldErrors"), fieldErrorsCaptor.capture());
        assertEquals("name is required", fieldErrorsCaptor.getValue().get("name"));
    }
}
