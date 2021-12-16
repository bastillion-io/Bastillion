/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.manage.db.ProfileDB;
import io.bastillion.manage.model.Profile;
import io.bastillion.manage.model.SortedSet;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Action to manage profiles
 */
public class ProfileKtrl extends BaseKontroller {

    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "profile")
    Profile profile = new Profile();

    public ProfileKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }


    @Kontrol(path = "/manage/viewProfiles", method = MethodType.GET)
    public String viewSystems() {

        sortedSet = ProfileDB.getProfileSet(sortedSet);

        return "/manage/view_profiles.html";
    }

    @Kontrol(path = "/manage/saveProfile", method = MethodType.POST)
    public String saveProfile() {

        if (profile.getId() != null) {
            ProfileDB.updateProfile(profile);
        } else {
            ProfileDB.insertProfile(profile);
        }
        return "redirect:/manage/viewProfiles.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }


    @Kontrol(path = "/manage/deleteProfile", method = MethodType.GET)
    public String deleteProfile() {

        if (profile.getId() != null) {
            ProfileDB.deleteProfile(profile.getId());
        }
        return "redirect:/manage/viewProfiles.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }

    /**
     * validate save profile
     */
    @Validate(input = "/manage/view_profiles.html")
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


}
