/**
 * Copyright (C) 2013 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptionUtilTest {

    @Test
    void verifyHashAcceptsCurrentPbkdf2Format() throws Exception {
        String password = "correct horse battery staple";
        String salt = EncryptionUtil.generateSalt();
        String stored = EncryptionUtil.hashV2(password, salt);

        assertTrue(EncryptionUtil.verifyHash(password, salt, stored));
        assertFalse(EncryptionUtil.verifyHash("wrong password", salt, stored));
    }

    @Test
    void verifyHashAcceptsBastillionV5LegacyFormat() throws Exception {
        // v5's own pre-PBKDF2 hash: salt and password digested as two separate updates.
        String password = "correct horse battery staple";
        String salt = EncryptionUtil.generateSalt();
        String stored = EncryptionUtil.hash(password, salt);

        assertTrue(EncryptionUtil.verifyHash(password, salt, stored));
        assertFalse(EncryptionUtil.verifyHash("wrong password", salt, stored));
    }

    @Test
    void verifyHashAcceptsRealBastillionV4Format() throws Exception {
        // Real Bastillion v4 (AuthDB/UserDB/DBInitServlet) computes
        // EncryptionUtil.hash(password + salt) - the single-arg overload, with salt
        // concatenated onto the password as a plain string rather than digested separately.
        // Confirmed against v4.0.2 source and a decompiled v4 install's AuthDB.class - see
        // tools/migrate/README.md. Migrated accounts must still be able to log in with this
        // exact format, without any re-hash step at migration time.
        String password = "correct horse battery staple";
        String salt = EncryptionUtil.generateSalt();
        String stored = EncryptionUtil.hash(password + salt);

        assertTrue(EncryptionUtil.verifyHash(password, salt, stored));
        assertFalse(EncryptionUtil.verifyHash("wrong password", salt, stored));
    }

    @Test
    void verifyHashRejectsEmptyInputs() throws Exception {
        String salt = EncryptionUtil.generateSalt();
        String stored = EncryptionUtil.hash("somepassword", salt);

        assertFalse(EncryptionUtil.verifyHash("", salt, stored));
        assertFalse(EncryptionUtil.verifyHash("somepassword", salt, ""));
        assertFalse(EncryptionUtil.verifyHash(null, salt, stored));
        assertFalse(EncryptionUtil.verifyHash("somepassword", salt, null));
    }
}
