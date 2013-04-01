/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.manage.util;

import com.jcraft.jsch.*;
import com.keybox.common.util.AppConfigLkup;
import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.db.ScriptDB;
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.SchSession;
import com.keybox.manage.model.Script;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.task.SessionOutputTask;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * SSH utility class used to create public/private key for system and distribute authorized key files
 */
public class SSHUtil {

    //private key name
    public static final String KEY_NAME = "id_dsa";
    //public key name
    public static final String PUB_KEY_NAME = KEY_NAME + ".pub";
    //system path to public/private key
    public static String KEY_PATH = DBUtils.class.getClassLoader().getResource("com/keybox/common/db").getPath();

    public static final int SESSION_TIMEOUT = 60000;
    public static final int CHANNEL_TIMEOUT = 60000;

    /**
     * returns the system's public key
     *
     * @return system's public key
     */
    public static String getPublicKey() {
        String publicKey = null;
        Commandline cmd = new Commandline();


        //get ssh-keygen cmd from properties file
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("pubKeyPath", KEY_PATH);
        replaceMap.put("pubKeyName", PUB_KEY_NAME);
        String cmdStr = AppConfigLkup.getProperty("catPublicKeyCmd", replaceMap);

        cmd.createArg().setLine(cmdStr);

        CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
        try {
            int returnCode = CommandLineUtils.executeCommandLine(cmd, out, err);

            if (returnCode != 0) {
                System.out.println(err.getOutput());
            } else {
                publicKey = out.getOutput();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return publicKey;
    }

    /**
     * generates system's public/private key par and returns passphrase
     *
     * @return passphrase for system generated key
     */
    public static String keyGen() {


        String passphrase = UUID.randomUUID().toString();

        Commandline cmd = new Commandline();


        //get ssh-keygen cmd from properties file
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("keyPath", KEY_PATH);
        replaceMap.put("keyName", KEY_NAME);
        replaceMap.put("passphrase", passphrase);
        String cmdStr = AppConfigLkup.getProperty("sshKeyGenCmd", replaceMap);

        //set command
        cmd.createArg().setLine(cmdStr);

        CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
        try {
            int returnCode = CommandLineUtils.executeCommandLine(cmd, out, err);

            if (returnCode != 0) {
                System.out.println(err.getOutput());
            } else {
                System.out.println(out.getOutput());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        cmd.clear();
        cmd.clearArgs();
        return passphrase;


    }

    /**
     * distributes authorized keys for host system
     *
     * @param hostSystemStatus object contains host system information
     * @param password         password to host system if needed
     * @return status of key distribution
     */
    public static SystemStatus authAndAddPubKey(SystemStatus hostSystemStatus, String password) {


        JSch jsch = new JSch();
        Session session = null;
        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);
        try {

            //add private key
            jsch.addIdentity(KEY_PATH + "/" + KEY_NAME, EncryptionUtil.decrypt(PrivateKeyDB.getPassphrase()));


            //create session
            session = jsch.getSession(hostSystemStatus.getHostSystem().getUser(), hostSystemStatus.getHostSystem().getHost(), hostSystemStatus.getHostSystem().getPort());

            //set password if passed in
            if (password != null && !password.equals("")) {
                session.setPassword(password);
            }
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(SESSION_TIMEOUT);
            authAndAddPubKey(hostSystemStatus, session);

        } catch (Exception e) {
            hostSystemStatus.setErrorMsg(e.getMessage());
            if (e.getMessage().contains("Auth fail") || e.getMessage().contains("Auth cancel")) {
                hostSystemStatus.setStatusCd(SystemStatus.AUTH_FAIL_STATUS);
            } else {
                hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
            }


        }

        if (session != null) {
            session.disconnect();
        }
        return hostSystemStatus;


    }

    /**
     * distributes authorized keys for host system
     *
     * @param hostSystemStatus object contains host system information
     * @param session          an established SSH session
     * @return status of key distribution
     */
    public static SystemStatus authAndAddPubKey(SystemStatus hostSystemStatus, Session session) {


        Channel channel = null;
        ChannelSftp c = null;

        try {


            channel = session.openChannel("sftp");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect(CHANNEL_TIMEOUT);

            c = (ChannelSftp) channel;
            String authorizedKeys = hostSystemStatus.getHostSystem().getAuthorizedKeys().replaceAll("~\\/", "");

            //turn public key list into a input stream
            InputStream inputStreamAuthKeyVal = new ByteArrayInputStream(hostSystemStatus.getAuthKeyVal().getBytes());
            c.put(inputStreamAuthKeyVal, authorizedKeys);


        } catch (Exception e) {
            hostSystemStatus.setErrorMsg(e.getMessage());
            hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
        }
        //exit
        if (c != null) {
            c.exit();
        }
        //disconnect
        if (channel != null) {
            channel.disconnect();
        }

        return hostSystemStatus;


    }




    /**
     * open SSH session host system
     *
     * @param hostSystemStatus object contains host system information
     * @param password         password to host system if needed
     * @return status of key distribution
     */
    public static SystemStatus openSSHTermOnSystem(SystemStatus hostSystemStatus, String password, Map<Long, SchSession> schSessionMap, Long scriptId) {

        JSch jsch = new JSch();

        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);

        SchSession schSession = null;

        try {
            //add private key
            jsch.addIdentity(KEY_PATH + "/" + KEY_NAME, EncryptionUtil.decrypt(PrivateKeyDB.getPassphrase()));
            Session session = jsch.getSession(hostSystemStatus.getHostSystem().getUser(), hostSystemStatus.getHostSystem().getHost(), hostSystemStatus.getHostSystem().getPort());

            //set password if it exists
            if (password != null && !password.trim().equals("")) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SESSION_TIMEOUT);
            Channel channel = session.openChannel("shell");
            ((ChannelShell) channel).setPtyType("vt102");

            InputStream outFromChannel = channel.getInputStream();

            ExecutorService executor = Executors.newCachedThreadPool();

            executor.execute(new SessionOutputTask(hostSystemStatus.getHostSystem().getId(), outFromChannel));


            OutputStream inputToChannel = channel.getOutputStream();
            PrintStream commander = new PrintStream(inputToChannel, true);


            channel.connect();

            schSession = new SchSession();
            schSession.setSession(session);
            schSession.setChannel(channel);
            schSession.setCommander(commander);
            schSession.setInputToChannel(inputToChannel);
            schSession.setOutFromChannel(outFromChannel);
            schSession.setHostSystem(hostSystemStatus.getHostSystem());


        } catch (Exception e) {
            hostSystemStatus.setErrorMsg(e.getMessage());
            if (e.getMessage().contains("Auth fail") || e.getMessage().contains("Auth cancel")) {
                hostSystemStatus.setStatusCd(SystemStatus.AUTH_FAIL_STATUS);
            } else {
                hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
            }
        }

        //add session to map
        if (hostSystemStatus.getStatusCd().equals(SystemStatus.SUCCESS_STATUS)) {
            schSessionMap.put(hostSystemStatus.getHostSystem().getId(), schSession);


            //run script if provided run it
            if (scriptId != null && scriptId > 0) {
                Script script = ScriptDB.getScript(scriptId);
                BufferedReader reader = new BufferedReader(new StringReader(script.getScript()));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        schSession.getCommander().println(line);
                    }
                } catch (Exception e) {
                    hostSystemStatus.setErrorMsg(e.getMessage());
                    hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
                }

            }
        }

        SystemStatusDB.updateSystemStatus(hostSystemStatus);

        return hostSystemStatus;
    }


}
