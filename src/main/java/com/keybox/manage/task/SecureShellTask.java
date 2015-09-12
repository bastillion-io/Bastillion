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
package com.keybox.manage.task;

import com.keybox.manage.util.SessionOutputUtil;
import com.keybox.manage.model.SessionOutput;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Task to watch for output read from the ssh session stream
 */
public class SecureShellTask implements Runnable {

    private static Logger log = LoggerFactory.getLogger(SecureShellTask.class);

    InputStream outFromChannel;
    SessionOutput sessionOutput;

    public SecureShellTask(SessionOutput sessionOutput, InputStream outFromChannel) {

        this.sessionOutput = sessionOutput;
        this.outFromChannel = outFromChannel;
    }

    public void run() {
        InputStreamReader isr = new InputStreamReader(outFromChannel);
        BufferedReader br = new BufferedReader(isr);
        try {

            SessionOutputUtil.addOutput(sessionOutput);

            char[] buff = new char[1024];
            int read;
            while((read = br.read(buff)) != -1) {

                SessionOutputUtil.addToOutput(sessionOutput.getSessionId(), sessionOutput.getInstanceId(), buff,0,read);
                Thread.sleep(50);
            }

            SessionOutputUtil.removeOutput(sessionOutput.getSessionId(), sessionOutput.getInstanceId());

        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
    }

}
