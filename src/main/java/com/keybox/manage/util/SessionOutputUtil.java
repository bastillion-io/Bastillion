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

import com.keybox.common.util.AppConfig;
import com.keybox.manage.db.SessionAuditDB;
import com.keybox.manage.model.SessionOutput;
import com.keybox.manage.model.UserSessionsOutput;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility to is used to store the output for a session until the ajax call that brings it to the screen
 */
public class SessionOutputUtil {


    private static Map<Long, UserSessionsOutput> userSessionsOutputMap = new ConcurrentHashMap<Long, UserSessionsOutput>();
    private static String enableAudit = AppConfig.getProperty("enableAudit");


    /**
     * removes session for user session
     *
     * @param sessionId session id
     */
    public static void removeUserSession(Long sessionId) {
        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionId);
        if (userSessionsOutput != null) {
            userSessionsOutput.getSessionOutputMap().clear();
        }
        userSessionsOutputMap.remove(sessionId);

    }

    /**
     * removes session output for host system
     *
     * @param sessionId    session id
     * @param hostSystemId host system id
     */
    public static void removeOutput(Long sessionId, Long hostSystemId) {

        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionId);
        if (userSessionsOutput != null) {
            userSessionsOutput.getSessionOutputMap().remove(hostSystemId);
        }
    }

    /**
     * adds a new output
     *
     * @param sessionId     session id
     * @param sessionOutput session output object
     */
    public static void addOutput(Long sessionId, SessionOutput sessionOutput) {

        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionId);
        if (userSessionsOutput == null) {
            userSessionsOutputMap.put(sessionId, new UserSessionsOutput());
            userSessionsOutput = userSessionsOutputMap.get(sessionId);
        }
        userSessionsOutput.getSessionOutputMap().put(sessionOutput.getHostSystemId(), new StringBuilder());


    }


    /**
     * adds a new output
     *
     * @param sessionId    session id
     * @param hostSystemId host system id
     * @param value        Array that is the source of characters
     * @param offset       The initial offset
     * @param count        The length
     */
    public static void addToOutput(Long sessionId, Long hostSystemId, char value[], int offset, int count) {


        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionId);
        if (userSessionsOutput != null) {
            userSessionsOutput.getSessionOutputMap().get(hostSystemId).append(value, offset, count);
        }

    }


    /**
     * returns list of output lines
     *
     * @param sessionId session id object
     * @return session output list
     */
    public static List<SessionOutput> getOutput(Connection con, Long sessionId) {
        List<SessionOutput> outputList = new ArrayList<SessionOutput>();


        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionId);
        if (userSessionsOutput != null) {


            for (Long key : userSessionsOutput.getSessionOutputMap().keySet()) {

                //get output chars and set to output
                try {
                    StringBuilder sb = userSessionsOutput.getSessionOutputMap().get(key);
                    if (sb != null) {
                        SessionOutput sessionOutput = new SessionOutput();
                        sessionOutput.setSessionId(sessionId);
                        sessionOutput.setHostSystemId(key);

                        sessionOutput.setOutput(sb.toString());

                        if (StringUtils.isNotEmpty(sessionOutput.getOutput())) {
                            outputList.add(sessionOutput);

                            if ("true".equals(enableAudit)) {
                                SessionAuditDB.insertTerminalLog(con, sessionOutput);
                            }

                            userSessionsOutput.getSessionOutputMap().put(key, new StringBuilder());
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        }


        return outputList;
    }


}
