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
import com.keybox.manage.db.*;
import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.PublicKey;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.SSHUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to generate and distribute auth keys for systems or users
 */
@SuppressWarnings("unchecked")
public class AuthKeysAction extends ActionSupport implements ServletRequestAware {


    HttpServletRequest servletRequest;
    List<Profile> profileList;
    PublicKey publicKey;
    SortedSet sortedSet = new SortedSet();
    List<Long> systemSelectId;

    HostSystem pendingSystem =null;
    HostSystem hostSystem=new HostSystem();

    String password;
    String passphrase;

    @Action(value = "/manage/viewKeys",
            results = {
                    @Result(name = "success", location = "/manage/view_keys.jsp")
            }
    )
    public String viewKeys() {

        profileList = ProfileDB.getAllProfiles();
        sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);
        return SUCCESS;
    }

    @Action(value = "/manage/savePublicKey",
            results = {
                    @Result(name = "input", location = "/manage/view_keys.jsp"),
                    @Result(name = "success", location = "/manage/viewKeys.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String savePublicKeys() {

        if (publicKey.getId() != null) {
            PublicKeyDB.updatePublicKey(publicKey);
        } else {
            PublicKeyDB.insertPublicKey(publicKey);
        }

        return SUCCESS;
    }

    @Action(value = "/manage/deletePublicKey",
            results = {
                    @Result(name = "input", location = "/manage/view_keys.jsp"),
                    @Result(name = "success", location = "/manage/viewKeys.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String deletePublicKey() {

        if (publicKey.getId() != null) {
            PublicKeyDB.deletePublicKey(publicKey.getId());
        }

        return SUCCESS;
    }


    @Action(value = "/manage/distributeKeysByProfile",
            results = {
                    @Result(name = "success", location = "/manage/distribute_keys.jsp")
            }
    )
    public String distributeKeysByProfile() {

        profileList = ProfileDB.getAllProfiles();
        return SUCCESS;
    }

    @Action(value = "/manage/distributeKeysBySystem",
            results = {
                    @Result(name = "success", location = "/manage/distribute_keys.jsp")
            }
    )
    public String distributeKeysBySystem() {

        sortedSet = SystemDB.getSystemSet(sortedSet);
        return SUCCESS;
    }


    @Action(value = "/manage/selectProfileForAuthKeys",
            results = {
                    @Result(name = "success", location = "/manage/view_systems.jsp")
            }
    )
    public String selectProfileForAuthKeys() {

        Long userId = AuthUtil.getUserId(servletRequest.getSession());
        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
        //get all host systems if no profile
        if (publicKey.getProfile() != null && publicKey.getProfile().getId() != null) {
            hostSystemList = ProfileSystemsDB.getSystemsByProfile(publicKey.getProfile().getId());
            //get host system for profile
        } else {
            sortedSet = SystemDB.getSystemSet(new SortedSet());
            if (sortedSet != null && sortedSet.getItemList() != null) {
                hostSystemList = (ArrayList<HostSystem>) sortedSet.getItemList();
            }
        }
        if (!hostSystemList.isEmpty()) {

            SystemStatusDB.setInitialSystemStatusByHostSystem(hostSystemList, userId);
            //set first system to set auth keys
            pendingSystem = SystemStatusDB.getNextPendingSystem(userId);

            if (pendingSystem != null) {
                pendingSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            }
        }
        sortedSet = SystemStatusDB.getSortedSetStatus(userId);


        return SUCCESS;
    }


    @Action(value = "/manage/selectSystemsForAuthKeys",
            results = {
                    @Result(name = "success", location = "/manage/view_systems.jsp")
            }
    )
    public String selectSystemsForAuthKeys() {
        Long userId = AuthUtil.getUserId(servletRequest.getSession());


        if (systemSelectId != null && !systemSelectId.isEmpty()) {

            SystemStatusDB.setInitialSystemStatus(systemSelectId, userId);
            //set first system to set auth keys
            pendingSystem = SystemStatusDB.getNextPendingSystem(userId);

            if (pendingSystem != null) {
                pendingSystem.setStatusCd(HostSystem.PUBLIC_KEY_FAIL_STATUS);
            }


        }


        sortedSet = SystemStatusDB.getSortedSetStatus(userId);
        return SUCCESS;
    }

    @Action(value = "/manage/genAuthKeyForSystem",
            results = {
                    @Result(name = "success", location = "/manage/view_systems.jsp")
            }
    )
    public String genAuthKeyForSystem() {

        Long userId = AuthUtil.getUserId(servletRequest.getSession());

        if (pendingSystem != null && pendingSystem.getId() != null) {

            //get key gen status and set current system
            hostSystem = SystemStatusDB.getSystemStatus(pendingSystem.getId(), userId);



            //try and sftp key to remote server
            hostSystem = SSHUtil.authAndAddPubKey(hostSystem, passphrase, password);

            SystemStatusDB.updateSystemStatus(hostSystem, userId);
            SystemDB.updateSystem(hostSystem);

            if (HostSystem.AUTH_FAIL_STATUS.equals(hostSystem.getStatusCd()) || HostSystem.PUBLIC_KEY_FAIL_STATUS.equals(hostSystem.getStatusCd())) {
                pendingSystem = hostSystem;

            } else {
                pendingSystem = SystemStatusDB.getNextPendingSystem(userId);


                //if success loop through systems until finished or need password
                while (pendingSystem != null && hostSystem != null && HostSystem.SUCCESS_STATUS.equals(hostSystem.getStatusCd())) {
                    hostSystem = SSHUtil.authAndAddPubKey(pendingSystem, passphrase, password);

                    SystemStatusDB.updateSystemStatus(hostSystem, userId);
                    SystemDB.updateSystem(hostSystem);

                    pendingSystem = SystemStatusDB.getNextPendingSystem(userId);
                }
            }

        }

        //finished - no more pending systems
        if(pendingSystem==null) {
            hostSystem=new HostSystem();
        }




        sortedSet = SystemStatusDB.getSortedSetStatus(userId);

        return SUCCESS;
    }


    @Action(value = "/manage/getNextPendingSystem",
            results = {
                    @Result(name = "success", location = "/manage/view_systems.jsp")
            }
    )
    public String getNextPendingSystem() {

        Long userId = AuthUtil.getUserId(servletRequest.getSession());

        pendingSystem = SystemStatusDB.getSystemStatus(pendingSystem.getId(), userId);
        pendingSystem.setErrorMsg("Auth fail");
        pendingSystem.setStatusCd(HostSystem.GENERIC_FAIL_STATUS);


        SystemStatusDB.updateSystemStatus(pendingSystem, userId);
        SystemDB.updateSystem(pendingSystem);


        pendingSystem = SystemStatusDB.getNextPendingSystem(userId);

        sortedSet = SystemStatusDB.getSortedSetStatus(userId);

        return SUCCESS;
    }




    /**
     * Validates all fields for adding a public key
     */
    public void validateSavePublicKeys() {
        if (publicKey == null
                || publicKey.getKeyNm() == null
                || publicKey.getKeyNm().trim().equals("")) {
            addFieldError("publicKey.keyNm", "Required");
        }
        if (publicKey == null
                || publicKey.getPublicKey() == null
                || publicKey.getPublicKey().trim().equals("")) {
            addFieldError("publicKey.publicKey", "Required");
        }

        if (!this.getFieldErrors().isEmpty()) {

            profileList = ProfileDB.getAllProfiles();
            sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);
        }

    }


    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public List<Profile> getProfileList() {
        return profileList;
    }

    public void setProfileList(List<Profile> profileList) {
        this.profileList = profileList;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public List<Long> getSystemSelectId() {
        return systemSelectId;
    }

    public void setSystemSelectId(List<Long> systemSelectId) {
        this.systemSelectId = systemSelectId;
    }


    public HostSystem getPendingSystem() {
        return pendingSystem;
    }

    public void setPendingSystem(HostSystem pendingSystem) {
        this.pendingSystem = pendingSystem;
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

    public HostSystem getHostSystem() {
        return hostSystem;
    }

    public void setHostSystem(HostSystem hostSystem) {
        this.hostSystem = hostSystem;
    }
}
