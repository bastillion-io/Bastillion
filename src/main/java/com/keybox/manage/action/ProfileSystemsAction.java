/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.action;


import com.keybox.manage.db.ProfileDB;
import com.keybox.manage.db.ProfileSystemsDB;
import com.keybox.manage.db.SystemDB;
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.SortedSet;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import java.util.List;
import java.util.Map;

/**
 * Action to assign systems to profiles
 */
public class ProfileSystemsAction extends ActionSupport {

    Profile profile;
    SortedSet sortedSet=new SortedSet();
    List<Long> systemSelectId;


    @Action(value = "/manage/viewProfileSystems",
            results = {
                    @Result(name = "success", location = "/manage/view_profile_systems.jsp")
            }
    )
    public String viewProfileSystems() {
        if (profile != null && profile.getId() != null) {
            profile = ProfileDB.getProfile(profile.getId());
            sortedSet = SystemDB.getSystemSet(sortedSet);
        }
        return SUCCESS;
    }


    @Action(value = "/manage/assignSystemsToProfile",
            results = {
                    @Result(name = "success", location = "/manage/viewProfiles.action", type = "redirect")
            }
    )
    public String assignSystemsToProfile() {

        ProfileSystemsDB.deleteAllSystemsFromProfile(profile.getId());
        if(systemSelectId!=null){
            for (Long hostSystemId : systemSelectId) {
                ProfileSystemsDB.addSystemToProfile(profile.getId(), hostSystemId);
            }
        }
        return SUCCESS;
    }


    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
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
}
