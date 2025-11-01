/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;

/**
 * Action to audit sessions and terminal history
 */
public class SessionAuditKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(SessionAuditKtrl.class);

    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "sessionId")
    Long sessionId;
    @Model(name = "instanceId")
    Integer instanceId;
    @Model(name = "sessionAudit")
    SessionAudit sessionAudit;
    @Model(name = "systemList")
    List<HostSystem> systemList;
    @Model(name = "userList")
    List<User> userList;

    public SessionAuditKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/manage/viewSessions", method = MethodType.GET)
    public String viewSessions() throws ServletException {

        if (sortedSet.getOrderByField() == null || sortedSet.getOrderByField().trim().equals("")) {
            sortedSet.setOrderByField(SessionAuditDB.SORT_BY_SESSION_TM);
            sortedSet.setOrderByDirection("desc");
        }
        try {
            systemList = SystemDB.getSystemSet(new SortedSet(SystemDB.SORT_BY_NAME)).getItemList();
            userList = UserDB.getUserSet(new SortedSet(SessionAuditDB.SORT_BY_USERNAME)).getItemList();
            sortedSet = SessionAuditDB.getSessions(sortedSet);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        return "/manage/view_sessions.html";

    }


    @Kontrol(path = "/manage/getTermsForSession", method = MethodType.GET)
    public String getTermsForSession() throws ServletException {
        try {
            sessionAudit = SessionAuditDB.getSessionsTerminals(sessionId);
        } catch (SQLException | GeneralSecurityException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }
        return "/manage/view_terms.html";
    }

    @Kontrol(path = "/manage/getJSONTermOutputForSession", method = MethodType.GET)
    public String getJSONTermOutputForSession() throws ServletException {

        try {
            String json = new Gson().toJson(SessionAuditDB.getTerminalLogsForSession(sessionId, instanceId));
            getResponse().getOutputStream().write(json.getBytes());
        } catch (SQLException | GeneralSecurityException | IOException ex) {
            log.error(ex.toString(), ex);
            throw new ServletException(ex.toString(), ex);
        }

        return null;
    }
}
