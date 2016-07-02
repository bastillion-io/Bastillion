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
import com.keybox.manage.db.ProfileDB;
import com.keybox.manage.db.ScriptDB;
import com.keybox.manage.db.SystemDB;
import com.keybox.manage.db.UserProfileDB;
import com.keybox.manage.model.*;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to manage systems
 */
@InterceptorRef("keyboxStack")
public class SystemAction extends ActionSupport implements ServletRequestAware {

    public static final String REQUIRED = "Required";
    SortedSet sortedSet = new SortedSet();
    HostSystem hostSystem = new HostSystem();
    Script script = null;
    HttpServletRequest servletRequest;
    String password;
    String passphrase;
    List<Profile> profileList= new ArrayList<>();


    @Action(value = "/admin/viewSystems",
            results = {
                    @Result(name = "success", location = "/admin/view_systems.jsp")
            }
    )
    public String viewAdminSystems() {
        Long userId = AuthUtil.getUserId(servletRequest.getSession());

        if (Auth.MANAGER.equals(AuthUtil.getUserType(servletRequest.getSession()))) {
            sortedSet = SystemDB.getSystemSet(sortedSet);
            profileList=ProfileDB.getAllProfiles();
        } else {
            sortedSet = SystemDB.getUserSystemSet(sortedSet, userId);
            profileList= UserProfileDB.getProfilesByUser(userId);
        }
        if (script != null && script.getId() != null) {
            script = ScriptDB.getScript(script.getId(), userId);
        }

        return SUCCESS;
    }

    @Action(value = "/manage/viewSystems",
            results = {
                    @Result(name = "success", location = "/manage/view_systems.jsp")
            }
    )
    public String viewManageSystems() {

        sortedSet = SystemDB.getSystemSet(sortedSet); 

        return SUCCESS;
    }

    @Action(value = "/manage/saveSystem",
            results = {
                    @Result(name = "input", location = "/manage/view_systems.jsp"),
                    @Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String saveSystem() {
        String retVal=SUCCESS;

        hostSystem = SSHUtil.authAndAddPubKey(hostSystem, passphrase, password);

        if (hostSystem.getId() != null) {
            SystemDB.updateSystem(hostSystem);
        } else {
            hostSystem.setId(SystemDB.insertSystem(hostSystem));
        }
        sortedSet = SystemDB.getSystemSet(sortedSet);

        if (!HostSystem.SUCCESS_STATUS.equals(hostSystem.getStatusCd())) {
            retVal=INPUT;
        }
        return retVal;
    }

    @Action(value = "/manage/deleteSystem",
            results = {
                    @Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String deleteSystem() {

        if (hostSystem.getId() != null) {
            SystemDB.deleteSystem(hostSystem.getId());
        }
        return SUCCESS;
    }

    /**
     * Validates all fields for adding a host system
     */
    public void validateSaveSystem() {
        if (hostSystem == null
                || hostSystem.getDisplayNm() == null
                || hostSystem.getDisplayNm().trim().equals("")) {
            addFieldError("hostSystem.displayNm", REQUIRED);
        }
        if (hostSystem == null
                || hostSystem.getUser() == null
                || hostSystem.getUser().trim().equals("")) {
            addFieldError("hostSystem.user", REQUIRED);
        }
        if (hostSystem == null
                || hostSystem.getHost() == null
                || hostSystem.getHost().trim().equals("")) {
            addFieldError("hostSystem.host", REQUIRED);
        }
        if (hostSystem == null
                || hostSystem.getPort() == null) {
            addFieldError("hostSystem.port", REQUIRED);
        } else if (!(hostSystem.getPort() > 0)) {
            addFieldError("hostSystem.port", "Invalid");
        }

        if (hostSystem == null
                || hostSystem.getAuthorizedKeys() == null
                || hostSystem.getAuthorizedKeys().trim().equals("") || hostSystem.getAuthorizedKeys().trim().equals("~")) {
            addFieldError("hostSystem.authorizedKeys", REQUIRED);
        }

        if (!this.getFieldErrors().isEmpty()) {

            sortedSet = SystemDB.getSystemSet(sortedSet);
        }

    }

   

    public List<Profile> getProfileList() {
        return profileList;
    }

    public void setProfileList(List<Profile> profileList) {
        this.profileList = profileList;
    }

    public HostSystem getHostSystem() {
        return hostSystem;
    }

    public void setHostSystem(HostSystem hostSystem) {
        this.hostSystem = hostSystem;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

}
