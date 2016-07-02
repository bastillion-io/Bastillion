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

import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.ScriptDB;
import com.keybox.manage.model.Script;
import com.keybox.manage.model.SortedSet;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Action to manage scripts
 */
@InterceptorRef("keyboxStack")
public class ScriptAction extends ActionSupport implements ServletRequestAware {

    SortedSet sortedSet = new SortedSet();
    Script script=new Script();
    HttpServletRequest servletRequest;

    @Action(value = "/admin/viewScripts",
            results = {
                    @Result(name = "success", location = "/admin/view_scripts.jsp")
            }
    )
    public String viewScripts() {
        Long userId= AuthUtil.getUserId(servletRequest.getSession());
        sortedSet = ScriptDB.getScriptSet(sortedSet, userId);

        return SUCCESS;
    }


    @Action(value = "/admin/saveScript",
            results = {
                    @Result(name = "input", location = "/admin/view_scripts.jsp"),
                    @Result(name = "success", location = "/admin/viewScripts.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type="redirect")
            }
    )
    public String saveScript() {
        Long userId= AuthUtil.getUserId(servletRequest.getSession());
        if (script.getId() != null) {
            ScriptDB.updateScript(script, userId);
        } else {
            ScriptDB.insertScript(script, userId);
        }
        return SUCCESS;
    }

    @Action(value = "/admin/deleteScript",
            results = {
                    @Result(name = "success", location = "/admin/viewScripts.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type="redirect")
            }
    )
    public String deleteScript() {

        Long userId= AuthUtil.getUserId(servletRequest.getSession());
        if (script.getId() != null) {
            ScriptDB.deleteScript(script.getId(), userId);
        }
        return SUCCESS;
    }


    /**
     * Validates all fields for adding a user
     */
    public void validateSaveScript() {
        if (script == null
                || script.getDisplayNm() == null
                || script.getDisplayNm().trim().equals("")) {
            addFieldError("script.displayNm", "Required");
        }

        if (script == null
                || script.getScript() == null
                || script.getScript().trim().equals("")
                || (new Script()).getScript().trim().equals(script.getScript().trim())
                ) {
            addFieldError("script.script", "Required");
        }

        if (!this.getFieldErrors().isEmpty()) {
            Long userId= AuthUtil.getUserId(servletRequest.getSession());
            sortedSet = ScriptDB.getScriptSet(sortedSet, userId);
        }

    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}
