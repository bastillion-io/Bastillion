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

import com.keybox.manage.model.SessionOutput;

import java.util.*;


public class SessionOutputUtil {

    private static Map<Long, SessionOutput> sessionOutputMap = new LinkedHashMap<Long, SessionOutput>();


    /**
     * adds a new output
     *
     * @param sessionOutput session output object
     */
    public synchronized static void addOutput(SessionOutput sessionOutput) {

        if (sessionOutput != null) {
            sessionOutputMap.put(sessionOutput.getSessionId(), sessionOutput);
        }

    }

    /**
     * remove output
     *
     * @param sessionOutput session output object
     */
    public synchronized static void removeOutput(SessionOutput sessionOutput) {


        if (sessionOutput != null) {
            sessionOutputMap.remove(sessionOutput.getSessionId());
        }

    }

    /**
     * adds a new output
     *
     * @param sessionOutput session output object
     */
    public synchronized static void addCharToOutput(SessionOutput sessionOutput, char c) {


        if (sessionOutput != null) {
            sessionOutput.getOutputChars().add(c);
            sessionOutputMap.put(sessionOutput.getSessionId(), sessionOutput);


        }

    }


    /**
     * returns list of output lines
     *
     * @return sessionId session id object
     */
    public synchronized static List<SessionOutput> getOutput() {
        List<SessionOutput> outputList = new ArrayList<SessionOutput>();


        for (Long outputId : sessionOutputMap.keySet()) {
            SessionOutput sessionOutput = sessionOutputMap.get(outputId);

            //get output chars and set to output
            StringBuilder output = new StringBuilder(sessionOutput.getOutputChars().size());
            for (Character ch : sessionOutput.getOutputChars()) {
                output.append(ch);
            }
            //sessionOutput.setOutput(output.toString().replaceAll("\\[.*?m|\\[.*?m",""));
            sessionOutput.setOutput(output.toString());
            sessionOutput.setOutputChars(new ArrayList<Character>());

            outputList.add(sessionOutput);

            //put back with new char array
            sessionOutputMap.put(sessionOutput.getSessionId(), sessionOutput);

        }

        return outputList;
    }


}
