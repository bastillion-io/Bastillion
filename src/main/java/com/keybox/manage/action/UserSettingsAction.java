/**
 * Copyright 2015 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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

import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.db.UserThemeDB;
import com.keybox.manage.model.Auth;
import com.keybox.manage.model.UserSettings;
import com.keybox.manage.util.PasswordUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Action for user settings
 */
@InterceptorRef("keyboxStack")
public class UserSettingsAction extends ActionSupport implements ServletRequestAware {

    public static final String REQUIRED = "Required";
    HttpServletRequest servletRequest;
    Auth auth;
    UserSettings userSettings;

    @Action(value = "/admin/userSettings",
            results = {
                    @Result(name = "success", location = "/admin/user_settings.jsp")
            }
    )
    public String userSettings() {
        userSettings = UserThemeDB.getTheme(AuthUtil.getUserId(servletRequest.getSession()));
        return SUCCESS;
    }

    @Action(value = "/admin/passwordSubmit",
            results = {
                    @Result(name = "input", location = "/admin/user_settings.jsp"),
                    @Result(name = "success", location = "/admin/menu.action", type = "redirect")
            }
    )
    public String passwordSubmit() {
        String retVal = INPUT;

        if (!auth.getPassword().equals(auth.getPasswordConfirm())) {
            addActionError("Passwords do not match");

        } else if (!PasswordUtil.isValid(auth.getPassword())) {
            addActionError(PasswordUtil.PASSWORD_REQ_ERROR_MSG);

        } else {
            auth.setAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));

            if (AuthDB.updatePassword(auth)) {
                retVal = SUCCESS;
            } else {
                addActionError("Current password is invalid");
            }
        }


        return retVal;
    }


    @Action(value = "/admin/themeSubmit",
            results = {
                    @Result(name = "success", location = "/admin/menu.action", type = "redirect")
            }
    )
    public String themeSubmit() {

        UserThemeDB.saveTheme(AuthUtil.getUserId(servletRequest.getSession()), userSettings);

        return SUCCESS;
    }

    /**
     * Validates fields for password submit
     */
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


    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }
}
