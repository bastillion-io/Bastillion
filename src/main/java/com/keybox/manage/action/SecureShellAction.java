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
package com.keybox.manage.action;

import com.keybox.common.util.AppConfigLkup;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.*;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.SessionOutputUtil;
import com.keybox.manage.model.*;
import com.keybox.manage.util.SSHUtil;
import com.google.gson.Gson;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This action will create composite ssh terminals to be used
 */
public class SecureShellAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    List<SessionOutput> outputList;
    String command;
    HttpServletResponse servletResponse;
    HttpServletRequest servletRequest;
    Integer keyCode = null;
    List<Long> idList = new ArrayList<Long>();
    List<Long> systemSelectId;
    HostSystem currentSystemStatus;
    HostSystem pendingSystemStatus;
    String password;
    String passphrase;
    List<HostSystem> systemList = new ArrayList<HostSystem>();

    static Map<Long, UserSchSessions> userSchSessionMap = new ConcurrentHashMap<Long, UserSchSessions>();


    Script script = new Script();


    /**
     * Maps key press events to the ascii values
     */
    static Map<Integer, byte[]> keyMap = new HashMap<Integer, byte[]>();

    static {
        //ESC
        keyMap.put(27, new byte[]{(byte) 0x1b});
        //ENTER
        keyMap.put(13, new byte[]{(byte) 0x0d});
        //LEFT
        keyMap.put(37, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x44});
        //UP
        keyMap.put(38, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x41});
        //RIGHT
        keyMap.put(39, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x43});
        //DOWN
        keyMap.put(40, new byte[]{(byte) 0x1b, (byte) 0x4f, (byte) 0x42});
        //BS
        keyMap.put(8, new byte[]{(byte) 0x08});
        //TAB
        keyMap.put(9, new byte[]{(byte) 0x09});
        //CTR
        keyMap.put(17, new byte[]{});
        //CTR-A
        keyMap.put(65, new byte[]{(byte) 0x01});
        //CTR-B
        keyMap.put(66, new byte[]{(byte) 0x02});
        //CTR-C
        keyMap.put(67, new byte[]{(byte) 0x03});
        //CTR-D
        keyMap.put(68, new byte[]{(byte) 0x04});
        //CTR-E
        keyMap.put(69, new byte[]{(byte) 0x05});
        //CTR-F
        keyMap.put(70, new byte[]{(byte) 0x06});
        //CTR-G
        keyMap.put(71, new byte[]{(byte) 0x07});
        //CTR-H
        keyMap.put(72, new byte[]{(byte) 0x08});
        //CTR-I
        keyMap.put(73, new byte[]{(byte) 0x09});
        //CTR-J
        keyMap.put(74, new byte[]{(byte) 0x0A});
        //CTR-K
        keyMap.put(75, new byte[]{(byte) 0x0B});
        //CTR-L
        keyMap.put(76, new byte[]{(byte) 0x0C});
        //CTR-M
        keyMap.put(77, new byte[]{(byte) 0x0D});
        //CTR-N
        keyMap.put(78, new byte[]{(byte) 0x0E});
        //CTR-O
        keyMap.put(79, new byte[]{(byte) 0x0F});
        //CTR-P
        keyMap.put(80, new byte[]{(byte) 0x10});
        //CTR-Q
        keyMap.put(81, new byte[]{(byte) 0x11});
        //CTR-R
        keyMap.put(82, new byte[]{(byte) 0x12});
        //CTR-S
        keyMap.put(83, new byte[]{(byte) 0x13});
        //CTR-T
        keyMap.put(84, new byte[]{(byte) 0x14});
        //CTR-U
        keyMap.put(85, new byte[]{(byte) 0x15});
        //CTR-V
        keyMap.put(86, new byte[]{(byte) 0x16});
        //CTR-W
        keyMap.put(87, new byte[]{(byte) 0x17});
        //CTR-X
        keyMap.put(88, new byte[]{(byte) 0x18});
        //CTR-Y
        keyMap.put(89, new byte[]{(byte) 0x19});
        //CTR-Z
        keyMap.put(90, new byte[]{(byte) 0x1A});
        //CTR-[
        keyMap.put(219, new byte[]{(byte) 0x1B});
        //CTR-]
        keyMap.put(221, new byte[]{(byte) 0x1D});

    }


    /**
     * Runs commands across sessions
     */
    @Action(value = "/terms/runCmd")
    public String runCmd() {
        Long userId = AuthUtil.getUserId(servletRequest.getSession());
        if (userId != null) {
            try {
                //if id then write to single system output buffer
                if (idList != null && idList.size() > 0) {
                    for (Long id : idList) {
                        //get servletRequest.getSession() for user
                        UserSchSessions userSchSessions = userSchSessionMap.get(userId);
                        if (userSchSessions != null) {
                            SchSession schSession = userSchSessions.getSchSessionMap().get(id);
                            if (keyCode != null) {
                                if (keyMap.containsKey(keyCode)) {
                                    schSession.getCommander().write(keyMap.get(keyCode));
                                }
                            } else {
                                schSession.getCommander().print(command);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }else{
            AuthUtil.deleteAllSession(servletRequest.getSession());
        }

        return null;
    }

    /**
     * returns terminal output as a json string
     */
    @Action(value = "/terms/getOutputJSON")
    public String getOutputJSON() {


        Connection con = DBUtils.getConn();
        //this checks to see if session is valid
        Long userId = AuthDB.getUserIdByAuthToken(con, AuthUtil.getAuthToken(servletRequest.getSession()));
        if (userId != null) {
            //update timeout
            AuthUtil.setTimeout(servletRequest.getSession());
            List<SessionOutput> outputList = SessionOutputUtil.getOutput(con, userId);
            String json = new Gson().toJson(outputList);
            try {
                servletResponse.getOutputStream().write(json.getBytes());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }else{
            AuthUtil.deleteAllSession(servletRequest.getSession());
        }

        DBUtils.closeConn(con);
        return null;

    }


    /**
     * creates composite terminals if there are errors or authentication issues.
     */
    @Action(value = "/admin/createTerms",
            results = {
                    @Result(name = "success", location = "/admin/secure_shell.jsp")
            }
    )
    public String createTerms() {

        Long userId = AuthUtil.getUserId(servletRequest.getSession());
        Long sessionId = AuthUtil.getSessionId(servletRequest.getSession());
        if (pendingSystemStatus != null && pendingSystemStatus.getId() != null) {


            //get status
            currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId(), userId);
            //if initial status run script
            if (currentSystemStatus != null
                    && (HostSystem.INITIAL_STATUS.equals(currentSystemStatus.getStatusCd())
                    || HostSystem.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())
                    || HostSystem.PUBLIC_KEY_FAIL_STATUS.equals(currentSystemStatus.getStatusCd()))
                    ) {

                //set current session
                currentSystemStatus = SSHUtil.openSSHTermOnSystem(passphrase, password, userId, sessionId, currentSystemStatus, userSchSessionMap);

            }
            if (currentSystemStatus != null
                    && (HostSystem.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())
                    || HostSystem.PUBLIC_KEY_FAIL_STATUS.equals(currentSystemStatus.getStatusCd()))) {

                pendingSystemStatus = currentSystemStatus;

            } else {


                pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);
                //if success loop through systems until finished or need password
                while (pendingSystemStatus != null && currentSystemStatus != null && HostSystem.SUCCESS_STATUS.equals(currentSystemStatus.getStatusCd())) {
                    currentSystemStatus = SSHUtil.openSSHTermOnSystem(passphrase, password, userId, sessionId, pendingSystemStatus, userSchSessionMap);
                    pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);
                }


            }

        }
        if (SystemStatusDB.getNextPendingSystem(userId) == null) {


            //check user map
            if (userSchSessionMap != null && !userSchSessionMap.isEmpty()) {

                //get user sessions
                Map<Long, SchSession> schSessionMap = userSchSessionMap.get(userId).getSchSessionMap();


                for (SchSession schSession : schSessionMap.values()) {
                    //add to host system list
                    systemList.add(schSession.getHostSystem());
                    //run script it exists
                    if (script != null && script.getId() != null && script.getId() > 0) {
                        script = ScriptDB.getScript(script.getId(), userId);
                        BufferedReader reader = new BufferedReader(new StringReader(script.getScript()));
                        String line;
                        try {

                            while ((line = reader.readLine()) != null) {
                                schSession.getCommander().println(line);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                }
            }
        }


        return SUCCESS;
    }


    @Action(value = "/admin/getNextPendingSystemForTerms",
            results = {
                    @Result(name = "success", location = "/admin/secure_shell.jsp")
            }
    )
    public String getNextPendingSystemForTerms() {
        Long userId = AuthUtil.getUserId(servletRequest.getSession());
        currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId(), userId);
        currentSystemStatus.setErrorMsg("Auth fail");
        currentSystemStatus.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);


        SystemStatusDB.updateSystemStatus(currentSystemStatus, userId);
        pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

        return SUCCESS;
    }

    @Action(value = "/admin/selectSystemsForCompositeTerms",
            results = {
                    @Result(name = "success", location = "/admin/secure_shell.jsp")
            }
    )
    public String selectSystemsForCompositeTerms() {


        Long userId = AuthUtil.getUserId(servletRequest.getSession());
        //exit any previous terms
        exitTerms();
        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            SystemStatusDB.setInitialSystemStatus(systemSelectId, userId);
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

            AuthUtil.setSessionId(servletRequest.getSession(), SessionAuditDB.createSessionLog(userId));


        }
        return SUCCESS;
    }


    @Action(value = "/admin/exitTerms",
            results = {
                    @Result(name = "success", location = "/admin/menu.action", type = "redirect")

            }
    )
    public String exitTerms() {


        Long userId = AuthUtil.getUserId(servletRequest.getSession());
        //check user map
        if (userSchSessionMap != null && !userSchSessionMap.isEmpty()) {

            //get user servletRequest.getSession()s
            for (Long userKey : userSchSessionMap.keySet()) {
                UserSchSessions userSchSessions = userSchSessionMap.get(userKey);

                //get current time and subtract number of hours set to determine expire time
                Calendar expireTime = Calendar.getInstance();
                expireTime.add(Calendar.HOUR, (-1 * Integer.parseInt(AppConfigLkup.getProperty("timeoutSshAfter"))));//subtract hours to get expire time

                //if current user or session has timed out remove ssh session
                if (userId.equals(userKey) || userSchSessions.getStartTime().before(expireTime.getTime())) {
                    Map<Long, SchSession> schSessionMap = userSchSessionMap.get(userKey).getSchSessionMap();

                    for (Long sessionKey : schSessionMap.keySet()) {

                        SchSession schSession = schSessionMap.get(sessionKey);

                        //disconnect ssh session
                        schSession.getChannel().disconnect();
                        schSession.getSession().disconnect();
                        schSession.setChannel(null);
                        schSession.setSession(null);
                        schSession.setInputToChannel(null);
                        schSession.setCommander(null);
                        schSession.setOutFromChannel(null);
                        schSession = null;
                        //remove from map
                        schSessionMap.remove(sessionKey);

                    }


                    //clear and remove session map for user
                    schSessionMap.clear();
                    userSchSessionMap.remove(userKey);
                    SessionOutputUtil.removeUserSession(userKey);

                }
            }

        }


        return SUCCESS;
    }


    public List<SessionOutput> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<SessionOutput> outputList) {
        this.outputList = outputList;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public Integer getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(Integer keyCode) {
        this.keyCode = keyCode;
    }


    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }

    public List<Long> getSystemSelectId() {
        return systemSelectId;
    }

    public void setSystemSelectId(List<Long> systemSelectId) {
        this.systemSelectId = systemSelectId;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HostSystem getCurrentSystemStatus() {
        return currentSystemStatus;
    }

    public void setCurrentSystemStatus(HostSystem currentSystemStatus) {
        this.currentSystemStatus = currentSystemStatus;
    }

    public HostSystem getPendingSystemStatus() {
        return pendingSystemStatus;
    }

    public void setPendingSystemStatus(HostSystem pendingSystemStatus) {
        this.pendingSystemStatus = pendingSystemStatus;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public List<HostSystem> getSystemList() {
        return systemList;
    }

    public void setSystemList(List<HostSystem> systemList) {
        this.systemList = systemList;
    }

    public static Map<Long, UserSchSessions> getUserSchSessionMap() {
        return userSchSessionMap;
    }

    public static void setUserSchSessionMap(Map<Long, UserSchSessions> userSchSessionMap) {
        SecureShellAction.userSchSessionMap = userSchSessionMap;
    }
}


