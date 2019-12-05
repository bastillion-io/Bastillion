/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * As a special exception, the copyright holders give permission to link the
 * code of portions of this program with the OpenSSL library under certain
 * conditions as described in each individual source file and distribute
 * linked combinations including the program with the OpenSSL library. You
 * must comply with the GNU Affero General Public License in all respects for
 * all of the code used other than as permitted herein. If you modify file(s)
 * with this exception, you may extend this exception to your version of the
 * file(s), but you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version. If you delete this
 * exception statement from all source files in the program, then also delete
 * it in the license file.
 */
package io.bastillion.manage.control;

import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.ProfileDB;
import io.bastillion.manage.db.ScriptDB;
import io.bastillion.manage.db.SystemDB;
import io.bastillion.manage.db.UserProfileDB;
import io.bastillion.manage.model.*;
import io.bastillion.manage.util.SSHUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to manage systems
 */
public class SystemKtrl extends BaseKontroller {

    public static final String REQUIRED = "Required";
    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "hostSystem")
    HostSystem hostSystem = new HostSystem();
    @Model(name = "script")
    Script script = null;
    @Model(name = "password")
    String password;
    @Model(name = "passphrase")
    String passphrase;
    @Model(name = "profileList")
    List<Profile> profileList = new ArrayList<>();

    public SystemKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/admin/viewSystems", method = MethodType.GET)
    public String viewAdminSystems() {
        Long userId = AuthUtil.getUserId(getRequest().getSession());

        if (Auth.MANAGER.equals(AuthUtil.getUserType(getRequest().getSession()))) {
            sortedSet = SystemDB.getSystemSet(sortedSet);
            profileList = ProfileDB.getAllProfiles();
        } else {
            sortedSet = SystemDB.getUserSystemSet(sortedSet, userId);
            profileList = UserProfileDB.getProfilesByUser(userId);
        }
        if (script != null && script.getId() != null) {
            script = ScriptDB.getScript(script.getId(), userId);
        }

        return "/admin/view_systems.html";
    }

    @Kontrol(path = "/manage/viewSystems", method = MethodType.GET)
    public String viewManageSystems() {
        sortedSet = SystemDB.getSystemSet(sortedSet);
        return "/manage/view_systems.html";
    }

    @Kontrol(path = "/manage/saveSystem", method = MethodType.POST)
    public String saveSystem() {
        String retVal = "redirect:/manage/viewSystems.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();

        hostSystem = SSHUtil.authAndAddPubKey(hostSystem, passphrase, password);

        if (hostSystem.getId() != null) {
            SystemDB.updateSystem(hostSystem);
        } else {
            hostSystem.setId(SystemDB.insertSystem(hostSystem));
        }
        sortedSet = SystemDB.getSystemSet(sortedSet);

        if (!HostSystem.SUCCESS_STATUS.equals(hostSystem.getStatusCd())) {
            retVal = "/manage/view_systems.html";
        }
        return retVal;
    }

    @Kontrol(path = "/manage/deleteSystem", method = MethodType.GET)
    public String deleteSystem() {

        if (hostSystem.getId() != null) {
            SystemDB.deleteSystem(hostSystem.getId());
        }
        ;
        return "redirect:/manage/viewSystems.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }

    /**
     * Validates all fields for adding a host system
     */
    @Validate(input = "/manage/view_systems.html")
    public void validateSaveSystem() {
        if (hostSystem == null
                || hostSystem.getDisplayNm() == null
                || hostSystem.getDisplayNm().trim().equals("")) {
            addFieldError("hostSystem.displayNm", REQUIRED);
        }
        if (hostSystem == null
                || hostSystem.getUser() == null
                || hostSystem.getUser().trim().equals("")) {
            addFieldError("hostSystem.user", REQUIRED);
        }
        if (hostSystem == null
                || hostSystem.getHost() == null
                || hostSystem.getHost().trim().equals("")) {
            addFieldError("hostSystem.host", REQUIRED);
        }
        if (hostSystem == null
                || hostSystem.getPort() == null) {
            addFieldError("hostSystem.port", REQUIRED);
        } else if (!(hostSystem.getPort() > 0)) {
            addFieldError("hostSystem.port", "Invalid");
        }

        if (hostSystem == null
                || hostSystem.getAuthorizedKeys() == null
                || hostSystem.getAuthorizedKeys().trim().equals("") || hostSystem.getAuthorizedKeys().trim().equals("~")) {
            addFieldError("hostSystem.authorizedKeys", REQUIRED);
        }

        if (!this.getFieldErrors().isEmpty()) {

            sortedSet = SystemDB.getSystemSet(sortedSet);
        }

    }

}
