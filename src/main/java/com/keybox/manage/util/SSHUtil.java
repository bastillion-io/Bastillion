/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.keybox.common.util.AppConfigLkup;
import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.Script;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.task.ScriptTask;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
            jsch.addIdentity(KEY_PATH + "/" + KEY_NAME, PrivateKeyDB.getPassphrase());


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
     * execute script on host system
     *
     * @param hostSystemStatus object contains host system information
     * @param password         password to host system if needed
     * @param script script object
     * @return status of key distribution
     */
    public static SystemStatus execScriptOnSystem(SystemStatus hostSystemStatus, String password, Script script) {


        //set status to in progress
        ExecutorService executor = Executors.newCachedThreadPool();

        JSch jsch = new JSch();
        try {
            //add private key
            jsch.addIdentity(KEY_PATH + "/" + KEY_NAME, PrivateKeyDB.getPassphrase());
            Session session = jsch.getSession(hostSystemStatus.getHostSystem().getUser(), hostSystemStatus.getHostSystem().getHost(), hostSystemStatus.getHostSystem().getPort());

            //set password if it exists
            if (password != null && !password.trim().equals("")) {
                session.setPassword(password);
            }
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SESSION_TIMEOUT);

            //set status to in progress
            hostSystemStatus.setStatusCd(SystemStatus.IN_PROGRESS_STATUS);
            SystemStatusDB.updateSystemStatus(hostSystemStatus);
            /* not sure if i should do this
            //set auth keys if password exists
            if (password != null && !password.trim().equals("")) {
                hostSystemStatus = SSHUtil.authAndAddPubKey(hostSystemStatus, session);
            }*/
            //make sure no errors have occurred
            if (SystemStatus.IN_PROGRESS_STATUS.equals(hostSystemStatus.getStatusCd())) {
                executor.execute(new ScriptTask(session, hostSystemStatus, script));
            }
        } catch (Exception e) {
            hostSystemStatus.setErrorMsg(e.getMessage());
            if (e.getMessage().contains("Auth fail") || e.getMessage().contains("Auth cancel")) {
                hostSystemStatus.setStatusCd(SystemStatus.AUTH_FAIL_STATUS);
            } else {
                hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
            }
        }
        return hostSystemStatus;
    }


}
