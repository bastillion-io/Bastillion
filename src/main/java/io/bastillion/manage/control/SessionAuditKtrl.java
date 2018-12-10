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

import com.google.gson.Gson;
import io.bastillion.manage.db.SessionAuditDB;
import io.bastillion.manage.db.SystemDB;
import io.bastillion.manage.db.UserDB;
import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.model.SessionAudit;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.model.User;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.base.BaseKontroller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Action to audit sessions and terminal history
 */
public class SessionAuditKtrl extends BaseKontroller {

    private static Logger log = LoggerFactory.getLogger(SessionAuditKtrl.class);

    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "sessionId")
    Long sessionId;
    @Model(name = "instanceId")
    Integer instanceId;
    @Model(name = "sessionAudit")
    SessionAudit sessionAudit;
    @Model(name = "systemList")
    List<HostSystem> systemList = SystemDB.getSystemSet(new SortedSet(SystemDB.SORT_BY_NAME)).getItemList();
    @Model(name = "userList")
    List<User> userList = UserDB.getUserSet(new SortedSet(SessionAuditDB.SORT_BY_USERNAME)).getItemList();

    public SessionAuditKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/manage/viewSessions", method = MethodType.GET)
    public String viewSessions() {

        if (sortedSet.getOrderByField() == null || sortedSet.getOrderByField().trim().equals("")) {
            sortedSet.setOrderByField(SessionAuditDB.SORT_BY_SESSION_TM);
            sortedSet.setOrderByDirection("desc");
        }


        sortedSet = SessionAuditDB.getSessions(sortedSet);


        return "/manage/view_sessions.html";

    }


    @Kontrol(path = "/manage/getTermsForSession", method = MethodType.GET)
    public String getTermsForSession() {

        sessionAudit = SessionAuditDB.getSessionsTerminals(sessionId);
        return "/manage/view_terms.html";

    }

    @Kontrol(path = "/manage/getJSONTermOutputForSession", method = MethodType.GET)
    public String getJSONTermOutputForSession() {

        String json = new Gson().toJson(SessionAuditDB.getTerminalLogsForSession(sessionId, instanceId));
        try {
            getResponse().getOutputStream().write(json.getBytes());
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }

        return null;

    }


}
