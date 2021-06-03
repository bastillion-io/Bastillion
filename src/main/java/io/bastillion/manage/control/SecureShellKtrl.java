/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
 */
package io.bastillion.manage.control;

import com.jcraft.jsch.ChannelShell;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.*;
import io.bastillion.manage.model.*;
import io.bastillion.manage.util.SSHUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.base.BaseKontroller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This action will create composite ssh terminals to be used
 */
public class SecureShellKtrl extends BaseKontroller {

    static Map<Long, UserSchSessions> userSchSessionMap = new ConcurrentHashMap<>();
    private static Logger log = LoggerFactory.getLogger(SecureShellKtrl.class);
    @Model(name = "systemSelectId")
    List<Long> systemSelectId = new ArrayList<>();
    @Model(name = "currentSystemStatus")
    HostSystem currentSystemStatus;
    @Model(name = "pendingSystemStatus")
    HostSystem pendingSystemStatus;
    @Model(name = "password")
    String password;
    @Model(name = "passphrase")
    String passphrase;
    @Model(name = "id")
    Integer id;
    @Model(name = "systemList")
    List<HostSystem> systemList = new ArrayList<>();
    @Model(name = "allocatedSystemList")
    List<HostSystem> allocatedSystemList = new ArrayList<>();
    @Model(name = "userSettings")
    UserSettings userSettings;
    @Model(name = "script")
    Script script = new Script();


    public SecureShellKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    public static Map<Long, UserSchSessions> getUserSchSessionMap() {
        return userSchSessionMap;
    }

    public static void setUserSchSessionMap(Map<Long, UserSchSessions> userSchSessionMap) {
        SecureShellKtrl.userSchSessionMap = userSchSessionMap;
    }

    /**
     * creates composite terminals if there are errors or authentication issues.
     */
    @Kontrol(path = "/admin/createTerms", method = MethodType.POST)
    public String createTerms() {

        Long userId = AuthUtil.getUserId(getRequest().getSession());
        Long sessionId = AuthUtil.getSessionId(getRequest().getSession());
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
        //set system list if no pending systems
        if (SystemStatusDB.getNextPendingSystem(userId) == null) {
            setSystemList(userId, sessionId);

            //set allocated systems for connect to
            SortedSet sortedSet = new SortedSet();
            sortedSet.setOrderByField(SystemDB.SORT_BY_NAME);
            if (Auth.MANAGER.equals(AuthUtil.getUserType(getRequest().getSession()))) {
                sortedSet = SystemDB.getSystemSet(sortedSet);
            } else {
                sortedSet = SystemDB.getUserSystemSet(sortedSet, userId);
            }
            if (sortedSet != null && sortedSet.getItemList() != null) {
                allocatedSystemList = (List<HostSystem>) sortedSet.getItemList();
            }
            //set theme
            this.userSettings = UserThemeDB.getTheme(userId);

        }


        return "/admin/secure_shell.html";
    }

    @Kontrol(path = "/admin/getNextPendingSystemForTerms", method = MethodType.GET)
    public String getNextPendingSystemForTerms() {
        Long userId = AuthUtil.getUserId(getRequest().getSession());
        currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId(), userId);
        currentSystemStatus.setErrorMsg("Auth fail");
        currentSystemStatus.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);


        SystemStatusDB.updateSystemStatus(currentSystemStatus, userId);
        SystemDB.updateSystem(currentSystemStatus);

        pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

        //set system list if no pending systems
        if (pendingSystemStatus == null) {
            setSystemList(userId, AuthUtil.getSessionId(getRequest().getSession()));
        }

        return "/admin/secure_shell.html";
    }

    @Kontrol(path = "/admin/selectSystemsForCompositeTerms", method = MethodType.GET)
    public String selectSystemsForCompositeTerms() {


        Long userId = AuthUtil.getUserId(getRequest().getSession());

        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            SystemStatusDB.setInitialSystemStatus(systemSelectId, userId, AuthUtil.getUserType(getRequest().getSession()));
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

            User user = UserDB.getUser(userId);
            user.setIpAddress(AuthUtil.getClientIPAddress(getRequest()));

            AuthUtil.setSessionId(getRequest().getSession(), SessionAuditDB.createSessionLog(user));
        }

        return "/admin/secure_shell.html";
    }

    @Kontrol(path = "/admin/exitTerms", method = MethodType.GET)
    public String exitTerms() {

        return "redirect:/admin/menu.html";
    }

    @Kontrol(path = "/admin/disconnectTerm", method = MethodType.GET)
    public String disconnectTerm() {
        Long sessionId = AuthUtil.getSessionId(getRequest().getSession());
        if (SecureShellKtrl.getUserSchSessionMap() != null) {
            UserSchSessions userSchSessions = SecureShellKtrl.getUserSchSessionMap().get(sessionId);
            if (userSchSessions != null) {
                try {
                    SchSession schSession = userSchSessions.getSchSessionMap().get(id);

                    //disconnect ssh session
                    if (schSession != null) {
                        if (schSession.getChannel() != null)
                            schSession.getChannel().disconnect();
                        if (schSession.getSession() != null)
                            schSession.getSession().disconnect();
                        schSession.setChannel(null);
                        schSession.setSession(null);
                        schSession.setInputToChannel(null);
                        schSession.setCommander(null);
                        schSession.setOutFromChannel(null);
                    }
                    //remove from map
                    userSchSessions.getSchSessionMap().remove(id);
                } catch (Exception ex) {
                    log.error(ex.toString(), ex);
                }
            }
        }

        return null;
    }

    @Kontrol(path = "/admin/createSession", method = MethodType.GET)
    public String createSession() {

        Long userId = AuthUtil.getUserId(getRequest().getSession());

        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            SystemStatusDB.setInitialSystemStatus(systemSelectId, userId, AuthUtil.getUserType(getRequest().getSession()));

            pendingSystemStatus = SystemStatusDB.getNextPendingSystem(userId);

            createTerms();

        }

        return null;
    }

    @Kontrol(path = "/admin/setPtyType", method = MethodType.GET)
    public String setPtyType() {

        Long sessionId = AuthUtil.getSessionId(getRequest().getSession());
        if (SecureShellKtrl.getUserSchSessionMap() != null) {
            UserSchSessions userSchSessions = SecureShellKtrl.getUserSchSessionMap().get(sessionId);
            if (userSchSessions != null && userSchSessions.getSchSessionMap() != null) {

                SchSession schSession = userSchSessions.getSchSessionMap().get(id);

                ChannelShell channel = (ChannelShell) schSession.getChannel();
                channel.setPtySize((int) Math.floor(userSettings.getPtyWidth() / 8.0000), (int) Math.floor(userSettings.getPtyHeight() / 14.4166), userSettings.getPtyWidth(), userSettings.getPtyHeight());
                schSession.setChannel(channel);

            }

        }

        return null;
    }

    /**
     * set system list once all connections have been attempted
     *
     * @param userId    user id
     * @param sessionId session id
     */
    private void setSystemList(Long userId, Long sessionId) {


        //check user map
        if (userSchSessionMap != null && !userSchSessionMap.isEmpty() && userSchSessionMap.get(sessionId) != null) {

            //get user sessions
            Map<Integer, SchSession> schSessionMap = userSchSessionMap.get(sessionId).getSchSessionMap();


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
                        log.error(e.toString(), e);

                    }
                }
            }
        }

    }
}


