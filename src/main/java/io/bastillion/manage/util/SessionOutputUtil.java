/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.db.SessionAuditDB;
import io.bastillion.manage.model.AuditWrapper;
import io.bastillion.manage.model.SessionOutput;
import io.bastillion.manage.model.User;
import io.bastillion.manage.model.UserSessionsOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility to is used to store the output for a session until the ajax call that brings it to the screen
 */
public class SessionOutputUtil {

    private static Logger log = LoggerFactory.getLogger(SessionOutputUtil.class);

    private static Map<Long, UserSessionsOutput> userSessionsOutputMap = new ConcurrentHashMap<>();
    public final static boolean enableInternalAudit = "true".equals(AppConfig.getProperty("enableInternalAudit"));
    private static Gson gson = new GsonBuilder().registerTypeAdapter(AuditWrapper.class, new SessionOutputSerializer()).create();
    private static Logger systemAuditLogger = LoggerFactory.getLogger("io.bastillion.manage.util.SystemAudit");

    private SessionOutputUtil() {
    }

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
     * @param instanceId id of host system instance
     */
    public static void removeOutput(Long sessionId, Integer instanceId) {

        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionId);
        if (userSessionsOutput != null) {
            userSessionsOutput.getSessionOutputMap().remove(instanceId);
        }
    }

    /**
     * adds a new output
     *
     * @param sessionOutput session output object
     */
    public static void addOutput(SessionOutput sessionOutput) {

        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionOutput.getSessionId());
        if (userSessionsOutput == null) {
            userSessionsOutputMap.put(sessionOutput.getSessionId(), new UserSessionsOutput());
            userSessionsOutput = userSessionsOutputMap.get(sessionOutput.getSessionId());
        }
        userSessionsOutput.getSessionOutputMap().put(sessionOutput.getInstanceId(), sessionOutput);


    }


    /**
     * adds a new output
     *
     * @param sessionId    session id
     * @param instanceId id of host system instance
     * @param value        Array that is the source of characters
     * @param offset       The initial offset
     * @param count        The length
     */
    public static void addToOutput(Long sessionId, Integer instanceId, char value[], int offset, int count) {

        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionId);
        if (userSessionsOutput != null) {
            userSessionsOutput.getSessionOutputMap().get(instanceId).getOutput().append(value, offset, count);
        }

    }


    /**
     * returns list of output lines
     *
     * @param sessionId session id object
     * @param user user auth object
     * @return session output list
     */
    public static List<SessionOutput> getOutput(Connection con, Long sessionId, User user) {
        List<SessionOutput> outputList = new ArrayList<>();

        UserSessionsOutput userSessionsOutput = userSessionsOutputMap.get(sessionId);
        if (userSessionsOutput != null) {

            for (Integer key : userSessionsOutput.getSessionOutputMap().keySet()) {

                //get output chars and set to output
                try {
                    SessionOutput sessionOutput = userSessionsOutput.getSessionOutputMap().get(key);
                    if (sessionOutput!=null && sessionOutput.getOutput() != null
                            && StringUtils.isNotEmpty(sessionOutput.getOutput())) {

                        outputList.add(sessionOutput);

                        //send to audit logger
                        systemAuditLogger.info(gson.toJson(new AuditWrapper(user, sessionOutput)));

                        if(enableInternalAudit) {
                            SessionAuditDB.insertTerminalLog(con, sessionOutput);
                        }

                        userSessionsOutput.getSessionOutputMap().put(key, new SessionOutput(sessionId, sessionOutput));
                    }
                } catch (Exception ex) {
                    log.error(ex.toString(), ex);
                }

            }

        }


        return outputList;
    }


}
