package com.keybox.manage.action;


import com.keybox.manage.db.ProfileDB;
import com.keybox.manage.db.UserDB;
import com.keybox.manage.db.UserProfileDB;
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.User;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import java.util.List;

/**
 * Action to assign profiles to users
 */
public class UserProfileAction extends ActionSupport {

    List<Profile> profileList;
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
