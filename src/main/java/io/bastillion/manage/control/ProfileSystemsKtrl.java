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
