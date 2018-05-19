/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
 */
package com.keybox.manage.control;

import com.keybox.manage.db.ProfileDB;
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.SortedSet;
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
