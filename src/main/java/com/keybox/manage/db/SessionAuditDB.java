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
package com.keybox.manage.db;


import com.keybox.common.util.AppConfig;
import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.SessionAudit;
import com.keybox.manage.model.SessionOutput;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.DBUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DB class to store terminal logs for sessions
 */
public class SessionAuditDB {

    private static Logger log = LoggerFactory.getLogger(SessionAuditDB.class);

    public static final String USER_ID = "user_id";
    public static final String FILTER_BY_USER_ID = USER_ID;
    public static final String FILTER_BY_SYSTEM_ID = "system_id";
    
    public static final String SORT_BY_FIRST_NM = "first_nm";
    public static final String SORT_BY_LAST_NM = "last_nm";
    public static final String SORT_BY_EMAIL = "email";
    public static final String SORT_BY_USERNAME = "username";
    public static final String SESSION_TM = "session_tm";
    public static final String SORT_BY_SESSION_TM = SESSION_TM;

    private SessionAuditDB() {
    }


    /**
     * deletes audit history for users if after time set in properties file
     *
     * @param con DB connection
     */
    public static void deleteAuditHistory(Connection con) {

        try {

            //delete logs with no terminal entries
            PreparedStatement stmt = con.prepareStatement("delete from session_log where id not in (select session_id from terminal_log)");
            stmt.execute();


            //take today's date and subtract how many days to keep history
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, (-1 * Integer.parseInt(AppConfig.getProperty("deleteAuditLogAfter")))); //subtract
            java.sql.Date date = new java.sql.Date(cal.getTimeInMillis());


            stmt = con.prepareStatement("delete from session_log where session_tm < ?");
            stmt.setDate(1, date);
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

    }


    /**
     * returns sessions based on sort order defined
     *
     * @param sortedSet object that defines sort order
     * @return session list
     */
    public static SortedSet getSessions(SortedSet sortedSet) {
        //get db connection
        Connection con = null;
        List<SessionAudit> outputList = new LinkedList<>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = " order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }


        String sql = "select * from session_log, users where users.id= session_log.user_id ";
        sql+= StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_USER_ID)) ? " and session_log.user_id=? " : "";
        sql+= StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM_ID)) ? " and session_log.id in ( select session_id from terminal_log where terminal_log.system_id=? ) " : "";
        sql+= orderBy;

        try {

            con = DBUtils.getConn();
            deleteAuditHistory(con);

            PreparedStatement stmt = con.prepareStatement(sql);
            int i=1;
            //set filters in prepared statement
            if(StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_USER_ID))){
                stmt.setLong(i++, Long.parseLong(sortedSet.getFilterMap().get(FILTER_BY_USER_ID)));
            }
            if(StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM_ID))){
                stmt.setLong(i, Long.parseLong(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM_ID)));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SessionAudit sessionAudit = new SessionAudit();
                sessionAudit.setId(rs.getLong("session_log.id"));
                sessionAudit.setSessionTm(rs.getTimestamp(SESSION_TM));
                sessionAudit.setUser(UserDB.getUser(con, rs.getLong(USER_ID)));
                outputList.add(sessionAudit);


            }


            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        sortedSet.setItemList(outputList);

        return sortedSet;


    }

    /**
     * insert new session record for user
     *
     * @param userId user id
     * @return session id
     */
    public static Long createSessionLog(Long userId) {
        //get db connection
        Connection con = DBUtils.getConn();

        Long sessionId = null;
        try {

            sessionId = createSessionLog(con, userId);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
        return sessionId;
    }

    /**
     * insert new session record for user
     *
     * @param con    DB connection
     * @param userId user id
     * @return session id
     */
    public static Long createSessionLog(Connection con, Long userId) {
        Long sessionId = null;
        try {

            //insert
            PreparedStatement stmt = con.prepareStatement("insert into session_log (user_id) values(?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, userId);
            stmt.execute();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs != null && rs.next()) {
                sessionId = rs.getLong(1);
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return sessionId;

    }


    /**
     * insert new terminal history for user
     *
     * @param sessionOutput output from session terminals
     * @return session id
     */
    public static void insertTerminalLog(SessionOutput sessionOutput) {
        //get db connection
        Connection con = DBUtils.getConn();

        try {

            insertTerminalLog(con, sessionOutput);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * insert new terminal history for user
     *
     * @param con           DB connection
     * @param sessionOutput output from session terminals
     * @return session id
     */
    public static void insertTerminalLog(Connection con, SessionOutput sessionOutput) {

        try {

            if (sessionOutput != null && sessionOutput.getSessionId() != null && sessionOutput.getInstanceId() != null && sessionOutput.getOutput() != null && !sessionOutput.getOutput().toString().equals("")) {
                //insert
                PreparedStatement stmt = con.prepareStatement("insert into terminal_log (session_id, instance_id, system_id, output) values(?,?,?,?)");
                stmt.setLong(1, sessionOutput.getSessionId());
                stmt.setLong(2, sessionOutput.getInstanceId());
                stmt.setLong(3, sessionOutput.getId());
                stmt.setString(4, sessionOutput.getOutput().toString());
                stmt.execute();
                DBUtils.closeStmt(stmt);
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

    }


    /**
     * returns terminal logs for user session for host system
     *
     * @param sessionId     session id
     * @param instanceId    instance id for terminal session
     * @return session output for session
     */
    public static List<SessionOutput> getTerminalLogsForSession(Long sessionId, Integer instanceId) {
        //get db connection
        Connection con = DBUtils.getConn();
        List<SessionOutput> outputList = null;

        try {
            outputList = getTerminalLogsForSession(con, sessionId, instanceId);


        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return outputList;
    }


    /**
     * returns terminal logs for user session for host system
     *
     * @param sessionId     session id
     * @param instanceId    instance id for terminal session
     * @return session output for session
     */
    public static List<SessionOutput> getTerminalLogsForSession(Connection con, Long sessionId, Integer instanceId) {

        List<SessionOutput> outputList = new LinkedList<>();
        try {
            PreparedStatement stmt = con.prepareStatement("select * from terminal_log where instance_id=? and session_id=? order by log_tm asc");
            stmt.setLong(1, instanceId);
            stmt.setLong(2, sessionId);
            ResultSet rs = stmt.executeQuery();
            StringBuilder outputBuilder = new StringBuilder("");
            while (rs.next()) {
                outputBuilder.append(rs.getString("output"));
            }

            String output = outputBuilder.toString();
            output = output.replaceAll("\\u0007|\u001B\\[K|\\]0;|\\[\\d\\d;\\d\\dm|\\[\\dm","");
            while (output.contains("\b")) {
                output = output.replaceFirst(".\b", "");
            }
            DBUtils.closeRs(rs);

            SessionOutput sessionOutput = new SessionOutput();
            sessionOutput.setSessionId(sessionId);
            sessionOutput.setInstanceId(instanceId);
            sessionOutput.getOutput().append(output);

            outputList.add(sessionOutput);


            DBUtils.closeRs(rs);


            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return outputList;

    }

    /**
     * returns terminal logs for user session for host system
     *
     * @param con       DB connection
     * @param sessionId session id
     * @return session output for session
     */
    public static List<HostSystem> getHostSystemsForSession(Connection con, Long sessionId) {

        List<HostSystem> hostSystemList = new ArrayList<>();
        try {
            PreparedStatement stmt = con.prepareStatement("select distinct instance_id, system_id from terminal_log where session_id=?");
            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HostSystem hostSystem = SystemDB.getSystem(con, rs.getLong("system_id"));
                hostSystem.setInstanceId(rs.getInt("instance_id"));
                hostSystemList.add(hostSystem);
            }

            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        return hostSystemList;

    }

    /**
     * returns a list of terminal sessions for session id
     *
     * @param sessionId session id
     * @return terminal sessions with host information
     */
    public static SessionAudit getSessionsTerminals(Long sessionId) {
        //get db connection
        Connection con = null;
        SessionAudit sessionAudit = new SessionAudit();


        String sql = "select * from session_log, users where users.id= session_log.user_id and session_log.id = ? ";
        try {

            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1, sessionId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sessionAudit.setId(rs.getLong("session_log.id"));
                sessionAudit.setSessionTm(rs.getTimestamp(SESSION_TM));
                sessionAudit.setUser(UserDB.getUser(con, rs.getLong(USER_ID)));
                sessionAudit.setHostSystemList(getHostSystemsForSession(con, sessionId));


            }


            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return sessionAudit;


    }

}
