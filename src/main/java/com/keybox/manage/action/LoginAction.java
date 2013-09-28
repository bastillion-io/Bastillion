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

import com.keybox.manage.db.AdminDB;
import com.keybox.manage.model.Login;
import com.keybox.manage.util.CookieUtil;
import com.keybox.manage.util.EncryptionUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Action to login to keybox
 */
public class LoginAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    HttpServletResponse servletResponse;
    HttpServletRequest servletRequest;
    Login login;

    @Action(value = "/login",
            results = {
                    @Result(name = "success", location = "/login.jsp")
            }
    )
    public String login() {

        return SUCCESS;
    }


    @Action(value = "/loginSubmit",
            results = {
                    @Result(name = "input", location = "/login.jsp"),
                    @Result(name = "change_password", location = "/manage/setPassword.action", type = "redirect"),
                    @Result(name = "success", location = "/manage/menu.jsp", type = "redirect")
            }
    )
    public String loginSubmit() {
        String retVal = SUCCESS;

        String authToken = AdminDB.loginAdmin(login);
        if (authToken != null) {
            CookieUtil.add(servletResponse, "authToken", EncryptionUtil.encrypt(authToken));

            //set timeout cookie
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss");
            Calendar timeout = Calendar.getInstance();
            timeout.add(Calendar.MINUTE, 15);
            CookieUtil.add(servletResponse, "timeout", sdf.format(timeout.getTime()));

        } else {
            addActionError("Invalid username and password combination");
            retVal = INPUT;
        }
        if (retVal == SUCCESS && "changeme".equals(login.getPassword())) {
            retVal = "change_password";
        }

        return retVal;
    }

    @Action(value = "/logout",
            results = {
                    @Result(name = "success", location = "/login.action", type = "redirect")
            }
    )
    public String logout() {
        CookieUtil.deleteAll(servletRequest, servletResponse);
        return SUCCESS;
    }

    @Action(value = "/manage/setPassword",
            results = {
                    @Result(name = "success", location = "/manage/set_password.jsp")
            }
    )
    public String setPassword() {

        return SUCCESS;
    }

    @Action(value = "/manage/passwordSubmit",
            results = {
                    @Result(name = "input", location = "/manage/set_password.jsp"),
                    @Result(name = "success", location = "/manage/menu.jsp", type = "redirect")
            }
    )
    public String passwordSubmit() {
        String retVal = SUCCESS;

        if (login.getPassword().equals(login.getPasswordConfirm())) {
            login.setAuthToken(EncryptionUtil.decrypt(CookieUtil.get(servletRequest, "authToken")));

            if (!AdminDB.updatePassword(login)) {
                addActionError("Current password is invalid");
                retVal = INPUT;
            }

        } else {
            addActionError("Passwords do not match");
            retVal = INPUT;
        }


        return retVal;
    }


    /**
     * Validates fields for login submit
     */
    public void validateLoginSubmit() {
        if (login.getUsername() == null ||
                login.getUsername().trim().equals("")) {
            addFieldError("login.username", "Required");
        }
        if (login.getPassword() == null ||
                login.getPassword().trim().equals("")) {
            addFieldError("login.password", "Required");
        }


    }


    /**
     * Validates fields for password submit
     */
    public void validatePasswordSubmit() {
        if (login.getPassword() == null ||
                login.getPassword().trim().equals("")) {
            addFieldError("login.password", "Required");
        }
        if (login.getPasswordConfirm() == null ||
                login.getPasswordConfirm().trim().equals("")) {
            addFieldError("login.passwordConfirm", "Required");
        }
        if (login.getPrevPassword() == null ||
                login.getPrevPassword().trim().equals("")) {
            addFieldError("login.prevPassword", "Required");
        }


    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }


    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}
