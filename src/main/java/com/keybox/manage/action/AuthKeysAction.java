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

import com.keybox.manage.db.SystemStatusDB;
import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import java.util.List;

/**
 * Action to generate and distribute auth keys for systems or users
 */
public class AuthKeysAction extends ActionSupport {


    List<Long> systemSelectId;
    List<Long> userSelectId;
    List<Long> profileSelectId;
    String password;
    static String passphrase;
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

            //get key gen status and set current system
            currentSystemStatus = SystemStatusDB.getSystemStatus(pendingSystemStatus.getId());

            //check to see if passphrase has been provided
            String  passphrase = EncryptionUtil.decrypt(PrivateKeyDB.getPassphrase());
            //set use provided passphrase
            if (passphrase == null || passphrase.trim().equals("")) {
                passphrase = this.getPassphrase();
            }

            //try and sftp key to remote server
            currentSystemStatus = SSHUtil.authAndAddPubKey(currentSystemStatus, passphrase, password);

            if (SystemStatus.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd()) || SystemStatus.PUBLIC_KEY_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())) {
                pendingSystemStatus = currentSystemStatus;

            } else {
                pendingSystemStatus = SystemStatusDB.getNextPendingSystem();


                //if success loop through systems until finished or need password
                while (pendingSystemStatus != null && currentSystemStatus != null && SystemStatus.SUCCESS_STATUS.equals(currentSystemStatus.getStatusCd())) {
                    currentSystemStatus = SSHUtil.authAndAddPubKey(pendingSystemStatus, passphrase, null);
                    pendingSystemStatus = SystemStatusDB.getNextPendingSystem();
                }

            }
            passphrase = null;

        }
        //if no pending systems null passphrase
        if (SystemStatusDB.getNextPendingSystem() == null) {
            this.passphrase = null;
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
            //check to see if passphrase has been provided
            String  passphrase = EncryptionUtil.decrypt(PrivateKeyDB.getPassphrase());
            //set use provided passphrase
            if (pendingSystemStatus!=null && (passphrase == null || passphrase.trim().equals(""))) {
                pendingSystemStatus.setStatusCd(SystemStatus.PUBLIC_KEY_FAIL_STATUS);
            }


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
            //check to see if passphrase has been provided
            String  passphrase = EncryptionUtil.decrypt(PrivateKeyDB.getPassphrase());
            //set use provided passphrase
            if (pendingSystemStatus!=null && (passphrase == null || passphrase.trim().equals(""))) {
                pendingSystemStatus.setStatusCd(SystemStatus.PUBLIC_KEY_FAIL_STATUS);
            }


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
            //check to see if passphrase has been provided
            String  passphrase = EncryptionUtil.decrypt(PrivateKeyDB.getPassphrase());
            //set use provided passphrase
            if (pendingSystemStatus!=null && (passphrase == null || passphrase.trim().equals(""))) {
                pendingSystemStatus.setStatusCd(SystemStatus.PUBLIC_KEY_FAIL_STATUS);
            }


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

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
}
