/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.manage.db.ProfileDB;
import io.bastillion.manage.db.ProfileSystemsDB;
import io.bastillion.manage.db.SystemDB;
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
 * Action to assign systems to profiles
 */
public class ProfileSystemsKtrl extends BaseKontroller {

    @Model(name = "profile")
    Profile profile;
    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "systemSelectId")
    List<Long> systemSelectId = new ArrayList<>();


    public ProfileSystemsKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }


    @Kontrol(path = "/manage/viewProfileSystems", method = MethodType.GET)
    public String viewProfileSystems() {
        if (profile != null && profile.getId() != null) {
            profile = ProfileDB.getProfile(profile.getId());
            sortedSet = SystemDB.getSystemSet(sortedSet, profile.getId());
        }
        return "/manage/view_profile_systems.html";
    }


    @Kontrol(path = "/manage/assignSystemsToProfile", method = MethodType.POST)
    public String assignSystemsToProfile() {

        if (systemSelectId != null) {
            ProfileSystemsDB.setSystemsForProfile(profile.getId(), systemSelectId);
        }
        RefreshAuthKeyUtil.refreshProfileSystems(profile.getId());
        return "redirect:/manage/viewProfiles.ktrl";
    }


}
