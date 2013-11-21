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

        String retVal = updateKeysForSystems();
        if (this.hasActionErrors()) {
            profileList = ProfileDB.getAllProfiles();
            sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);
        }


        return retVal;
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

        String retVal = updateKeysForSystems();
        if (this.hasActionErrors()) {
            profileList = ProfileDB.getAllProfiles();
            sortedSet = PublicKeyDB.getPublicKeySet(sortedSet);
        }

        return retVal;
    }

    /**
     * update keys for systems
     *
     * @return success or fail value
     */
    private String updateKeysForSystems() {
        String retVal = SUCCESS;
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


        for (HostSystem hostSystem : hostSystemList) {
            hostSystem = SSHUtil.authAndAddPubKey(hostSystem, null, null);

            SystemDB.updateSystem(hostSystem);

            if (this.getActionErrors().isEmpty() && !HostSystem.SUCCESS_STATUS.equals(hostSystem.getStatusCd())) {

                addActionError("Public keys failed for some systems. Please refresh 'Failed' systems <a href='../manage/viewSystems.action'>here</a>");
                retVal = INPUT;
            }

        }
        return retVal;

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


}
