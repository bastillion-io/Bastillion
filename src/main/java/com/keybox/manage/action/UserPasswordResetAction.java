/**
 * Copyright 2015 Robert Vorkoeper - robert-vor@gmx.de
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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.keybox.common.util.AppConfig;
import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.db.UserDB;
import com.keybox.manage.model.Auth;
import com.keybox.manage.model.User;
import com.keybox.manage.util.OTPUtil;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action to User Password reset with Mail send
 * 
 * @author Robert Vorkoeper
 *
 */
public class UserPasswordResetAction extends ActionSupport{

    private final String PW_RESET_MASSAGE="Password reset. Please look into your email inbox.";
    private final String ERROR_MASSAGE="There has been an error.Please contact the administrator or try it again later.";
    private final String INVALID_MAIL_MASSAGE="Your mail address is invalid.";
    String email;
    boolean pwMailResetEnabled="true".equals(AppConfig.getProperty("pwMailReset"));
	

	@Action(value = "/pwReset",
            results = {
                    @Result(name = "success", location = "/pw_reset.jsp"),
                    @Result(name =  "error", location = "/error.jsp" )
            }
    )
	public String pwReset() {
		if (pwMailResetEnabled){
			return SUCCESS;
		} else {
			return ERROR;
		}
    }

	
	@Action(value = "/pwResetSubmit",
            results = {
                    @Result(name = "input", location = "/pw_reset.jsp"),
                    @Result(name =  "error", location = "/error.jsp" )
            }
    )
    public String pwResetSubmit() {
		if (pwMailResetEnabled && StringUtils.isNotEmpty(email)){
			try {
				//EMail Address validate Test
				InternetAddress internetAddress = new InternetAddress(email);
				internetAddress.validate();
				
				//Reset Password and send mail
				if(UserDB.resetPWMail(email)) {
					addActionMessage(PW_RESET_MASSAGE);
					addActionError(null);
				} else {
					addActionMessage(null);
					addActionError(ERROR_MASSAGE);
				}								
			} catch (AddressException e) {
				addActionMessage(null);
				addActionError(INVALID_MAIL_MASSAGE);
			}
			return INPUT;
		} else {
			return ERROR;
		}
    }
	
    public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}
}
