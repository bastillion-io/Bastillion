/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.action;

import com.keybox.manage.db.ScriptDB;
import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.model.Script;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import java.util.List;

public class ExecScriptAction extends ActionSupport {


    List<Long> systemSelectId;
    List<Long> profileSelectId;
    String password;

    SystemStatus pendingSystemStatus;
    SystemStatus currentSystemStatus;
    List<SystemStatus> systemStatusList;

    Script script = new Script();

    @Action(value = "/manage/genExecScriptForSystem",
            results = {
                    @Result(name = "success", location = "/manage/exec_script_status.jsp")
            }

    )
    public String genExecScriptForSystem() {
        script = ScriptDB.getScript(script.getId());

        if (pendingSystemStatus != null && pendingSystemStatus.getId() != null) {

            //get status
            currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId());
            //if initial status run script
            if (SystemStatus.INITIAL_STATUS.equals(currentSystemStatus.getStatusCd())
                    || SystemStatus.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())
                    ) {

                //try and run script
                currentSystemStatus = SSHUtil.execScriptOnSystem(currentSystemStatus, password, script);
            }
            if (SystemStatus.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())) {
                pendingSystemStatus = currentSystemStatus;

            } else {

                pendingSystemStatus = SystemStatusDB.getNextPendingSystem();
                if(pendingSystemStatus==null  && !SystemStatusDB.isFinished()){
                    pendingSystemStatus=currentSystemStatus;
                }

            }

        } else {
            //done
            currentSystemStatus = null;
            pendingSystemStatus = null;
        }

        systemStatusList = SystemStatusDB.getAllSystemStatus();
        return SUCCESS;
    }


    @Action(value = "/manage/getNextPendingSystemForExecScript",
            results = {
                    @Result(name = "success", location = "/manage/exec_script_status.jsp")
            }
    )
    public String getNextPendingSystemForExecScript() {
        currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId());
        currentSystemStatus.setErrorMsg("Auth fail");
        currentSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);


        SystemStatusDB.updateSystemStatus(currentSystemStatus);
        pendingSystemStatus = SystemStatusDB.getNextPendingSystem();


        systemStatusList = SystemStatusDB.getAllSystemStatus();
        script = ScriptDB.getScript(script.getId());
        return SUCCESS;
    }

    @Action(value = "/manage/selectSystemsForExecScript",
            results = {
                    @Result(name = "success", location = "/manage/exec_script_status.jsp")
            }
    )
    public String selectSystemsForExecScript() {

        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            systemStatusList = SystemStatusDB.setInitialSystemStatus(SystemStatusDB.findAuthKeysForSystems(systemSelectId));
            //set first system to set auth keys
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem();
            script = ScriptDB.getScript(script.getId());

        }
        return SUCCESS;
    }


    @Action(value = "/manage/selectProfilesForExecScript",
            results = {
                    @Result(name = "success", location = "/manage/exec_script_status.jsp")
            }
    )
    public String selectProfilesForExecScript() {


        if (profileSelectId != null && !profileSelectId.isEmpty()) {

            systemStatusList = SystemStatusDB.setInitialSystemStatus(SystemStatusDB.findAuthKeysForProfile(profileSelectId));

            //set first system to set auth keys
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem();
            script = ScriptDB.getScript(script.getId());

        }


        return SUCCESS;
    }


    public List<Long> getSystemSelectId() {
        return systemSelectId;
    }

    public void setSystemSelectId(List<Long> systemSelectId) {
        this.systemSelectId = systemSelectId;
    }

    public List<Long> getProfileSelectId() {
        return profileSelectId;
    }

    public void setProfileSelectId(List<Long> profileSelectId) {
        this.profileSelectId = profileSelectId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SystemStatus getPendingSystemStatus() {
        return pendingSystemStatus;
    }

    public void setPendingSystemStatus(SystemStatus pendingSystemStatus) {
        this.pendingSystemStatus = pendingSystemStatus;
    }

    public SystemStatus getCurrentSystemStatus() {
        return currentSystemStatus;
    }

    public void setCurrentSystemStatus(SystemStatus currentSystemStatus) {
        this.currentSystemStatus = currentSystemStatus;
    }

    public List<SystemStatus> getSystemStatusList() {
        return systemStatusList;
    }

    public void setSystemStatusList(List<SystemStatus> systemStatusList) {
        this.systemStatusList = systemStatusList;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
