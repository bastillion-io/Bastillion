package com.keybox.manage.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.keybox.common.util.AppConfigLkup;
import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.SystemKeyGenStatus;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SSH utility class used to create public/private key for system and distribute authorized key files
 */
public class SSHUtil {

    //private key name
    private static final String KEY_NAME = "id_dsa";
    //public key name
    private static final String PUB_KEY_NAME = KEY_NAME + ".pub";
    //system path to public/private key
    private static String KEY_PATH = DBUtils.class.getClassLoader().getResource("com/keybox/common/db").getPath();

    /**
     * returns the system's public key
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
     * @param hostSystemStatus object contains host system information
     * @param password password to host system if needed
     * @param passPhrase passphrase for system generated key
     * @return status of key distribution
     */
    public static SystemKeyGenStatus authAndAddPubKey(SystemKeyGenStatus hostSystemStatus, String password,
                                                      String passPhrase) {


        JSch jsch = new JSch();
        Channel channel = null;
        Session session = null;

        ChannelSftp c = null;

        hostSystemStatus.setStatusCd(SystemKeyGenStatus.SUCCESS_STATUS);
         try {

            //add private key
            jsch.addIdentity(KEY_PATH + "/" + KEY_NAME, passPhrase);


            //create session
            session = jsch.getSession(hostSystemStatus.getHostSystem().getUser(), hostSystemStatus.getHostSystem().getHost(), hostSystemStatus.getHostSystem().getPort());

            //set password if passed in
            if (password != null && !password.equals("")) {
                session.setPassword(password);
            }
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            channel = session.openChannel("sftp");
            channel.setInputStream(System.in);
            channel.setOutputStream(System.out);
            channel.connect();

            c = (ChannelSftp) channel;
            String authorizedKeys = hostSystemStatus.getHostSystem().getAuthorizedKeys().replaceAll("~\\/", "");

            //turn public key list into a input stream
            InputStream inputStreamAuthKeyVal = new ByteArrayInputStream(hostSystemStatus.getAuthKeyVal().getBytes());
            c.put(inputStreamAuthKeyVal, authorizedKeys);


        } catch (Exception e) {
            hostSystemStatus.setErrorMsg(e.getMessage());
            if (e.getMessage().contains("Auth fail")||e.getMessage().contains("Auth cancel")) {
                hostSystemStatus.setStatusCd(SystemKeyGenStatus.AUTH_FAIL_STATUS);
            } else {
                hostSystemStatus.setStatusCd(SystemKeyGenStatus.GENERIC_FAIL_STATUS);
            }


        }
        //exit
        if (c != null) {
            c.exit();
        }
        //disconnect
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        return hostSystemStatus;


    }


}
