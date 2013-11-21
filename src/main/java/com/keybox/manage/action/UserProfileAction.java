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
import com.keybox.manage.db.UserDB;
import com.keybox.manage.db.UserProfileDB;
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.User;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Action to assign profiles to users
 */
public class UserProfileAction extends ActionSupport {

    List<Profile> profileList = new ArrayList<Profile>();
    User user;
    Long profileId;


    @Action(value = "/manage/viewUserProfiles",
            results = {
                    @Result(name = "success", location = "/manage/view_user_profiles.jsp")
            }
    )
    public String viewUserProfiles() {
        if (user != null && user.getId() != null) {
            user = UserDB.getUser(user.getId());
            profileList = ProfileDB.getAllProfiles();
        }
        return SUCCESS;
    }


    @Action(value = "/manage/addProfileToUser",
            results = {
                    @Result(name = "success", location = "/manage/viewUserProfiles.action?user.id=${user.id}", type = "redirect")
            }
    )
    public String addProfileToUser() {

        if (profileId != null) {
            UserProfileDB.addProfileToUser(profileId, user.getId());
        }

        return SUCCESS;
    }

    @Action(value = "/manage/deleteProfileFromUser",
            results = {
                    @Result(name = "success", location = "/manage/viewUserProfiles.action?user.id=${user.id}", type = "redirect")
            }
    )
    public String deleteProfileFromUser() {

        UserProfileDB.deleteProfileFromUser(profileId, user.getId());

        return SUCCESS;
    }

    public List<Profile> getProfileList() {
        return profileList;
    }

    public void setProfileList(List<Profile> profileList) {
        this.profileList = profileList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
}
