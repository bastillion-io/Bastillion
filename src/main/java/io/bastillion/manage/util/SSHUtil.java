/**
 * Copyright (C) 2013 Loophole, LLC
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import com.jcraft.jsch.*;
import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.db.*;
import io.bastillion.manage.model.*;
import io.bastillion.manage.task.SecureShellTask;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;

/**
 * SSH utility class used to create public/private key for system and distribute authorized key files
 */
public class SSHUtil {

    public static final String PRIVATE_KEY = "privateKey";
    public static final String PUBLIC_KEY = "publicKey";
    private static final Logger log = LoggerFactory.getLogger(SSHUtil.class);
    public static final boolean keyManagementEnabled = "true".equals(AppConfig.getProperty("keyManagementEnabled"));

    public static final String KEY_PATH = AppConfig.CONFIG_DIR + "/keydb";
    public static final String KEY_TYPE = AppConfig.getProperty("sshKeyType");
    public static final int KEY_LENGTH = StringUtils.isNumeric(AppConfig.getProperty("sshKeyLength"))
            ? Integer.parseInt(AppConfig.getProperty("sshKeyLength")) : 4096;

    public static final String DEFAULT_USER_KEY_TYPE = AppConfig.getProperty("defaultUserKeyType", "ed25519");
    public static final boolean ALLOW_USER_KEY_TYPE_SELECTION =
            "true".equals(AppConfig.getProperty("allowUserKeyTypeSelection", "false"));

    public static final String PVT_KEY = KEY_PATH + "/id_" + KEY_TYPE;
    public static final String PUB_KEY = PVT_KEY + ".pub";

    public static final int SERVER_ALIVE_INTERVAL = StringUtils.isNumeric(AppConfig.getProperty("serverAliveInterval"))
            ? Integer.parseInt(AppConfig.getProperty("serverAliveInterval")) * 1000 : 60 * 1000;
    public static final int SESSION_TIMEOUT = 60000;
    public static final int CHANNEL_TIMEOUT = 60000;

    private SSHUtil() {}

    // --- Key Accessors ---

    public static String getPublicKey() throws IOException {
        String publicKey = PUB_KEY;
        if (StringUtils.isNotEmpty(AppConfig.getProperty(PRIVATE_KEY)) &&
                StringUtils.isNotEmpty(AppConfig.getProperty(PUBLIC_KEY))) {
            publicKey = AppConfig.getProperty(PUBLIC_KEY);
        }
        return FileUtils.readFileToString(new File(publicKey), StandardCharsets.UTF_8);
    }

    public static String getPrivateKey() throws IOException {
        String privateKey = PVT_KEY;
        if (StringUtils.isNotEmpty(AppConfig.getProperty(PRIVATE_KEY)) &&
                StringUtils.isNotEmpty(AppConfig.getProperty(PUBLIC_KEY))) {
            privateKey = AppConfig.getProperty(PRIVATE_KEY);
        }
        return FileUtils.readFileToString(new File(privateKey), StandardCharsets.UTF_8);
    }

    // --- Key Generation ---

    public static String keyGen() throws ConfigurationException, JSchException, IOException, GeneralSecurityException, InterruptedException {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("randomPassphrase", UUID.randomUUID().toString());
        String passphrase = AppConfig.getProperty("defaultSSHPassphrase", replaceMap);
        AppConfig.updateProperty("defaultSSHPassphrase", "${randomPassphrase}");
        return keyGen(passphrase);
    }

    public static void deleteGenSSHKeys() throws IOException {
        deletePvtGenSSHKey();
        File pub = new File(PUB_KEY);
        if (pub.exists()) FileUtils.forceDelete(pub);
    }

    public static void deletePvtGenSSHKey() throws IOException {
        File pvt = new File(PVT_KEY);
        if (pvt.exists()) FileUtils.forceDelete(pvt);
    }

    public static String keyGen(String passphrase) throws IOException, JSchException, InterruptedException, GeneralSecurityException {
        FileUtils.forceMkdir(new File(KEY_PATH));
        deleteGenSSHKeys();

        if (StringUtils.isEmpty(AppConfig.getProperty(PRIVATE_KEY)) ||
                StringUtils.isEmpty(AppConfig.getProperty(PUBLIC_KEY))) {

            Path tmpDir = Files.createTempDirectory("bastillion_keygen_" + KEY_TYPE + "_");
            Path tmpPvt = tmpDir.resolve("id_" + KEY_TYPE);
            Path tmpPub = tmpDir.resolve("id_" + KEY_TYPE + ".pub");

            int type = KeyPair.ED25519;
            if ("rsa".equalsIgnoreCase(KEY_TYPE)) type = KeyPair.RSA;
            else if ("dsa".equalsIgnoreCase(KEY_TYPE)) type = KeyPair.DSA;
            else if ("ecdsa".equalsIgnoreCase(KEY_TYPE)) type = KeyPair.ECDSA;
            else if ("ed448".equalsIgnoreCase(KEY_TYPE)) type = KeyPair.ED448;

            String comment = "bastillion@global_key";
            JSch jsch = new JSch();
            KeyPair keyPair = KeyPair.genKeyPair(jsch, type, KEY_LENGTH);

            if (type == KeyPair.RSA || type == KeyPair.DSA || type == KeyPair.ECDSA) {
                keyPair.writePublicKey(tmpPub.toString(), comment);
                keyPair.writePrivateKey(tmpPvt.toString(),
                        StringUtils.isNotBlank(passphrase) ? passphrase.getBytes(StandardCharsets.UTF_8) : null);
                log.info("Generated {} keypair with passphrase", KEY_TYPE);
            } else {
                log.info("Generating unencrypted OpenSSH-format {} keypair", KEY_TYPE);
                java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance(
                        KEY_TYPE.equalsIgnoreCase("ed448") ? "Ed448" : "Ed25519");
                java.security.KeyPair newPair = kpg.generateKeyPair();

                String opensshPEM = buildOpenSSHPrivateKey(newPair, type);
                Files.writeString(tmpPvt, opensshPEM, StandardCharsets.US_ASCII);

                String publicKeyContent = generateOpenSSHPublicKey(newPair, comment, type);
                Files.writeString(tmpPub, publicKeyContent, StandardCharsets.UTF_8);

                passphrase = "";
                log.info("Generated {} keypair without passphrase", KEY_TYPE);
            }

            Files.move(tmpPvt, Path.of(PVT_KEY), StandardCopyOption.REPLACE_EXISTING);
            Files.move(tmpPub, Path.of(PUB_KEY), StandardCopyOption.REPLACE_EXISTING);
            try { Files.deleteIfExists(tmpDir); } catch (Exception ignored) {}

            setFilePerms(PVT_KEY);
            setFilePerms(PUB_KEY);

            log.info("Generated {} SSH key — fingerprint: {}", KEY_TYPE, keyPair.getFingerPrint());
            keyPair.dispose();
        }
        return passphrase;
    }

    private static void setFilePerms(String path) {
        try {
            File f = new File(path);
            f.setReadable(false, false);
            f.setWritable(false, false);
            f.setExecutable(false, false);
            f.setReadable(true, true);
            f.setWritable(true, true);
        } catch (Exception ignored) {}
    }

    // --- Legacy-compatible methods restored ---

    public static byte[] encodeSSHPublicKey(String keyType, byte[] rawPubBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeSSHString(out, keyType.getBytes(StandardCharsets.UTF_8));
        byte[] keyPart = extractRawKeyFromX509(rawPubBytes);
        writeSSHBytes(out, keyPart);
        return out.toByteArray();
    }

    public static String buildOpenSSHPrivateKey(Long userId, java.security.KeyPair kp, int type, String passphrase)
            throws IOException, GeneralSecurityException, InterruptedException {
        String unencryptedPEM = buildOpenSSHPrivateKey(kp, type);
        if (StringUtils.isBlank(passphrase)) return unencryptedPEM;
        return rewrapWithOpenSSHKeygen(userId != null ? userId.toString() : UUID.randomUUID().toString(),
                unencryptedPEM, passphrase);
    }

    // ---- SSH key distribution methods ----

    public static HostSystem addPubKey(HostSystem hostSystem, Session session, String appPublicKey) {
        try {
            String authorizedKeys = hostSystem.getAuthorizedKeys().replaceAll("~\\/|~", "");
            ChannelExec exec = (ChannelExec) session.openChannel("exec");
            exec.setCommand("cat " + authorizedKeys);
            exec.setErrStream(System.err);
            exec.setInputStream(null);
            InputStream in = exec.getInputStream();
            exec.connect(CHANNEL_TIMEOUT);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder existingKeysBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                existingKeysBuilder.append(line).append("\n");
            reader.close();
            exec.disconnect();

            String existingKeys = existingKeysBuilder.toString();
            String appPubKey = appPublicKey.replace("\n", "").trim();

            String newKeys;
            if (keyManagementEnabled) {
                List<String> assigned = PublicKeyDB.getPublicKeysForSystem(hostSystem.getId());
                StringBuilder sb = new StringBuilder();
                for (String k : assigned) sb.append(k.replace("\n", "").trim()).append("\n");
                sb.append(appPubKey);
                newKeys = sb.toString();
            } else {
                if (!existingKeys.contains(appPubKey))
                    newKeys = existingKeys + "\n" + appPubKey;
                else newKeys = existingKeys;
            }

            if (!newKeys.equals(existingKeys)) {
                ChannelExec upd = (ChannelExec) session.openChannel("exec");
                upd.setCommand("echo '" + newKeys + "' > " + authorizedKeys + "; chmod 600 " + authorizedKeys);
                upd.connect(CHANNEL_TIMEOUT);
                upd.disconnect();
            }
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        return hostSystem;
    }

    public static HostSystem pushUpload(HostSystem hostSystem, Session session, String source, String destination) {
        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
        Channel channel = null;
        ChannelSftp c = null;
        try (FileInputStream file = new FileInputStream(source)) {
            channel = session.openChannel("sftp");
            channel.connect(CHANNEL_TIMEOUT);
            c = (ChannelSftp) channel;
            destination = destination.replaceAll("~\\/|~", "");
            c.put(file, destination);
        } catch (Exception ex) {
            log.info(ex.toString(), ex);
            hostSystem.setErrorMsg(ex.getMessage());
            hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
        }
        if (c != null) c.exit();
        if (channel != null) channel.disconnect();
        return hostSystem;
    }

    public static HostSystem openSSHTermOnSystem(String passphrase, String password, Long userId, Long sessionId,
                                                 HostSystem hostSystem, Map<Long, UserSchSessions> userSessionMap)
            throws SQLException, GeneralSecurityException {

        JSch jsch = new JSch();
        int instanceId = getNextInstanceId(sessionId, userSessionMap);
        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
        hostSystem.setInstanceId(instanceId);
        SchSession schSession = null;

        try {
            ApplicationKey appKey = PrivateKeyDB.getApplicationKey();
            if (StringUtils.isBlank(passphrase)) passphrase = appKey.getPassphrase();
            if (passphrase == null) passphrase = "";

            jsch.addIdentity(appKey.getId().toString(),
                    appKey.getPrivateKey().trim().getBytes(),
                    appKey.getPublicKey().getBytes(),
                    passphrase.getBytes());

            Session session = jsch.getSession(hostSystem.getUser(), hostSystem.getHost(), hostSystem.getPort());
            if (StringUtils.isNotBlank(password)) session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.setServerAliveInterval(SERVER_ALIVE_INTERVAL);
            session.connect(SESSION_TIMEOUT);

            ChannelShell channel = (ChannelShell) session.openChannel("shell");
            channel.setPtyType("xterm");
            InputStream outFromChannel = channel.getInputStream();
            SessionOutput sessionOutput = new SessionOutput(sessionId, hostSystem);
            new Thread(new SecureShellTask(sessionOutput, outFromChannel)).start();

            OutputStream inputToChannel = channel.getOutputStream();
            PrintStream commander = new PrintStream(inputToChannel, true);
            channel.connect();

            schSession = new SchSession();
            schSession.setUserId(userId);
            schSession.setSession(session);
            schSession.setChannel(channel);
            schSession.setCommander(commander);
            schSession.setInputToChannel(inputToChannel);
            schSession.setOutFromChannel(outFromChannel);
            schSession.setHostSystem(hostSystem);

            addPubKey(hostSystem, session, appKey.getPublicKey());
        } catch (Exception ex) {
            log.info(ex.toString(), ex);
            hostSystem.setErrorMsg(ex.getMessage());
            hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
        }

        if (hostSystem.getStatusCd().equals(HostSystem.SUCCESS_STATUS)) {
            UserSchSessions userSchSessions = userSessionMap.getOrDefault(sessionId, new UserSchSessions());
            userSchSessions.getSchSessionMap().put(instanceId, schSession);
            userSessionMap.put(sessionId, userSchSessions);
        }
        SystemStatusDB.updateSystemStatus(hostSystem, userId);
        SystemDB.updateSystem(hostSystem);
        return hostSystem;
    }

    private static int getNextInstanceId(Long sessionId, Map<Long, UserSchSessions> map) {
        int instanceId = 1;
        if (map.get(sessionId) != null) {
            for (Integer id : map.get(sessionId).getSchSessionMap().keySet()) {
                if (!id.equals(instanceId) && map.get(sessionId).getSchSessionMap().get(instanceId) == null)
                    return instanceId;
                instanceId++;
            }
        }
        return instanceId;
    }

    // --- Authentication and Add Key ---
    public static HostSystem authAndAddPubKey(HostSystem hostSystem, String passphrase, String password) {
        JSch jsch = new JSch();
        Session session = null;
        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
        try {
            ApplicationKey appKey = PrivateKeyDB.getApplicationKey();
            if (StringUtils.isBlank(passphrase)) passphrase = appKey.getPassphrase();
            if (passphrase == null) passphrase = "";

            jsch.addIdentity(appKey.getId().toString(),
                    appKey.getPrivateKey().trim().getBytes(),
                    appKey.getPublicKey().getBytes(),
                    passphrase.getBytes());

            session = jsch.getSession(hostSystem.getUser(), hostSystem.getHost(), hostSystem.getPort());
            if (password != null && !password.isEmpty()) session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.setServerAliveInterval(SERVER_ALIVE_INTERVAL);
            session.connect(SESSION_TIMEOUT);

            addPubKey(hostSystem, session, appKey.getPublicKey());

        } catch (Exception ex) {
            log.info(ex.toString(), ex);
            hostSystem.setErrorMsg(ex.getMessage());
            String msg = ex.getMessage().toLowerCase();
            if (msg.contains("userauth fail")) hostSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            else if (msg.contains("auth fail") || msg.contains("auth cancel"))
                hostSystem.setStatusCd(HostSystem.AUTH_FAIL_STATUS);
            else if (msg.contains("unknownhostexception")) {
                hostSystem.setErrorMsg("DNS Lookup Failed");
                hostSystem.setStatusCd(HostSystem.HOST_FAIL_STATUS);
            } else hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
        }
        if (session != null) session.disconnect();
        return hostSystem;
    }

    // --- Fingerprint helper ---
    public static String getFingerprint(String publicKey) {
        String fingerprint = null;
        if (StringUtils.isNotEmpty(publicKey)) {
            if (publicKey.contains("ssh-")) {
                publicKey = publicKey.substring(publicKey.indexOf("ssh-"));
            } else if (publicKey.contains("ecdsa-")) {
                publicKey = publicKey.substring(publicKey.indexOf("ecdsa-"));
            }
            try {
                KeyPair keyPair = KeyPair.load(new JSch(), null, publicKey.getBytes());
                if (keyPair != null) {
                    fingerprint = keyPair.getFingerPrint();
                }
            } catch (JSchException ex) {
                log.error(ex.toString(), ex);
            }
        }
        return fingerprint;
    }

    // --- Distribution methods ---

    public static void distributePubKeysToAllSystems() throws SQLException, GeneralSecurityException {
        if (keyManagementEnabled) {
            for (HostSystem s : SystemDB.getAllSystems()) {
                s = SSHUtil.authAndAddPubKey(s, null, null);
                SystemDB.updateSystem(s);
            }
        }
    }
    /**
     * Returns public key type from an OpenSSH public key string.
     * Recognizes DSA, RSA, ECDSA, ED25519, ED448.
     */
    public static String getKeyType(String publicKey) {
        String keyType = null;
        if (StringUtils.isNotEmpty(publicKey)) {
            // Normalize to start at the algorithm token
            if (publicKey.contains("ssh-")) {
                publicKey = publicKey.substring(publicKey.indexOf("ssh-"));
            } else if (publicKey.contains("ecdsa-")) {
                publicKey = publicKey.substring(publicKey.indexOf("ecdsa-"));
            }
            try {
                KeyPair keyPair = KeyPair.load(new JSch(), null, publicKey.getBytes(StandardCharsets.UTF_8));
                if (keyPair != null) {
                    int type = keyPair.getKeyType();
                    if (KeyPair.DSA == type)        keyType = "DSA";
                    else if (KeyPair.RSA == type)    keyType = "RSA";
                    else if (KeyPair.ECDSA == type)  keyType = "ECDSA";
                    else if (KeyPair.ED25519 == type)keyType = "ED25519";
                    else if (KeyPair.ED448 == type)  keyType = "ED448";
                    else if (KeyPair.UNKNOWN == type)keyType = "UNKNOWN";
                    else if (KeyPair.ERROR == type)  keyType = "ERROR";
                }
            } catch (JSchException ex) {
                log.error(ex.toString(), ex);
            }
        }
        return keyType;
    }


    public static void distributePubKeysToProfile(Long profileId) throws SQLException, GeneralSecurityException {
        if (keyManagementEnabled) {
            for (HostSystem s : ProfileSystemsDB.getSystemsByProfile(profileId)) {
                s = SSHUtil.authAndAddPubKey(s, null, null);
                SystemDB.updateSystem(s);
            }
        }
    }

    public static void distributePubKeysToUser(Long userId) throws SQLException, GeneralSecurityException {
        if (keyManagementEnabled) {
            for (Profile profile : UserProfileDB.getProfilesByUser(userId)) {
                for (HostSystem s : ProfileSystemsDB.getSystemsByProfile(profile.getId())) {
                    s = SSHUtil.authAndAddPubKey(s, null, null);
                    SystemDB.updateSystem(s);
                }
            }
        }
    }

    // --- Encoding Helpers ---

    public static String buildOpenSSHPrivateKey(java.security.KeyPair kp, int type) throws IOException {
        String keyType = (type == KeyPair.ED25519) ? "ssh-ed25519" : "ssh-ed448";
        ByteArrayOutputStream outer = new ByteArrayOutputStream();
        outer.write("openssh-key-v1\0".getBytes(StandardCharsets.US_ASCII));
        writeSSHString(outer, "none".getBytes());
        writeSSHString(outer, "none".getBytes());
        writeSSHString(outer, new byte[0]);
        outer.write(ByteBuffer.allocate(4).putInt(1).array());

        ByteArrayOutputStream pubBlob = new ByteArrayOutputStream();
        writeSSHString(pubBlob, keyType.getBytes());
        byte[] pubRaw = extractRawKeyFromX509(kp.getPublic().getEncoded());
        writeSSHBytes(pubBlob, pubRaw);
        writeSSHBytes(outer, pubBlob.toByteArray());

        ByteArrayOutputStream privBlob = new ByteArrayOutputStream();
        int chk = new SecureRandom().nextInt();
        privBlob.write(ByteBuffer.allocate(4).putInt(chk).array());
        privBlob.write(ByteBuffer.allocate(4).putInt(chk).array());
        writeSSHString(privBlob, keyType.getBytes());
        writeSSHBytes(privBlob, pubRaw);

        byte[] privRaw = extractRawKeyFromX509(kp.getPrivate().getEncoded());
        if (keyType.equals("ssh-ed25519") && privRaw.length > 32)
            privRaw = Arrays.copyOfRange(privRaw, privRaw.length - 32, privRaw.length);
        ByteArrayOutputStream combo = new ByteArrayOutputStream();
        combo.write(privRaw);
        combo.write(pubRaw);
        writeSSHBytes(privBlob, combo.toByteArray());
        writeSSHString(privBlob, "".getBytes());
        int padLen = 8 - (privBlob.size() % 8);
        for (int i = 1; i <= padLen; i++) privBlob.write(i);
        writeSSHBytes(outer, privBlob.toByteArray());
        String base64 = Base64.getMimeEncoder(70, "\n".getBytes())
                .encodeToString(outer.toByteArray());
        return "-----BEGIN OPENSSH PRIVATE KEY-----\n" + base64 +
                "\n-----END OPENSSH PRIVATE KEY-----\n";
    }

    private static String generateOpenSSHPublicKey(java.security.KeyPair kp, String comment, int type) throws IOException {
        String algo = (type == KeyPair.ED25519) ? "ssh-ed25519" :
                (type == KeyPair.ED448) ? "ssh-ed448" : "ssh-unknown";
        byte[] rawPub = extractRawKeyFromX509(kp.getPublic().getEncoded());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeSSHString(out, algo.getBytes());
        writeSSHBytes(out, rawPub);
        String encoded = Base64.getEncoder().encodeToString(out.toByteArray());
        return algo + " " + encoded + " " + comment + "\n";
    }

    public static void writeSSHString(ByteArrayOutputStream out, byte[] data) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(data.length).array());
        out.write(data);
    }

    public static void writeSSHBytes(ByteArrayOutputStream out, byte[] bytes) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
        out.write(bytes);
    }

    public static byte[] extractRawKeyFromX509(byte[] x509Encoded) {
        for (int i = 0; i < x509Encoded.length - 3; i++) {
            if (x509Encoded[i] == 0x03 && x509Encoded[i + 2] == 0x00)
                return Arrays.copyOfRange(x509Encoded, i + 3, x509Encoded.length);
        }
        return x509Encoded;
    }

    public static String rewrapWithOpenSSHKeygen(String userId, String pem, String passphrase)
            throws IOException, InterruptedException, GeneralSecurityException {
        Path tmp = Files.createTempFile("bastillion_key_" + userId + "_", ".key");
        Files.write(tmp, pem.getBytes(StandardCharsets.US_ASCII));
        try {
            try {
                Files.setPosixFilePermissions(tmp, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            } catch (UnsupportedOperationException ignored) {}
            ProcessBuilder pb = new ProcessBuilder("ssh-keygen", "-p", "-P", "",
                    "-N", passphrase, "-f", tmp.toString(), "-o", "-a", "16");
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            proc.waitFor();
            return Files.readString(tmp, StandardCharsets.US_ASCII);
        } finally {
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
        }
    }
}
