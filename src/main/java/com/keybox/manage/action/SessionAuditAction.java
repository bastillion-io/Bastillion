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

import com.keybox.manage.db.SessionAuditDB;
import com.keybox.manage.model.SessionAudit;
import com.keybox.manage.model.SortedSet;
import com.google.gson.Gson;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletResponseAware;
import javax.servlet.http.HttpServletResponse;

/**
 * Action to audit sessions and terminal history
 */
public class SessionAuditAction extends ActionSupport implements ServletResponseAware {


    SortedSet sortedSet=new SortedSet();
    Long sessionId;
    Long hostSystemId;
    SessionAudit sessionAudit;
    HttpServletResponse servletResponse;

    @Action(value = "/manage/viewSessions",
            results = {
                    @Result(name = "success", location = "/manage/view_sessions.jsp")
            }
    )
    public String viewSessions() {

        if (sortedSet.getOrderByField() == null || sortedSet.getOrderByField().trim().equals("")) {
            sortedSet.setOrderByField(SessionAuditDB.SORT_BY_SESSION_TM);
            sortedSet.setOrderByDirection("desc");
        }


        sortedSet= SessionAuditDB.getSessions(sortedSet);


        return SUCCESS;

    }


    @Action(value = "/manage/getTermsForSession",
            results = {
                    @Result(name = "success", location = "/manage/view_terms.jsp")
            }
    )
    public String getTermsForSession() {


        sessionAudit=SessionAuditDB.getSessionsTerminals(sessionId);

        return SUCCESS;

    }

    @Action(value = "/manage/getJSONTermOutputForSession")
    public String getJSONTermOutputForSession() {

        String json=new Gson().toJson(SessionAuditDB.getTerminalLogsForSession(sessionId, hostSystemId));
        try {
            servletResponse.getOutputStream().write(json.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public SessionAudit getSessionAudit() {
        return sessionAudit;
    }

    public void setSessionAudit(SessionAudit sessionAudit) {
        this.sessionAudit = sessionAudit;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public Long getHostSystemId() {
        return hostSystemId;
    }

    public void setHostSystemId(Long hostSystemId) {
        this.hostSystemId = hostSystemId;
    }
}
