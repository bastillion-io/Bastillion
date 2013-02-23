package com.keybox.manage.task;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.Script;
import com.keybox.manage.model.SystemStatus;
import java.io.*;


public class ScriptTask implements Runnable {

    Session session;
    Script script;
    SystemStatus hostSystemStatus;

    public ScriptTask(Session session, SystemStatus hostSystemStatus, Script script) {
        this.session = session;
        this.hostSystemStatus = hostSystemStatus;
        this.script = script;
    }


    public void run() {


        //default status to success
        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);

        try {
            Channel channel = session.openChannel("shell");
            ((ChannelShell) channel).setPtyType("vt102");

            OutputStream inputToChannel = channel.getOutputStream();
            PrintStream commander = new PrintStream(inputToChannel, true);

            InputStream outputFromChannel = channel.getInputStream();

            channel.connect();
            commander.println();

            BufferedReader reader = new BufferedReader(new StringReader(script.getScript()));

            String line = null;
            while ((line = reader.readLine()) != null) {
                commander.println(line);
            }

            commander.println("exit");
            commander.close();


            BufferedReader br = new BufferedReader(new InputStreamReader(outputFromChannel));
            String output = "";

            try {
                while ((line = br.readLine()) != null)
                    output = output + line + "\n";
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //System.out.println(output);

            hostSystemStatus.setOutput(output);

            channel.disconnect();

            session.disconnect();
        } catch (Exception e) {
            hostSystemStatus.setErrorMsg(e.getMessage());
            hostSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);
        }
        SystemStatusDB.updateSystemStatus(hostSystemStatus);


    }
}

