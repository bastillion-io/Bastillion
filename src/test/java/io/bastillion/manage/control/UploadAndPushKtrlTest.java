/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.SystemStatusDB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;

import static io.bastillion.common.util.AuthTestSupport.encryptedAttribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * uploadFileName is bound directly from this request's own parameter (carried forward as a
 * hidden field between push() iterations) rather than re-derived server-side, and is used to
 * build both the local file path SFTPed to the remote system and the local file path deleted
 * once every system has been pushed to - a crafted "../../../etc/passwd"-shaped value must
 * never survive past entry into push(), or an already-authenticated admin could read or delete
 * an arbitrary local file instead of only ever touching UPLOAD_PATH.
 */
@ExtendWith(MockitoExtension.class)
class UploadAndPushKtrlTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private static void setField(UploadAndPushKtrl ktrl, String name, Object value) throws Exception {
        Field field = UploadAndPushKtrl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(ktrl, value);
    }

    private static Object getField(UploadAndPushKtrl ktrl, String name) throws Exception {
        Field field = UploadAndPushKtrl.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(ktrl);
    }

    @Test
    void pathTraversalInUploadFileNameIsStrippedToABareFilenameBeforeUse() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(AuthUtil.USER_ID)).thenReturn(encryptedAttribute("1"));

        UploadAndPushKtrl ktrl = new UploadAndPushKtrl(request, response);
        setField(ktrl, "uploadFileName", "../../../../etc/passwd");

        // No pending system - push() takes the "finished, clean up" branch, which is where
        // the vulnerable delete (and, in the pending-system branch above it, the vulnerable
        // SFTP source path) both read the same uploadFileName field.
        try (MockedStatic<SystemStatusDB> statusDB = mockStatic(SystemStatusDB.class)) {
            statusDB.when(() -> SystemStatusDB.getNextPendingSystem(1L)).thenReturn(null);
            statusDB.when(() -> SystemStatusDB.getAllSystemStatus(1L)).thenReturn(Collections.emptyList());

            ktrl.push();
        }

        assertEquals("passwd", getField(ktrl, "uploadFileName"));
    }
}
