/**
 * Copyright 2016 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.keybox.common.util.AuthUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.interceptor.TokenInterceptor;

/**
 * Interceptor class to prevent cross-site request forgery
 */
public class CSRFInterceptor extends TokenInterceptor {

    private static final long serialVersionUID = 7234421386123543997L;

    @Override
    protected String handleToken(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpSession session = request.getSession(true);
        synchronized (session) {
            String sessionToken = (String) session.getAttribute(AuthUtil.CSRF_TOKEN_NM);
            String token = request.getParameter(AuthUtil.CSRF_TOKEN_NM);
            if (StringUtils.isEmpty(token) || StringUtils.isEmpty(sessionToken) || !token.equals(sessionToken)) {
                AuthUtil.deleteAllSession(session);
                return this.handleInvalidToken(invocation);
            }

            //generate new token upon post
            if ("POST".equals(request.getMethod())
                    && !request.getContentType().contains("multipart/form-data")) {
                AuthUtil.generateCSRFToken(session);
            }
        }

        return this.handleValidToken(invocation);
    }

}