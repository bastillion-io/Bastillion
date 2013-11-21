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

import com.keybox.manage.db.SessionAuditDB;
import com.keybox.manage.model.SessionOutput;
import com.keybox.manage.model.UserSessionsOutput;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility to is used to store the output for a session until the ajax call that brings it to the screen
 */
public class SessionOutputUtil {


    private static Map<Long, UserSessionsOutput> userSessionsOutputMap = new ConcurrentHashMap<Long, UserSessionsOutput>();


    /**
     * removes session for user
     *
     * @param userId user id
     */
    public static void removeUserSession(Long userId) {
        UserSessionsOutput userSessionsOutput =userSessionsOutputMap.get(userId);
        if(userSessionsOutput!=null){
            userSessionsOutput.getSessionOutputMap().clear();
        }
        userSessionsOutputMap.remove(userId);

    }

    /**
     * removes session output for host system
     *
     * @param userId user id
     * @param hostSystemId host system id
     */
    public static void removeOutput(Long userId, Long hostSystemId) {

        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(userId);
        if (userSessionsOutput != null) {
            userSessionsOutput.getSessionOutputMap().remove(hostSystemId);
        }
    }

    /**
     * adds a new output
     *
     * @param userId user id
     * @param sessionOutput session output object
     */
    public static void addOutput(Long userId, SessionOutput sessionOutput) {

        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(userId);
        if (userSessionsOutput == null) {
            userSessionsOutputMap.put(userId, new UserSessionsOutput());
            userSessionsOutput = userSessionsOutputMap.get(userId);
        }
        userSessionsOutput.getSessionOutputMap().put(sessionOutput.getHostSystemId(), sessionOutput);


    }


    /**
     * adds a new output
     *
     * @param userId       user id
     * @param hostSystemId host system id
     * @param c            character
     */
    public static void addCharToOutput(Long userId, Long hostSystemId, char c) {


        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(userId);
        if (userSessionsOutput != null) {
            SessionOutput sessionOutput = userSessionsOutput.getSessionOutputMap().get(hostSystemId);
            if (sessionOutput != null) {
                sessionOutput.setOutput(sessionOutput.getOutput() + Character.toString(c));
            }
        }

    }


    /**
     * returns list of output lines
     *
     * @param userId user id
     * @return sessionId session id object
     */
    public static List<SessionOutput> getOutput(Connection con,Long userId) {
        List<SessionOutput> outputList = new ArrayList<SessionOutput>();


        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(userId);
        if (userSessionsOutput != null) {


            for (Long key : userSessionsOutput.getSessionOutputMap().keySet()) {

                //get output chars and set to output
                try {
                    SessionOutput sessionOutput = (SessionOutput) BeanUtils.cloneBean(userSessionsOutput.getSessionOutputMap().get(key));

                    outputList.add(sessionOutput);

                    SessionAuditDB.insertTerminalLog(con, sessionOutput);

                    userSessionsOutput.getSessionOutputMap().get(key).setOutput("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        }


        return outputList;
    }


}
