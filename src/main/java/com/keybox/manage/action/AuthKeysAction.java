package com.keybox.manage.action;

import com.keybox.manage.db.GenerateAuthKeysDB;
import com.keybox.manage.db.PrivateKeyDB;
import com.keybox.manage.model.SystemKeyGenStatus;
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
    String password;
    SystemKeyGenStatus pendingSystemStatus;
    SystemKeyGenStatus currentSystemStatus;
    List<SystemKeyGenStatus> systemStatusList;


    @Action(value = "/manage/genAuthKeyForSystem",
            results = {
                    @Result(name = "success", location = "/manage/key_gen_status.jsp")
            }
    )
    public String genAuthKeyForSystem() {

        if (pendingSystemStatus != null && pendingSystemStatus.getId() != null) {

            //get key gen status and a
            currentSystemStatus = GenerateAuthKeysDB.getSystemKeyGen(pendingSystemStatus.getId());


            //try and sftp key to remote server
            currentSystemStatus = SSHUtil.authAndAddPubKey(currentSystemStatus, password,
                    PrivateKeyDB.getPassphrase());


            GenerateAuthKeysDB.updateSystemKeyGen(currentSystemStatus);


            if (SystemKeyGenStatus.AUTH_FAIL_STATUS.equals(currentSystemStatus.getStatusCd())) {
                pendingSystemStatus = currentSystemStatus;

            } else {
                pendingSystemStatus = GenerateAuthKeysDB.getNextPendingSystem();
            }

        } else {
            currentSystemStatus = null;
            pendingSystemStatus = null;
        }

        systemStatusList = GenerateAuthKeysDB.getAllSystemKeyGen();
        return SUCCESS;
    }


    @Action(value = "/manage/getNextPendingSystem",
            results = {
                    @Result(name = "success", location = "/manage/key_gen_status.jsp")
            }
    )
    public String getNextPendingSystem() {
        currentSystemStatus = GenerateAuthKeysDB.getSystemKeyGen(pendingSystemStatus.getId());
        currentSystemStatus.setErrorMsg("Auth fail");
        currentSystemStatus.setStatusCd(SystemKeyGenStatus.GENERIC_FAIL_STATUS);


        GenerateAuthKeysDB.updateSystemKeyGen(currentSystemStatus);
        pendingSystemStatus = GenerateAuthKeysDB.getNextPendingSystem();


        systemStatusList = GenerateAuthKeysDB.getAllSystemKeyGen();
        return SUCCESS;
    }

    @Action(value = "/manage/selectSystemsForAuthKeys",
            results = {
                    @Result(name = "success", location = "/manage/key_gen_status.jsp")
            }
    )
    public String selectSystemsForAuthKeys() {

        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            systemStatusList = GenerateAuthKeysDB.setInitialSystemKeyGen(GenerateAuthKeysDB.findAuthKeysForSystems(systemSelectId));
            //set first system to set auth keys
            pendingSystemStatus = GenerateAuthKeysDB.getNextPendingSystem();


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

            systemStatusList = GenerateAuthKeysDB.setInitialSystemKeyGen(GenerateAuthKeysDB.findAuthKeysForUsers(userSelectId));

            //set first system to set auth keys
            pendingSystemStatus = GenerateAuthKeysDB.getNextPendingSystem();

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

    public List<SystemKeyGenStatus> getSystemStatusList() {
        return systemStatusList;
    }

    public void setSystemStatusList(List<SystemKeyGenStatus> systemStatusList) {
        this.systemStatusList = systemStatusList;
    }

    public SystemKeyGenStatus getPendingSystemStatus() {
        return pendingSystemStatus;
    }

    public void setPendingSystemStatus(SystemKeyGenStatus pendingSystemStatus) {
        this.pendingSystemStatus = pendingSystemStatus;
    }

    public SystemKeyGenStatus getCurrentSystemStatus() {
        return currentSystemStatus;
    }

    public void setCurrentSystemStatus(SystemKeyGenStatus currentSystemStatus) {
        this.currentSystemStatus = currentSystemStatus;
    }
}
