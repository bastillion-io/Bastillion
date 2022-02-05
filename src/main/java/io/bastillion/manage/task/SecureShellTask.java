/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.task;

import io.bastillion.manage.model.SessionOutput;
import io.bastillion.manage.util.SessionOutputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Task to watch for output read from the ssh session stream
 */
public class SecureShellTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SecureShellTask.class);

    InputStream outFromChannel;
    SessionOutput sessionOutput;

    public SecureShellTask(SessionOutput sessionOutput, InputStream outFromChannel) {

        this.sessionOutput = sessionOutput;
        this.outFromChannel = outFromChannel;
    }

    public void run() {
        InputStreamReader isr = new InputStreamReader(outFromChannel);
        BufferedReader br = new BufferedReader(isr);

        SessionOutputUtil.addOutput(sessionOutput);

        char[] buff = new char[1024];
        int read;
        try {
            while ((read = br.read(buff)) != -1) {
                SessionOutputUtil.addToOutput(sessionOutput.getSessionId(), sessionOutput.getInstanceId(), buff, 0, read);
                Thread.sleep(50);
            }
            SessionOutputUtil.removeOutput(sessionOutput.getSessionId(), sessionOutput.getInstanceId());

        } catch (IOException | InterruptedException ex) {
            log.error(ex.toString(), ex);
        }
    }

}
