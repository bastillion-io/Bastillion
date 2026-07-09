/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the pure key-encoding/validation logic behind Bastillion's two key-generation
 * paths: SSHUtil.keyGen (the application's own keypair, written to disk on first startup /
 * key rotation) and AuthKeysKtrl's per-user key generation, which calls
 * buildOpenSSHPrivateKey/encodeSSHPublicKey directly with a fresh java.security.KeyPair.
 * Getting either wrong produces a key that looks valid but nothing can actually
 * authenticate with.
 */
class SSHUtilTest {

    // --- keyGen: the application's own keypair, generated at first startup and on rotation ---

    @Test
    void keyGenWritesALoadableEd25519KeyPairToDisk() throws Exception {
        SSHUtil.keyGen("unused-for-ed25519");
        try {
            String privateKey = SSHUtil.getPrivateKey();
            String publicKey = SSHUtil.getPublicKey();

            assertTrue(publicKey.startsWith("ssh-ed25519 "));
            assertDoesNotThrow(() -> SSHUtil.validateKeyPair(privateKey, publicKey, ""));
            assertEquals("ED25519", SSHUtil.getKeyType(publicKey));
            assertNotNull(SSHUtil.getFingerprint(publicKey));
        } finally {
            SSHUtil.deleteGenSSHKeys();
        }
    }

    @Test
    void deleteGenSSHKeysRemovesGeneratedKeyFiles() throws Exception {
        SSHUtil.keyGen("unused-for-ed25519");
        SSHUtil.deleteGenSSHKeys();

        assertThrows(java.io.IOException.class, SSHUtil::getPrivateKey);
        assertThrows(java.io.IOException.class, SSHUtil::getPublicKey);
    }

    // --- Per-user key generation path (AuthKeysKtrl): raw java.security.KeyPair -> OpenSSH format ---

    @Test
    void buildOpenSSHPrivateKeyAndEncodeSSHPublicKeyProduceAMutuallyValidPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        java.security.KeyPair kp = kpg.generateKeyPair();

        String privatePem = SSHUtil.buildOpenSSHPrivateKey(kp, KeyPair.ED25519);

        byte[] encoded = SSHUtil.encodeSSHPublicKey("ssh-ed25519", kp.getPublic().getEncoded());
        String publicKey = "ssh-ed25519 " + Base64.getEncoder().encodeToString(encoded) + " test@bastillion";

        assertDoesNotThrow(() -> SSHUtil.validateKeyPair(privatePem, publicKey, ""));
        assertEquals("ED25519", SSHUtil.getKeyType(publicKey));
    }

    @Test
    void buildOpenSSHPrivateKeyWithUserIdAndBlankPassphraseSkipsEncryption() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        java.security.KeyPair kp = kpg.generateKeyPair();

        // Each call salts its own random checksum (see SSHUtil.buildOpenSSHPrivateKey), so
        // the two PEMs won't be byte-identical - what must hold is that a blank passphrase
        // via the (userId, KeyPair, type, passphrase) overload still produces an
        // *unencrypted* key, same as the direct (KeyPair, type) call.
        String viaOverload = SSHUtil.buildOpenSSHPrivateKey(42L, kp, KeyPair.ED25519, "");

        KeyPair loaded = KeyPair.load(new JSch(), viaOverload.getBytes(), null);
        assertFalse(loaded.isEncrypted());
        loaded.dispose();
    }

    // --- getKeyType / getFingerprint against keys generated straight through JSch ---

    @Test
    void getKeyTypeRecognizesRsaAndEcdsaKeys() throws Exception {
        assertEquals("RSA", SSHUtil.getKeyType(genJschPublicKey(KeyPair.RSA, 2048)));
        assertEquals("ECDSA", SSHUtil.getKeyType(genJschPublicKey(KeyPair.ECDSA, 256)));
    }

    @Test
    void getFingerprintIsStableForTheSameKeyAndDiffersAcrossKeys() throws Exception {
        String publicKey = genJschPublicKey(KeyPair.RSA, 2048);

        String fingerprintA = SSHUtil.getFingerprint(publicKey);
        String fingerprintB = SSHUtil.getFingerprint(publicKey);
        String otherKeyFingerprint = SSHUtil.getFingerprint(genJschPublicKey(KeyPair.RSA, 2048));

        assertNotNull(fingerprintA);
        assertEquals(fingerprintA, fingerprintB);
        assertNotEquals(fingerprintA, otherKeyFingerprint);
    }

    // --- validateKeyPair: guards the "paste your own application key" UI flow in Settings ---

    @Test
    void validateKeyPairRejectsMissingKeys() {
        JSchException ex = assertThrows(JSchException.class,
                () -> SSHUtil.validateKeyPair("", "", null));
        assertTrue(ex.getMessage().contains("required"));
    }

    @Test
    void validateKeyPairRejectsWrongPassphrase() throws Exception {
        JSch jsch = new JSch();
        KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA, 2048);

        ByteArrayOutputStream privOut = new ByteArrayOutputStream();
        keyPair.writePrivateKey(privOut, "correct-passphrase".getBytes());
        ByteArrayOutputStream pubOut = new ByteArrayOutputStream();
        keyPair.writePublicKey(pubOut, "test@bastillion");
        keyPair.dispose();

        String privateKey = privOut.toString();
        String publicKey = pubOut.toString();

        assertDoesNotThrow(() -> SSHUtil.validateKeyPair(privateKey, publicKey, "correct-passphrase"));
        assertThrows(JSchException.class,
                () -> SSHUtil.validateKeyPair(privateKey, publicKey, "wrong-passphrase"));
    }

    @Test
    void validateKeyPairRejectsGarbageInput() {
        assertThrows(JSchException.class,
                () -> SSHUtil.validateKeyPair("not a key", "also not a key", null));
    }

    private static String genJschPublicKey(int type, int length) throws Exception {
        JSch jsch = new JSch();
        KeyPair keyPair = KeyPair.genKeyPair(jsch, type, length);
        ByteArrayOutputStream pubOut = new ByteArrayOutputStream();
        keyPair.writePublicKey(pubOut, "test@bastillion");
        keyPair.dispose();
        return pubOut.toString();
    }
}
