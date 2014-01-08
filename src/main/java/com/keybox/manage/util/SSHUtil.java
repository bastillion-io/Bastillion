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
import com.keybox.manage.db.PublicKeyDB;
import com.keybox.manage.db.SystemDB;
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.*;
import com.keybox.manage.task.SecureShellTask;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
     * returns the system's public key
     *
     * @return system's public key
     */
    public static String getPrivateKey() {
        String privateKey = null;


        //get ssh-keygen cmd from properties file
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("keyPath", KEY_PATH);
        replaceMap.put("keyName", KEY_NAME);
        //cat public ssh key
        String cmdStr = AppConfigLkup.getProperty("catPrivateKeyCmd", replaceMap);


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CommandLine cmdLine = CommandLine.parse(cmdStr);
        DefaultExecutor executor = new DefaultExecutor();
        PumpStreamHandler streamHandler = new PumpStreamHandler(out);
        executor.setStreamHandler(streamHandler);
        try {
            executor.execute(cmdLine);
            privateKey = out.toString();
        } catch (ExecuteException ex) {
            System.out.println(out.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return privateKey;
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
     * delete SSH keys
     *
     */
    public static void deleteSshKeys() {


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

    }
    /**
     * generates system's public/private key par and returns passphrase
     *
     * @return passphrase for system generated key
     */
    public static String keyGen(String passphrase) {


        deleteSshKeys();

        //if no passphrase set to random GUID
        if (passphrase == null || passphrase.trim().equals("")) {
            passphrase = UUID.randomUUID().toString();
        }

        //get ssh-keygen cmd from properties file
        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("keyPath", KEY_PATH);
        replaceMap.put("keyName", KEY_NAME);
        replaceMap.put("passphrase", passphrase);
        //create new ssh keys
        String cmdStr = AppConfigLkup.getProperty("sshKeyGenCmd", replaceMap);

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


        return passphrase;


    }

    /**
     * distributes authorized keys for host system
     *
     * @param hostSystem object contains host system information
     * @param passphrase ssh key passphrase
     * @param password   password to host system if needed
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
            }
            //add private key
            jsch.addIdentity(appKey.getId().toString(), appKey.getPrivateKey().trim().getBytes(), appKey.getPublicKey().getBytes(), passphrase.getBytes());

            //create session
            session = jsch.getSession(hostSystem.getUser(), hostSystem.getHost(), hostSystem.getPort());

            //set password if passed in
            if (password != null && !password.equals("")) {
                session.setPassword(password);
            }
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(SESSION_TIMEOUT);

            addPubKey(hostSystem, session, appKey.getPublicKey());

        } catch (Exception e) {
            hostSystem.setErrorMsg(e.getMessage());
            if (e.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            } else if (e.getMessage().toLowerCase().contains("auth fail") || e.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystem.setStatusCd(HostSystem.AUTH_FAIL_STATUS);
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
            hostSystem.setErrorMsg(e.getMessage());
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
     * @param hostSystem object contains host system information
     * @param session    an established SSH session
     * @param appPublicKey application public key value
     * @return status of key distribution
     */
    public static HostSystem addPubKey(HostSystem hostSystem, Session session, String appPublicKey) {


        Channel channel = null;
        ChannelSftp c = null;

        try {

            channel = session.openChannel("sftp");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect(CHANNEL_TIMEOUT);

            c = (ChannelSftp) channel;

            //return public key list into a input stream
            String authorizedKeys = hostSystem.getAuthorizedKeys().replaceAll("~\\/|~", "");

            String keyValue=appPublicKey.replace("\n", "").trim();

            for(String str : PublicKeyDB.getPublicKeysForSystem(hostSystem.getId())){
                keyValue=keyValue+"\n" +str.replace("\n", "").trim();
            }
            keyValue=keyValue+"\n";

            InputStream inputStreamAuthKeyVal = new ByteArrayInputStream(keyValue.getBytes());
            c.put(inputStreamAuthKeyVal, authorizedKeys);


        } catch (Exception e) {
            hostSystem.setErrorMsg(e.getMessage());
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
    * open new ssh session on host system
    *
    * @param passphrase key passphrase for instance
    * @param password password for instance
    * @param userId user id
    * @param sessionId session id
    * @param hostSystem host system
    * @param userSessionMap user session map
    * @return status of systems
    */
    public static HostSystem openSSHTermOnSystem(String passphrase, String password, Long userId, Long sessionId, HostSystem hostSystem,  Map<Long, UserSchSessions> userSessionMap) {

        JSch jsch = new JSch();

        hostSystem.setStatusCd(HostSystem.SUCCESS_STATUS);

        SchSession schSession = null;

        try {
            ApplicationKey appKey = PrivateKeyDB.getApplicationKey();
            //check to see if passphrase has been provided
            if (passphrase == null || passphrase.trim().equals("")) {
                passphrase = appKey.getPassphrase();
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
            session.connect(SESSION_TIMEOUT);
            Channel channel = session.openChannel("shell");
            ((ChannelShell) channel).setPtyType("vt102");

            InputStream outFromChannel = channel.getInputStream();


            //new session output
            SessionOutput sessionOutput=new SessionOutput();
            sessionOutput.setUserId(userId);
            sessionOutput.setHostSystemId(hostSystem.getId());
            sessionOutput.setSessionId(sessionId);


            Runnable run=new SecureShellTask(sessionOutput, outFromChannel);
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


        } catch (Exception e) {
            hostSystem.setErrorMsg(e.getMessage());
            if (e.getMessage().toLowerCase().contains("userauth fail")) {
                hostSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            } else if (e.getMessage().toLowerCase().contains("auth fail") || e.getMessage().toLowerCase().contains("auth cancel")) {
                hostSystem.setStatusCd(HostSystem.AUTH_FAIL_STATUS);
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
            Map<Long, SchSession> schSessionMap = userSchSessions.getSchSessionMap();

            //add server information
            schSessionMap.put(hostSystem.getId(), schSession);
            userSchSessions.setSchSessionMap(schSessionMap);
            //add back to map
            userSessionMap.put(sessionId, userSchSessions);
        }

        SystemStatusDB.updateSystemStatus(hostSystem, userId);
        SystemDB.updateSystem(hostSystem);

        return hostSystem;
    }


}
