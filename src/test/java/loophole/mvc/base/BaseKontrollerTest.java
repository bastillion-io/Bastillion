/**
 * Copyright (C) 2018 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package loophole.mvc.base;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class BaseKontrollerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private BaseKontroller kontroller;

    @BeforeEach
    void setUp() {
        kontroller = new BaseKontroller(request, response);
    }

    @Test
    void newlyConstructedKontrollerHasNoErrors() {
        assertFalse(kontroller.hasErrors());
        assertTrue(kontroller.getErrors().isEmpty());
        assertTrue(kontroller.getFieldErrors().isEmpty());
    }

    @Test
    void addErrorAppendsToErrorsListAndSetsHasErrors() {
        kontroller.addError("something went wrong");

        assertTrue(kontroller.hasErrors());
        assertEquals(1, kontroller.getErrors().size());
        assertEquals("something went wrong", kontroller.getErrors().get(0));
    }

    @Test
    void addFieldErrorAppendsToFieldErrorsMapAndSetsHasErrors() {
        kontroller.addFieldError("email", "email is required");

        assertTrue(kontroller.hasErrors());
        assertEquals("email is required", kontroller.getFieldErrors().get("email"));
    }

    @Test
    void requestAndResponseAccessorsReturnConstructorValues() {
        assertSame(request, kontroller.getRequest());
        assertSame(response, kontroller.getResponse());
    }

    @Test
    void requestAndResponseAreMutable() {
        HttpServletRequest otherRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse otherResponse = org.mockito.Mockito.mock(HttpServletResponse.class);

        kontroller.setRequest(otherRequest);
        kontroller.setResponse(otherResponse);

        assertSame(otherRequest, kontroller.getRequest());
        assertSame(otherResponse, kontroller.getResponse());
    }
}
