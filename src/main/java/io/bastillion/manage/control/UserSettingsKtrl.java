/**
 *    Copyright (C) 2015 Loophole, LLC
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

import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.db.PrivateKeyDB;
import io.bastillion.manage.db.UserThemeDB;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.UserSettings;
import io.bastillion.manage.util.PasswordUtil;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Action for user settings
 */
public class UserSettingsKtrl extends BaseKontroller {

    public static final String REQUIRED = "Required";
    @Model(name = "themeMap")
    static Map<String, String> themeMap1 = new LinkedHashMap<>(Map.ofEntries(
            entry("Tango", "#2e3436,#cc0000,#4e9a06,#c4a000,#3465a4,#75507b,#06989a,#d3d7cf,#555753,#ef2929,#8ae234,#fce94f,#729fcf,#ad7fa8,#34e2e2,#eeeeec"),
            entry("XTerm", "#000000,#cd0000,#00cd00,#cdcd00,#0000ee,#cd00cd,#00cdcd,#e5e5e5,#7f7f7f,#ff0000,#00ff00,#ffff00,#5c5cff,#ff00ff,#00ffff,#ffffff")
    ));
    @Model(name = "planeMap")
    static Map<String, String> planeMap1 = new LinkedHashMap<>(Map.ofEntries(
            entry("Black on light yellow", "#FFFFDD,#000000"),
            entry("Black on white", "#FFFFFF,#000000"),
            entry("Gray on black", "#000000,#AAAAAA"),
            entry("Green on black", "#000000,#00FF00"),
            entry("White on black", "#000000,#FFFFFF")
    ));
    @Model(name = "publicKey")
    static String publicKey = PrivateKeyDB.getApplicationKey().getPublicKey();

    @Model(name = "auth")
    Auth auth;
    @Model(name = "userSettings")
    UserSettings userSettings;



    public UserSettingsKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/admin/userSettings", method = MethodType.GET)
    public String userSettings() {
        userSettings = UserThemeDB.getTheme(AuthUtil.getUserId(getRequest().getSession()));
        return "/admin/user_settings.html";
    }

    @Kontrol(path = "/admin/passwordSubmit", method = MethodType.POST)
    public String passwordSubmit() {
        String retVal = "/admin/user_settings.html";

        if (!auth.getPassword().equals(auth.getPasswordConfirm())) {
            addError("Passwords do not match");

        } else if (!PasswordUtil.isValid(auth.getPassword())) {
            addError(PasswordUtil.PASSWORD_REQ_ERROR_MSG);

        } else {
            auth.setAuthToken(AuthUtil.getAuthToken(getRequest().getSession()));

            if (AuthDB.updatePassword(auth)) {
                retVal = "redirect:/admin/menu.html";
            } else {
                addError("Current password is invalid");
            }
        }

        return retVal;
    }

    @Kontrol(path = "/admin/themeSubmit", method = MethodType.POST)
    public String themeSubmit() {
        userSettings.setTheme(userSettings.getTheme());
        userSettings.setPlane(userSettings.getPlane());
        UserThemeDB.saveTheme(AuthUtil.getUserId(getRequest().getSession()), userSettings);

        return "redirect:/admin/menu.html";
    }

    /**
     * Validates fields for password submit
     */
    @Validate(input = "/admin/user_settings.html")
    public void validatePasswordSubmit() {
        if (auth.getPassword() == null ||
                auth.getPassword().trim().equals("")) {
            addFieldError("auth.password", REQUIRED);
        }
        if (auth.getPasswordConfirm() == null ||
                auth.getPasswordConfirm().trim().equals("")) {
            addFieldError("auth.passwordConfirm", REQUIRED);
        }
        if (auth.getPrevPassword() == null ||
                auth.getPrevPassword().trim().equals("")) {
            addFieldError("auth.prevPassword", REQUIRED);
        }
    }
}
