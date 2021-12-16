/**
 *    Copyright (C) 2015 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.manage.db.ProfileDB;
import io.bastillion.manage.db.UserDB;
import io.bastillion.manage.db.UserProfileDB;
import io.bastillion.manage.model.Profile;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.RefreshAuthKeyUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.base.BaseKontroller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to assign users to profiles
 */
public class ProfileUsersKtrl extends BaseKontroller {

    @Model(name = "profile")
    Profile profile;
    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "userSelectId")
    List<Long> userSelectId = new ArrayList<>();

    public ProfileUsersKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/manage/viewProfileUsers", method = MethodType.GET)
    public String viewProfileUsers() {
        if (profile != null && profile.getId() != null) {
            profile = ProfileDB.getProfile(profile.getId());
            sortedSet = UserDB.getAdminUserSet(sortedSet, profile.getId());
        }
        return "/manage/view_profile_users.html";
    }

    @Kontrol(path = "/manage/assignUsersToProfile", method = MethodType.POST)
    public String assignSystemsToProfile() {

        if (userSelectId != null) {
            UserProfileDB.setUsersForProfile(profile.getId(), userSelectId);
        }
        RefreshAuthKeyUtil.refreshProfileSystems(profile.getId());
        return "redirect:/manage/viewProfiles.ktrl";
    }


}
