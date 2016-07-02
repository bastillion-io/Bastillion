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

import com.keybox.manage.db.ProfileDB;
import com.keybox.manage.db.ProfileSystemsDB;
import com.keybox.manage.db.SystemDB;
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.RefreshAuthKeyUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import java.util.List;

/**
 * Action to assign systems to profiles
 */
@InterceptorRef("keyboxStack")
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

        if (systemSelectId != null) {
            ProfileSystemsDB.setSystemsForProfile(profile.getId(), systemSelectId);
        }
        RefreshAuthKeyUtil.refreshProfileSystems(profile.getId());
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
