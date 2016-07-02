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
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.Script;
import com.keybox.manage.model.SortedSet;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;


/**
 * Action to manage profiles
 */
@InterceptorRef("keyboxStack")
public class ProfileAction extends ActionSupport {

    SortedSet sortedSet = new SortedSet();
    Profile profile = new Profile();
    Script script=null;


    @Action(value = "/manage/viewProfiles",
            results = {
                    @Result(name = "success", location = "/manage/view_profiles.jsp")
            }
    )
    public String viewSystems() {

        sortedSet = ProfileDB.getProfileSet(sortedSet);

        return SUCCESS;
    }

    @Action(value = "/manage/saveProfile",
            results = {
                    @Result(name = "input", location = "/manage/view_profiles.jsp"),
                    @Result(name = "success", location = "/manage/viewProfiles.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String saveProfile() {

        if (profile.getId() != null) {
            ProfileDB.updateProfile(profile);
        } else {
            ProfileDB.insertProfile(profile);
        }
        return SUCCESS;
    }


    @Action(value = "/manage/deleteProfile",
            results = {
                    @Result(name = "success", location = "/manage/viewProfiles.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String deleteProfile() {

        if (profile.getId() != null) {
            ProfileDB.deleteProfile(profile.getId());
        }
        return SUCCESS;
    }

    /**
     * validate save profile
     */
    public void validateSaveProfile() {
        if (profile == null
                || profile.getNm() == null
                || profile.getNm().trim().equals("")) {
            addFieldError("profile.nm", "Required");
        }

        if (!this.getFieldErrors().isEmpty()) {
            sortedSet = ProfileDB.getProfileSet(sortedSet);
        }

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

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

}
