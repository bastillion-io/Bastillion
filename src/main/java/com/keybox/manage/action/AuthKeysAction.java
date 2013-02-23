/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.action;

import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Action to generate and distribute auth keys for systems or users
 */
public class AuthKeysAction extends ActionSupport {


    List<Long> systemSelectId;
    List<Long> userSelectId;
    List<Long> profileSelectId;
    String password;
    SystemStatus pendingSystemStatus;
    SystemStatus currentSystemStatus;
    List<SystemStatus> systemStatusList;


    @Action(value = "/manage/genAuthKeyForSystem",
            results = {
                    @Result(name = "success", location = "/manage/key_gen_status.jsp")
            }
    )
    public String genAuthKeyForSystem() {

        if (pendingSystemStatus != null && pendingSystemStatus.getId() != null) {

            //get key gen status and a
            currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId());


            //try and sftp key to remote server
            currentSystemStatus = SSHUtil.authAndAddPubKey(currentSystemStatus, password);


            SystemStatusDB.updateSystemStatus(currentSystemStatus);


            if (SystemStatus.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())) {
                pendingSystemStatus = currentSystemStatus;

            } else {
                pendingSystemStatus = SystemStatusDB.getNextPendingSystem();
            }

        } else {
            currentSystemStatus = null;
            pendingSystemStatus = null;
        }

        systemStatusList = SystemStatusDB.getAllSystemStatus();
        return SUCCESS;
    }


    @Action(value = "/manage/getNextPendingSystem",
            results = {
                    @Result(name = "success", location = "/manage/key_gen_status.jsp")
            }
    )
    public String getNextPendingSystem() {
        currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId());
        currentSystemStatus.setErrorMsg("Auth fail");
        currentSystemStatus.setStatusCd(SystemStatus.GENERIC_FAIL_STATUS);


        SystemStatusDB.updateSystemStatus(currentSystemStatus);
        pendingSystemStatus = SystemStatusDB.getNextPendingSystem();


        systemStatusList = SystemStatusDB.getAllSystemStatus();
        return SUCCESS;
    }

    @Action(value = "/manage/selectSystemsForAuthKeys",
            results = {
                    @Result(name = "success", location = "/manage/key_gen_status.jsp")
            }
    )
    public String selectSystemsForAuthKeys() {

        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            systemStatusList = SystemStatusDB.setInitialSystemStatus(SystemStatusDB.findAuthKeysForSystems(systemSelectId));
            //set first system to set auth keys
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem();


        }
        return SUCCESS;
    }

    @Action(value = "/manage/selectUsersForAuthKeys",
            results = {
                    @Result(name = "success", location = "/manage/key_gen_status.jsp")
            }
    )
    public String selectUsersForAuthKeys() {


        if (userSelectId != null && !userSelectId.isEmpty()) {

            systemStatusList = SystemStatusDB.setInitialSystemStatus(SystemStatusDB.findAuthKeysForUsers(userSelectId));

            //set first system to set auth keys
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem();

        }


        return SUCCESS;
    }

    @Action(value = "/manage/selectProfilesForAuthKeys",
            results = {
                    @Result(name = "success", location = "/manage/key_gen_status.jsp")
            }
    )
    public String selectProfilesForAuthKeys() {


        if (profileSelectId != null && !profileSelectId.isEmpty()) {

            systemStatusList = SystemStatusDB.setInitialSystemStatus(SystemStatusDB.findAuthKeysForProfile(profileSelectId));

            //set first system to set auth keys
            pendingSystemStatus = SystemStatusDB.getNextPendingSystem();

        }


        return SUCCESS;
    }

    public List<Long> getSystemSelectId() {
        return systemSelectId;
    }

    public void setSystemSelectId(List<Long> systemSelectId) {
        this.systemSelectId = systemSelectId;
    }

    public List<Long> getUserSelectId() {
        return userSelectId;
    }

    public void setUserSelectId(List<Long> userSelectId) {
        this.userSelectId = userSelectId;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<SystemStatus> getSystemStatusList() {
        return systemStatusList;
    }

    public void setSystemStatusList(List<SystemStatus> systemStatusList) {
        this.systemStatusList = systemStatusList;
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

    public List<Long> getProfileSelectId() {
        return profileSelectId;
    }

    public void setProfileSelectId(List<Long> profileSelectId) {
        this.profileSelectId = profileSelectId;
    }
}
