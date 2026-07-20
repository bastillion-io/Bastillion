/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.util;

import io.bastillion.manage.util.EncryptionUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Shared helpers for tests that simulate an HttpSession carrying AuthUtil-encrypted
 * attributes (auth token, timeout) - anywhere gating logic like AuthFilter or
 * SecureShellWS.onOpen() is exercised directly against a mocked session.
 */
public final class AuthTestSupport {

    private AuthTestSupport() {
    }

    /**
     * @return plaintext encrypted the same way AuthUtil stores session attributes, so a
     * mocked HttpSession.getAttribute(...) stub can hand it back to the real decrypt call
     */
    public static String encryptedAttribute(String plaintext) throws Exception {
        return EncryptionUtil.encrypt(plaintext);
    }

    /**
     * @return a timeout value in the exact format AuthUtil.setTimeout()/getTimeout() use,
     * offset from now by minutesFromNow (negative for an already-expired timeout)
     */
    public static String timeoutString(int minutesFromNow) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, minutesFromNow);
        return new SimpleDateFormat("MMddyyyyHHmmss").format(c.getTime());
    }
}
