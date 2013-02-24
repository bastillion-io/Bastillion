/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.task;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.keybox.common.util.AppConfigLkup;
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.Script;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.util.SSHUtil;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Task to run a shell script with timeout
 */
public class ScriptTask implements Runnable {
    static Timer timer = new Timer(true);

    Session session;
    Script script;
    SystemStatus hostSystemStatus;

    public ScriptTask(Session session, SystemStatus hostSystemStatus, Script script) {
        this.session = session;
        this.hostSystemStatus = hostSystemStatus;
        this.script = script;

    }


    public void run() {


        timer.schedule(new TimeOut(Thread.currentThread()), 60000 * Integer.parseInt(AppConfigLkup.getProperty("shellExecTimeout")) );
        //default status to success
        hostSystemStatus.setStatusCd(SystemStatus.SUCCESS_STATUS);

        try {
            Channel channel = session.openChannel("shell");
            ((ChannelShell) channel).setPtyType("vt102");

            OutputStream inputToChannel = channel.getOutputStream();
            PrintStream commander = new PrintStream(inputToChannel, true);

            InputStream outputFromChannel = channel.getInputStream();

            channel.connect(SSHUtil.CHANNEL_TIMEOUT);
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
            } catch (java.io.InterruptedIOException ex) {
                hostSystemStatus.setStatusCd(SystemStatus.TIMEOUT_STATUS);
            }

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


class TimeOut extends TimerTask {

    Thread thread;

    public TimeOut (Thread thread) {
        this.thread = thread;
    }

    public void run() {

        if (thread != null && thread.isAlive()) {
            //System.out.println("Timeout: " + thread.getName());
            thread.interrupt();
        }
    }
}





