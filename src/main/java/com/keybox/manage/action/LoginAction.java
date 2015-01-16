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

import com.keybox.common.util.AppConfig;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.model.Auth;
import com.keybox.manage.db.UserDB;
import com.keybox.manage.util.OTPUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;


/**
 * Action to auth to keybox
 */
public class LoginAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    HttpServletResponse servletResponse;
    HttpServletRequest servletRequest;
    Boolean showOtpPage;
    String showPasswordNotification;
    Auth auth;
    private final String AUTH_ERROR="Authentication Failed : Login credentials are invalid";
    //check if otp is enabled
    boolean otpEnabled="true".equals(AppConfig.getProperty("enableOTP"));

    @Action(value = "/login",
            results = {
                    @Result(name = "success", location = "/login.jsp")
            }
    )
    public String login() {
        
        return SUCCESS;
    }

    @Action(value = "/admin/menu",
            results = {
                    @Result(name = "success", location = "/admin/menu.jsp")
            }
    )
    public String menu() {

        return SUCCESS;
    }


    @Action(value = "/loginSubmit",
            results = {
                    @Result(name = "input", location = "/login.jsp"),
                    @Result(name = "change_password", location = "/admin/user_settings.action", type = "redirect"),
                    @Result(name = "otp", location = "/admin/viewOTP.action", type = "redirect"),
                    @Result(name = "success", location = "/admin/menu.action", type = "redirect")
            }
    )
    public String loginSubmit() {
        String retVal = SUCCESS;

        String authToken = AuthDB.login(auth);
        if (authToken != null) {

            Long userId = AuthDB.getUserIdByAuthToken(authToken);
            String sharedSecret = null;
            if (otpEnabled) {
                sharedSecret = AuthDB.getSharedSecret(userId);
                if (StringUtils.isNotEmpty(sharedSecret) && (auth.getOtpToken() == null || !OTPUtil.verifyToken(sharedSecret, auth.getOtpToken()))) {
                    addActionError(AUTH_ERROR);
                    return INPUT;
                }
            }

            AuthUtil.setAuthToken(servletRequest.getSession(), authToken);
            AuthUtil.setUserId(servletRequest.getSession(), userId);
            AuthUtil.setTimeout(servletRequest.getSession());
            
            boolean bUserUsesOTP = UserDB.getUser(userId).getUseOtp();

            //for first time login redirect to set OTP
            if (otpEnabled && 
                StringUtils.isEmpty(sharedSecret) &&
                bUserUsesOTP) {
                return "otp";
            }
            
            else if (!bUserUsesOTP) {
                retVal = "success";
            }
                
            
            else if ("changeme".equals(auth.getPassword())) {
                retVal = "change_password";
            }

        } else {
            addActionError(AUTH_ERROR);
            retVal = INPUT;
        }



        return retVal;
    }

    @Action(value = "/logout",
            results = {
                    @Result(name = "success", location = "/login.action", type = "redirect")
            }
    )
    public String logout() {
        AuthUtil.deleteAllSession(servletRequest.getSession());
        return SUCCESS;
    }

    @Action(value = "/admin/userSettings",
            results = {
                    @Result(name = "success", location = "/admin/user_settings.jsp")
            }
    )
    public String setPassword() {
        //Set the showOtpPage on page load so you can access it with JSP
        Long userId= AuthUtil.getUserId(servletRequest.getSession());
        showOtpPage = UserDB.getUser(userId).getUseOtp();
        
        return SUCCESS;
    }

    @Action(value = "/admin/passwordSubmit",
            results = {
                    @Result(name = "input", location = "/admin/user_settings.jsp"),
                    @Result(name = "success", location = "/admin/userSettings.action", type = "redirect"),
                    @Result(name = "danger", location = "/admin/userSettings.action", type = "redirect"),
                    @Result(name = "warning", location = "/admin/userSettings.action", type = "redirect")
            }
    )
    public String passwordSubmit() {
        String retVal = SUCCESS;
        
        //Get the session and set default value as success
        HttpSession session = servletRequest.getSession();
        session.setAttribute("showPasswordNotification", "success");

        if (auth.getPassword().equals(auth.getPasswordConfirm())) {
            auth.setAuthToken(AuthUtil.getAuthToken(servletRequest.getSession()));

            if (!AuthDB.updatePassword(auth)) {
                addActionError("Current password is invalid");
                retVal = "danger";
                //Set value as danger (aka. password was wrong)
                session.setAttribute("showPasswordNotification", "danger");
            }

        } else {
            addActionError("Passwords do not match");
            retVal = "warning";
            //Set value as warning (aka. passwords didn't match)
            session.setAttribute("showPasswordNotification", "warning");
        }
        
        return retVal;
    }


    /**
     * Validates fields for auth submit
     */
    public void validateLoginSubmit() {
        if (auth.getUsername() == null ||
                auth.getUsername().trim().equals("")) {
            addFieldError("auth.username", "Required");
        }
        if (auth.getPassword() == null ||
                auth.getPassword().trim().equals("")) {
            addFieldError("auth.password", "Required");
        }


    }


    /**
     * Validates fields for password submit
     */
    public void validatePasswordSubmit() {
        if (auth.getPassword() == null ||
                auth.getPassword().trim().equals("")) {
            addFieldError("auth.password", "Required");
        }
        if (auth.getPasswordConfirm() == null ||
                auth.getPasswordConfirm().trim().equals("")) {
            addFieldError("auth.passwordConfirm", "Required");
        }
        if (auth.getPrevPassword() == null ||
                auth.getPrevPassword().trim().equals("")) {
            addFieldError("auth.prevPassword", "Required");
        }


    }
    
    public String getShowPasswordNotification() {
        return  (String)servletRequest.getSession().getAttribute("showPasswordNotification");
    }
    
    public void setShowPasswordNotification(String showPasswordNotification) {
        servletRequest.getSession().setAttribute("showPasswordNotification", showPasswordNotification);
    }
    
    
    public boolean getShowOtpPage() {
        return showOtpPage;
    }
    
    public void setshowOtpPage(boolean showOtpPage) {
        this.showOtpPage = showOtpPage;
    }

    public boolean isOtpEnabled() {
        return otpEnabled;
    }

    public void setOtpEnabled(boolean otpEnabled) {
        this.otpEnabled = otpEnabled;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
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
