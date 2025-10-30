/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
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

    //system path to public/private key
    public static final String KEY_PATH = AppConfig.CONFIG_DIR + "/keydb";

    //key type - rsa, ecdsa, ed25519, or dsa (deprecated)
    public static final String KEY_TYPE = AppConfig.getProperty("sshKeyType");
    public static final int KEY_LENGTH = StringUtils.isNumeric(AppConfig.getProperty("sshKeyLength")) ? Integer.parseInt(AppConfig.getProperty("sshKeyLength")) : 4096;
    
    //default key type for user-generated keys
    public static final String DEFAULT_USER_KEY_TYPE = AppConfig.getProperty("defaultUserKeyType", "rsa");
    //whether users can select their key type
    public static final boolean ALLOW_USER_KEY_TYPE_SELECTION = "true".equals(AppConfig.getProperty("allowUserKeyTypeSelection", "false"));

    //private key name
    public static final String PVT_KEY = KEY_PATH + "/id_" + KEY_TYPE;
    //public key name
    public static final String PUB_KEY = PVT_KEY + ".pub";


    public static final int SERVER_ALIVE_INTERVAL = StringUtils.isNumeric(AppConfig.getProperty("serverAliveInterval")) ? Integer.parseInt(AppConfig.getProperty("serverAliveInterval")) * 1000 : 60 * 1000;
    public static final int SESSION_TIMEOUT = 60000;
    public static final int CHANNEL_TIMEOUT = 60000;

    private SSHUtil() {
    }

    /**
     * returns the system's public key
     *
     * @return system's public key
     */
    public static String getPublicKey() throws IOException {

        String publicKey = PUB_KEY;
        //check to see if pub/pvt are defined in properties
        if (StringUtils.isNotEmpty(AppConfig.getProperty(PRIVATE_KEY)) && StringUtils.isNotEmpty(AppConfig.getProperty(PUBLIC_KEY))) {
            publicKey = AppConfig.getProperty(PUBLIC_KEY);
        }
        //read pvt ssh key
        File file = new File(publicKey);
        publicKey = FileUtils.readFileToString(file, "UTF-8");

        return publicKey;
    }


    /**
     * returns the system's public key
     *
     * @return system's public key
     */
    public static String getPrivateKey() throws IOException {

        String privateKey = PVT_KEY;
        //check to see if pub/pvt are defined in properties
        if (StringUtils.isNotEmpty(AppConfig.getProperty(PRIVATE_KEY)) && StringUtils.isNotEmpty(AppConfig.getProperty(PUBLIC_KEY))) {
            privateKey = AppConfig.getProperty(PRIVATE_KEY);
        }

        //read pvt ssh key
        File file = new File(privateKey);
        privateKey = FileUtils.readFileToString(file, "UTF-8");


        return privateKey;
    }

    /**
     * generates system's public/private key par and returns passphrase
     *
     * @return passphrase for system generated key
     */
    public static String keyGen() throws ConfigurationException, JSchException, IOException {

        //get passphrase cmd from properties file
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("randomPassphrase", UUID.randomUUID().toString());

        String passphrase = AppConfig.getProperty("defaultSSHPassphrase", replaceMap);

        AppConfig.updateProperty("defaultSSHPassphrase", "${randomPassphrase}");

        return keyGen(passphrase);

    }

    /**
     * delete SSH keys
     */
    public static void deleteGenSSHKeys() throws IOException {

        deletePvtGenSSHKey();
        //delete public key
        File file = new File(PUB_KEY);
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }

    }


    /**
     * delete SSH keys
     */
    public static void deletePvtGenSSHKey() throws IOException {

        //delete private key
        File file = new File(PVT_KEY);
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }

    }

    /**
     * Generates the system's public/private key pair and returns the passphrase.
     * Works around mwiede JSch's unsupported Ed25519 private key serialization.
     * @return passphrase for system generated key
     */
    public static String keyGen(String passphrase) throws IOException, JSchException {

        FileUtils.forceMkdir(new File(KEY_PATH));
        deleteGenSSHKeys();

        if (StringUtils.isEmpty(AppConfig.getProperty(PRIVATE_KEY)) || StringUtils.isEmpty(AppConfig.getProperty(PUBLIC_KEY))) {

            // Determine SSH key type
            int type = KeyPair.ED25519;
            if ("rsa".equalsIgnoreCase(KEY_TYPE)) {
                type = KeyPair.RSA;
            } else if ("dsa".equalsIgnoreCase(KEY_TYPE)) {
                type = KeyPair.DSA;
            } else if ("ecdsa".equalsIgnoreCase(KEY_TYPE)) {
                type = KeyPair.ECDSA;
            } else if ("ed448".equalsIgnoreCase(KEY_TYPE)) {
                type = KeyPair.ED448;
            }

            String comment = "bastillion@global_key";
            JSch jsch = new JSch();
            KeyPair keyPair = KeyPair.genKeyPair(jsch, type, KEY_LENGTH);

            // --- Write public key
            keyPair.writePublicKey(PUB_KEY, comment);

            try {
                // Try the standard PEM private key first
                keyPair.writePrivateKey(PVT_KEY, passphrase != null ? passphrase.getBytes() : null);
            } catch (UnsupportedOperationException ex) {
                // Ed25519/Ed448 fall back to OpenSSH v1 format (supported public API)
                log.warn("Falling back to OpenSSH v1 key serialization for type: {}", KEY_TYPE);

                try (FileOutputStream fos = new FileOutputStream(PVT_KEY)) {
                    keyPair.writeOpenSSHv1PrivateKey(fos, passphrase != null ? passphrase.getBytes() : null);
                }
            }

            // Set restrictive file permissions (600)
            File pvt = new File(PVT_KEY);
            pvt.setReadable(false, false);
            pvt.setWritable(false, false);
            pvt.setExecutable(false, false);
            pvt.setReadable(true, true);
            pvt.setWritable(true, true);

            File pub = new File(PUB_KEY);
            pub.setReadable(false, false);
            pub.setWritable(false, false);
            pub.setExecutable(false, false);
            pub.setReadable(true, true);
            pub.setWritable(true, true);

            log.info("Generated {} SSH key — fingerprint: {}", KEY_TYPE, keyPair.getFingerPrint());
            keyPair.dispose();
        }

        return passphrase;
    }

    /**
     * distributes authorized keys for host system
     *
     * @param hostSystem      object contains host system information
     * @param passphrase      ssh key passphrase
     * @param password        password to host system if needed
     * @return status of key distribution
     */
    public static HostSystem authAndAddPubKey(HostSystem hostSystem, String passphrase, String password) {


        JSch jsch = new JSch();
        Session session = null;
        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
        try {
            ApplicationKey appKey = PrivateKeyDB.getApplicationKey();
            //check to see if passphrase has been provided
            if (passphrase == null || passphrase.trim().equals("")) {
                passphrase = appKey.getPassphrase();
                //check for null inorder to use key without passphrase
                if (passphrase == null) {
                    passphrase = "";
                }
            }
            //add private key
            jsch.addIdentity(appKey.getId().toString(), appKey.getPrivateKey().trim().getBytes(), appKey.getPublicKey().getBytes(), passphrase.getBytes());

            //create session
            session = jsch.getSession(hostSystem.getUser(), hostSystem.getHost(), hostSystem.getPort());

            //set password if passed in
            if (password != null && !password.equals("")) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.setServerAliveInterval(SERVER_ALIVE_INTERVAL);
            session.connect(SESSION_TIMEOUT);


            addPubKey(hostSystem, session, appKey.getPublicKey());

        } catch (JSchException | SQLException | GeneralSecurityException ex) {
            log.info(ex.toString(), ex);
            hostSystem.setErrorMsg(ex.getMessage());
            if (ex.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            } else if (ex.getMessage().toLowerCase().contains("auth fail") || ex.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystem.setStatusCd(HostSystem.AUTH_FAIL_STATUS);
            } else if (ex.getMessage().toLowerCase().contains("unknownhostexception")) {
                hostSystem.setErrorMsg("DNS Lookup Failed");
                hostSystem.setStatusCd(HostSystem.HOST_FAIL_STATUS);
            } else {
                hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
            }


        }

        if (session != null) {
            session.disconnect();
        }

        return hostSystem;


    }


    /**
     * distributes uploaded item to system defined
     *
     * @param hostSystem  object contains host system information
     * @param session     an established SSH session
     * @param source      source file
     * @param destination destination file
     * @return status uploaded file
     */
    public static HostSystem pushUpload(HostSystem hostSystem, Session session, String source, String destination) {


        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
        Channel channel = null;
        ChannelSftp c = null;

        try (FileInputStream file = new FileInputStream(source)) {
            channel = session.openChannel("sftp");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect(CHANNEL_TIMEOUT);

            c = (ChannelSftp) channel;
            destination = destination.replaceAll("~\\/|~", "");

            c.put(file, destination);

        } catch (JSchException | IOException | SftpException ex) {
            log.info(ex.toString(), ex);
            hostSystem.setErrorMsg(ex.getMessage());
            hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
        }
        //exit
        if (c != null) {
            c.exit();
        }
        //disconnect
        if (channel != null) {
            channel.disconnect();
        }

        return hostSystem;


    }


    /**
     * distributes authorized keys for host system
     *
     * @param hostSystem      object contains host system information
     * @param session         an established SSH session
     * @param appPublicKey    application public key value
     * @return status of key distribution
     */
    public static HostSystem addPubKey(HostSystem hostSystem, Session session, String appPublicKey) {

        try {
            String authorizedKeys = hostSystem.getAuthorizedKeys().replaceAll("~\\/|~", "");

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("cat " + authorizedKeys);
            ((ChannelExec) channel).setErrStream(System.err);
            channel.setInputStream(null);

            InputStream in = channel.getInputStream();
            InputStreamReader is = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(is);

            channel.connect(CHANNEL_TIMEOUT);

            String appPubKey = appPublicKey.replace("\n", "").trim();
            StringBuilder existingKeysBuilder = new StringBuilder();

            String currentKey;
            while ((currentKey = reader.readLine()) != null) {
                existingKeysBuilder.append(currentKey).append("\n");
            }
            String existingKeys = existingKeysBuilder.toString();
            existingKeys = existingKeys.replaceAll("\\n$", "");
            reader.close();
            //disconnect
            channel.disconnect();

            StringBuilder newKeysBuilder = new StringBuilder();
            if (keyManagementEnabled) {
                //get keys assigned to system
                List<String> assignedKeys = PublicKeyDB.getPublicKeysForSystem(hostSystem.getId());
                for (String key : assignedKeys) {
                    newKeysBuilder.append(key.replace("\n", "").trim()).append("\n");
                }
                newKeysBuilder.append(appPubKey);
            } else {
                if (existingKeys.indexOf(appPubKey) < 0) {
                    newKeysBuilder.append(existingKeys).append("\n").append(appPubKey);
                } else {
                    newKeysBuilder.append(existingKeys);
                }
            }

            String newKeys = newKeysBuilder.toString();
            if (!newKeys.equals(existingKeys)) {
                log.info("Update Public Keys  ==> " + newKeys);
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand("echo '" + newKeys + "' > " + authorizedKeys + "; chmod 600 " + authorizedKeys);
                ((ChannelExec) channel).setErrStream(System.err);
                channel.setInputStream(null);
                channel.connect(CHANNEL_TIMEOUT);
                //disconnect
                channel.disconnect();
            }

        } catch (JSchException | SQLException | IOException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
        }
        return hostSystem;
    }

    /**
     * return the next instance id based on ids defined in the session map
     *
     * @param sessionId      session id
     * @param userSessionMap user session map
     * @return
     */
    private static int getNextInstanceId(Long sessionId, Map<Long, UserSchSessions> userSessionMap) {

        Integer instanceId = 1;
        if (userSessionMap.get(sessionId) != null) {

            for (Integer id : userSessionMap.get(sessionId).getSchSessionMap().keySet()) {
                if (!id.equals(instanceId) && userSessionMap.get(sessionId).getSchSessionMap().get(instanceId) == null) {
                    return instanceId;
                }
                instanceId = instanceId + 1;
            }
        }
        return instanceId;

    }


    /**
     * open new ssh session on host system
     *
     * @param passphrase     key passphrase for instance
     * @param password       password for instance
     * @param userId         user id
     * @param sessionId      session id
     * @param hostSystem     host system
     * @param userSessionMap user session map
     * @return status of systems
     */
    public static HostSystem openSSHTermOnSystem(String passphrase, String password, Long userId, Long sessionId, HostSystem hostSystem, Map<Long, UserSchSessions> userSessionMap) throws SQLException, GeneralSecurityException {

        JSch jsch = new JSch();

        int instanceId = getNextInstanceId(sessionId, userSessionMap);
        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);
        hostSystem.setInstanceId(instanceId);


        SchSession schSession = null;

        try {
            ApplicationKey appKey = PrivateKeyDB.getApplicationKey();
            //check to see if passphrase has been provided
            if (passphrase == null || passphrase.trim().equals("")) {
                passphrase = appKey.getPassphrase();
                //check for null inorder to use key without passphrase
                if (passphrase == null) {
                    passphrase = "";
                }
            }
            //add private key
            jsch.addIdentity(appKey.getId().toString(), appKey.getPrivateKey().trim().getBytes(), appKey.getPublicKey().getBytes(), passphrase.getBytes());

            //create session
            Session session = jsch.getSession(hostSystem.getUser(), hostSystem.getHost(), hostSystem.getPort());

            //set password if it exists
            if (password != null && !password.trim().equals("")) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.setServerAliveInterval(SERVER_ALIVE_INTERVAL);
            session.connect(SESSION_TIMEOUT);
            Channel channel = session.openChannel("shell");
            if ("true".equals(AppConfig.getProperty("agentForwarding"))) {
                ((ChannelShell) channel).setAgentForwarding(true);
            }
            ((ChannelShell) channel).setPtyType("xterm");

            InputStream outFromChannel = channel.getInputStream();


            //new session output
            SessionOutput sessionOutput = new SessionOutput(sessionId, hostSystem);

            Runnable run = new SecureShellTask(sessionOutput, outFromChannel);
            Thread thread = new Thread(run);
            thread.start();


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

            //refresh keys for session
            addPubKey(hostSystem, session, appKey.getPublicKey());


        } catch (JSchException | IOException | GeneralSecurityException ex) {
            log.info(ex.toString(), ex);
            hostSystem.setErrorMsg(ex.getMessage());
            if (ex.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            } else if (ex.getMessage().toLowerCase().contains("auth fail") || ex.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystem.setStatusCd(HostSystem.AUTH_FAIL_STATUS);
            } else if (ex.getMessage().toLowerCase().contains("unknownhostexception")) {
                hostSystem.setErrorMsg("DNS Lookup Failed");
                hostSystem.setStatusCd(HostSystem.HOST_FAIL_STATUS);
            } else {
                hostSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);
            }
        }

        //add session to map
        if (hostSystem.getStatusCd().equals(HostSystem.SUCCESS_STATUS)) {
            //get the server maps for user
            UserSchSessions userSchSessions = userSessionMap.get(sessionId);

            //if no user session create a new one
            if (userSchSessions == null) {
                userSchSessions = new UserSchSessions();
            }
            Map<Integer, SchSession> schSessionMap = userSchSessions.getSchSessionMap();

            //add server information
            schSessionMap.put(instanceId, schSession);
            userSchSessions.setSchSessionMap(schSessionMap);
            //add back to map
            userSessionMap.put(sessionId, userSchSessions);
        }

        SystemStatusDB.updateSystemStatus(hostSystem, userId);
        SystemDB.updateSystem(hostSystem);


        return hostSystem;
    }


    /**
     * distributes public keys to all systems
     */
    public static void distributePubKeysToAllSystems() throws SQLException, GeneralSecurityException {

        if (keyManagementEnabled) {
            List<HostSystem> hostSystemList = SystemDB.getAllSystems();
            for (HostSystem hostSystem : hostSystemList) {
                hostSystem = SSHUtil.authAndAddPubKey(hostSystem, null, null);
                SystemDB.updateSystem(hostSystem);
            }
        }
    }


    /**
     * distributes public keys to all systems under profile
     *
     * @param profileId profile id
     */
    public static void distributePubKeysToProfile(Long profileId) throws SQLException, GeneralSecurityException {

        if (keyManagementEnabled) {
            List<HostSystem> hostSystemList = ProfileSystemsDB.getSystemsByProfile(profileId);
            for (HostSystem hostSystem : hostSystemList) {
                hostSystem = SSHUtil.authAndAddPubKey(hostSystem, null, null);
                SystemDB.updateSystem(hostSystem);
            }
        }
    }

    /**
     * distributes public keys to all systems under all user profiles
     *
     * @param userId user id
     */
    public static void distributePubKeysToUser(Long userId) throws SQLException, GeneralSecurityException {

        if (keyManagementEnabled) {
            for (Profile profile : UserProfileDB.getProfilesByUser(userId)) {
                List<HostSystem> hostSystemList = ProfileSystemsDB.getSystemsByProfile(profile.getId());
                for (HostSystem hostSystem : hostSystemList) {
                    hostSystem = SSHUtil.authAndAddPubKey(hostSystem, null, null);
                    SystemDB.updateSystem(hostSystem);
                }
            }
        }
    }


    /**
     * returns public key fingerprint
     *
     * @param publicKey public key
     * @return fingerprint of public key
     */
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

    /**
     * returns public key type
     *
     * @param publicKey public key
     * @return fingerprint of public key
     */
    public static String getKeyType(String publicKey) {
        String keyType = null;
        if (StringUtils.isNotEmpty(publicKey)) {
            if (publicKey.contains("ssh-")) {
                publicKey = publicKey.substring(publicKey.indexOf("ssh-"));
            } else if (publicKey.contains("ecdsa-")) {
                publicKey = publicKey.substring(publicKey.indexOf("ecdsa-"));
            }
            try {
                KeyPair keyPair = KeyPair.load(new JSch(), null, publicKey.getBytes());
                if (keyPair != null) {
                    int type = keyPair.getKeyType();
                    if (KeyPair.DSA == type) {
                        keyType = "DSA";
                    } else if (KeyPair.RSA == type) {
                        keyType = "RSA";
                    } else if (KeyPair.ECDSA == type) {
                        keyType = "ECDSA";
                    } else if (KeyPair.ED25519 == type) {
                        keyType = "ED25519";
                    } else if (KeyPair.ED448 == type) {
                        keyType = "ED448";
                    } else if (KeyPair.UNKNOWN == type) {
                        keyType = "UNKNOWN";
                    } else if (KeyPair.ERROR == type) {
                        keyType = "ERROR";
                    }
                }

            } catch (JSchException ex) {
                log.error(ex.toString(), ex);
            }
        }
        return keyType;

    }

    /**
     * Build an unencrypted OpenSSH private key (openssh-key-v1, cipher=none).
     */
    public static String buildOpenSSHPrivateKey(java.security.KeyPair kp, int type) throws IOException {
        String keyType = (type == KeyPair.ED25519) ? "ssh-ed25519" : "ssh-ed448";

        ByteArrayOutputStream outer = new ByteArrayOutputStream();
        outer.write("openssh-key-v1\0".getBytes(StandardCharsets.US_ASCII));
        writeSSHString(outer, "none".getBytes(StandardCharsets.US_ASCII)); // ciphername
        writeSSHString(outer, "none".getBytes(StandardCharsets.US_ASCII)); // kdfname
        writeSSHString(outer, new byte[0]);                                 // kdfoptions
        outer.write(ByteBuffer.allocate(4).putInt(1).array());              // number of keys

        // ----- public key blob -----
        ByteArrayOutputStream pubBlob = new ByteArrayOutputStream();
        writeSSHString(pubBlob, keyType.getBytes(StandardCharsets.UTF_8));
        byte[] pubRaw = extractRawKeyFromX509(kp.getPublic().getEncoded());
        writeSSHBytes(pubBlob, pubRaw);
        byte[] pubBlobBytes = pubBlob.toByteArray();
        writeSSHBytes(outer, pubBlobBytes);

        // ----- private key section -----
        ByteArrayOutputStream privBlob = new ByteArrayOutputStream();

        int checkInt = new java.util.Random().nextInt();
        privBlob.write(ByteBuffer.allocate(4).putInt(checkInt).array());
        privBlob.write(ByteBuffer.allocate(4).putInt(checkInt).array());

        writeSSHString(privBlob, keyType.getBytes(StandardCharsets.UTF_8));
        writeSSHBytes(privBlob, pubRaw);

        byte[] privRaw = extractRawKeyFromX509(kp.getPrivate().getEncoded());

        // For Ed25519: private key = 32 bytes seed, concatenate with public key (32 bytes)
        if (keyType.equals("ssh-ed25519") && privRaw.length > 32) {
            privRaw = Arrays.copyOfRange(privRaw, privRaw.length - 32, privRaw.length);
        }

        ByteArrayOutputStream privConcat = new ByteArrayOutputStream();
        privConcat.write(privRaw);
        privConcat.write(pubRaw);
        writeSSHBytes(privBlob, privConcat.toByteArray());

        writeSSHString(privBlob, "".getBytes(StandardCharsets.UTF_8)); // comment (empty)

        // padding to 8-byte boundary
        int padLen = 8 - (privBlob.size() % 8);
        for (int i = 1; i <= padLen; i++) privBlob.write(i);

        writeSSHBytes(outer, privBlob.toByteArray());

        String base64 = Base64.getMimeEncoder(70, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(outer.toByteArray());
        return "-----BEGIN OPENSSH PRIVATE KEY-----\n" + base64 + "\n-----END OPENSSH PRIVATE KEY-----\n";
    }

    public static String buildOpenSSHPrivateKey(Long userId, java.security.KeyPair kp, int type, String passphrase) throws IOException, GeneralSecurityException, InterruptedException {
        return buildOpenSSHPrivateKey(userId != null ? userId.toString() : UUID.randomUUID().toString(), kp, type, passphrase);
    }

    /**
     * Build an OpenSSH private key; if passphrase provided, rewrap with ssh-keygen to use bcrypt KDF.
     */
    public static String buildOpenSSHPrivateKey(String userId, java.security.KeyPair kp, int type, String passphrase) throws IOException, GeneralSecurityException, InterruptedException {
        String unencryptedPEM = buildOpenSSHPrivateKey(kp, type);

        if (StringUtils.isBlank(passphrase)) {
            return unencryptedPEM;
        }

        return rewrapWithOpenSSHKeygen(userId, unencryptedPEM, passphrase);
    }


    /**
     * Use system ssh-keygen to convert an unencrypted OpenSSH key into bcrypt-encrypted OpenSSH key.
     */
    public static String rewrapWithOpenSSHKeygen(String userId, String pem, String passphrase) throws IOException, InterruptedException, GeneralSecurityException {
        Path tmp = Files.createTempFile("bastillion_key_" + userId + "_", ".key");
        Files.write(tmp, pem.getBytes(StandardCharsets.US_ASCII));
        try {
            // chmod 600 (ignore if unsupported FS)
            try {
                Files.setPosixFilePermissions(tmp, EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE));
            } catch (UnsupportedOperationException ignored) {
                // Windows or FS without POSIX perms
            }

            // ssh-keygen -p -P "" -N <passphrase> -f <file> -o -a 16
            ProcessBuilder pb = new ProcessBuilder(
                    "ssh-keygen", "-p",
                    "-P", "",
                    "-N", passphrase,
                    "-f", tmp.toAbsolutePath().toString(),
                    "-o",
                    "-a", "16"
            );
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    log.debug("[ssh-keygen] {}", line);
                }
            }

            int rc = proc.waitFor();
            if (rc != 0) {
                throw new IOException("ssh-keygen exited with code " + rc);
            }

            byte[] out = Files.readAllBytes(tmp);
            return new String(out, StandardCharsets.US_ASCII);
        } finally {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException e) {
                log.warn("Unable to delete temp key {}", tmp);
            }
        }
    }

    public static byte[] extractRawKeyFromX509(byte[] x509Encoded) {
        // DER structure: SubjectPublicKeyInfo -> BIT STRING
        // Find the BIT STRING header 0x03, then skip 2–3 bytes
        for (int i = 0; i < x509Encoded.length - 3; i++) {
            if (x509Encoded[i] == 0x03 && x509Encoded[i + 2] == 0x00) {
                return Arrays.copyOfRange(x509Encoded, i + 3, x509Encoded.length);
            }
        }
        // fallback: assume already raw
        return x509Encoded;
    }

    public static byte[] encodeSSHPublicKey(String keyType, byte[] rawPubBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeSSHString(out, keyType.getBytes(StandardCharsets.UTF_8));
        byte[] keyPart = extractRawKeyFromX509(rawPubBytes);
        writeSSHBytes(out, keyPart);
        return out.toByteArray();
    }

    public static void writeSSHString(ByteArrayOutputStream out, byte[] str) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(str.length).array());
        out.write(str);
    }

    public static void writeSSHBytes(ByteArrayOutputStream out, byte[] bytes) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
        out.write(bytes);
    }


}
