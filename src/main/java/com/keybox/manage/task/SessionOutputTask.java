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

/**
 * Task to watch for output read from the ssh session stream
 */
public class SessionOutputTask implements Runnable {

    InputStream outFromChannel;
    Long sessionId;

    public SessionOutputTask(Long sessionId, InputStream outFromChannel) {

        this.sessionId = sessionId;
        this.outFromChannel = outFromChannel;
    }

    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(outFromChannel));

        try {

            int value = 0;

            SessionOutput sessionOutput = new SessionOutput();
            sessionOutput.setSessionId(sessionId);
            SessionOutputUtil.addOutput(sessionOutput);

            while ((value = br.read()) != -1) {
                // converts int to character
                char c = (char) value;
                SessionOutputUtil.addCharToOutput(sessionOutput, c);

            }
            //sleep for 5 sec before removing output object so that leftover output is displayed
            Thread.sleep(5000);
            SessionOutputUtil.removeOutput(sessionOutput);

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

}
