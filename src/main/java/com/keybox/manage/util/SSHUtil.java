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
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.SchSession;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.task.SessionOutputTask;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

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


        //get ssh-keygen cmd from properties file
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("pubKeyPath", KEY_PATH);
        replaceMap.put("pubKeyName", PUB_KEY_NAME);
        //cat public ssh key
        String cmdStr = AppConfigLkup.getProperty("catPublicKeyCmd", replaceMap);


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CommandLine cmdLine = CommandLine.parse(cmdStr);
        DefaultExecutor executor = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(out);
        executor.setStreamHandler(streamHandler);
        try {
            executor.execute(cmdLine);
            publicKey = out.toString();
        } catch (ExecuteException ex) {
            System.out.println(out.toString());
        } catch (IOException ex) {
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
        return keyGen(null);

    }

    /**
     * generates system's public/private key par and returns passphrase
     *
     * @return passphrase for system generated key
     */
    public static String keyGen(String passphrase) {


        //get ssh-keygen cmd from properties file
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("keyPath", KEY_PATH);
        replaceMap.put("keyName", KEY_NAME);
        replaceMap.put("pubKeyPath", KEY_PATH);
        replaceMap.put("pubKeyName", PUB_KEY_NAME);
        //delete previous ssh keys
        String cmdStr = AppConfigLkup.getProperty("deleteSshKeys", replaceMap);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CommandLine cmdLine = CommandLine.parse(cmdStr);
        DefaultExecutor executor = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(out);
        executor.setStreamHandler(streamHandler);

        try {
            executor.execute(cmdLine);
        } catch (ExecuteException ex) {
            System.out.println(out.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        //if no passphrase set to random GUID
        if (passphrase == null || passphrase.trim().equals("")) {
            passphrase = UUID.randomUUID().toString();
        }

        //get ssh-keygen cmd from properties file
        replaceMap = new HashMap<String, String>();
        replaceMap.put("keyPath", KEY_PATH);
        replaceMap.put("keyName", KEY_NAME);
        replaceMap.put("passphrase", passphrase);
        //create new ssh keys
        cmdStr = AppConfigLkup.getProperty("sshKeyGenCmd", replaceMap);

        out = new ByteArrayOutputStream();
        cmdLine = CommandLine.parse(cmdStr);
        executor = new DefaultExecutor();
        streamHandler = new PumpStreamHandler(out);
        executor.setStreamHandler(streamHandler);

        try {
            executor.execute(cmdLine);
        } catch (ExecuteException ex) {
            System.out.println(out.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        return passphrase;


    }

    /**
     * checks to see if passphrase is valid for ssh-key
     *
     * @param passphrase ssh key passphrase
     * @return boolean validity indicator
     */
    public static boolean isPassphraseValid(String passphrase) {
        boolean isValid = false;


        String verifyPubKey = null;
        //get ssh-keygen command from properties file to verify passphrase
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("keyPath", KEY_PATH);
        replaceMap.put("keyName", KEY_NAME);
        replaceMap.put("passphrase", passphrase);
        //verify ssh key passphrase
        String cmdStr = AppConfigLkup.getProperty("verifyPassphrase", replaceMap);


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CommandLine cmdLine = CommandLine.parse(cmdStr);
        DefaultExecutor executor = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(out);
        executor.setStreamHandler(streamHandler);
        try {
            executor.execute(cmdLine);
            verifyPubKey = out.toString();
        } catch (ExecuteException ex) {
            System.out.println(out.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        String currentPubKey = getPublicKey();
        if (currentPubKey != null && !currentPubKey.trim().equals("")
                && verifyPubKey != null && !verifyPubKey.trim().equals("")) {

            //replace everything but public key text
            currentPubKey = currentPubKey.trim().replaceAll("^.*?\\ |\\ .*?$", "");
            verifyPubKey = verifyPubKey.trim().replaceAll("^.*?\\ |\\ .*?$", "");
            //compair to make sure it matches
            if (currentPubKey.equals(verifyPubKey)) {
                isValid = true;
            }

        }


        return isValid;

    }

    /**
     * distributes authorized keys for host system
     *
     * @param hostSystemStatus object contains host system information
     * @param passphrase       ssh key passphrase
     * @param password         password to host system if needed
     * @return status of key distribution
     */
    public static SystemStatus authAndAddPubKey(SystemStatus hostSystemStatus, String passphrase, String password) {


        JSch jsch = new JSch();
        Session session = null;
        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);
        try {

            //add private key
            jsch.addIdentity(KEY_PATH + "/" + KEY_NAME, passphrase);

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
            if (e.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystemStatus.setStatusCd(SystemStatus.PUBLIC_KEY_FAIL_STATUS);
            } else if (e.getMessage().toLowerCase().contains("auth fail") || e.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystemStatus.setStatusCd(SystemStatus.AUTH_FAIL_STATUS);
            } else {
                hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
            }


        }

        if (session != null) {
            session.disconnect();
        }
        SystemStatusDB.updateSystemStatus(hostSystemStatus);
        return hostSystemStatus;


    }


    /**
     * distributes uploaded item to system defined
     *
     * @param hostSystemStatus object contains host system information
     * @param session          an established SSH session
     * @param source           source file
     * @param destination      destination file
     * @return status uploaded file
     */
    public static SystemStatus pushUpload(SystemStatus hostSystemStatus, Session session, String source, String destination) {


        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);
        Channel channel = null;
        ChannelSftp c = null;

        try {


            channel = session.openChannel("sftp");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect(CHANNEL_TIMEOUT);

            c = (ChannelSftp) channel;
            destination = destination.replaceAll("~\\/|~", "");


            //get file input stream
            FileInputStream file = new FileInputStream(source);
            c.put(file, destination);


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
            String authorizedKeys = hostSystemStatus.getHostSystem().getAuthorizedKeys().replaceAll("~\\/|~", "");

            //return public key list into a input stream
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
     * @param passphrase       ssh key passphrase
     * @param password         password to host system if needed
     * @return status of key distribution
     */
    public static SystemStatus openSSHTermOnSystem(SystemStatus hostSystemStatus, String passphrase, String password, Map<Long, SchSession> schSessionMap) {

        JSch jsch = new JSch();

        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);

        SchSession schSession = null;

        try {
            //add private key
            jsch.addIdentity(KEY_PATH + "/" + KEY_NAME, passphrase);

            //create session
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
            if (e.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystemStatus.setStatusCd(SystemStatus.PUBLIC_KEY_FAIL_STATUS);
            } else if (e.getMessage().toLowerCase().contains("auth fail") || e.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystemStatus.setStatusCd(SystemStatus.AUTH_FAIL_STATUS);
            } else {
                hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
            }
        }

        //add session to map
        if (hostSystemStatus.getStatusCd().equals(SystemStatus.SUCCESS_STATUS)) {
            schSessionMap.put(hostSystemStatus.getHostSystem().getId(), schSession);
        }

        SystemStatusDB.updateSystemStatus(hostSystemStatus);

        return hostSystemStatus;
    }


}
