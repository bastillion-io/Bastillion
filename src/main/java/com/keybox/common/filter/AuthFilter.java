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
package com.keybox.common.filter;

import com.keybox.common.util.AuthUtil;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.model.Auth;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter determines if admin user is authenticated
 */
public class AuthFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(AuthFilter.class);

    public void init(FilterConfig config) throws ServletException {

    }

    public void destroy() {
    }

    /**
     * doFilter determines if user is an administrator or redirect to login page
     *
     * @param req   task request
     * @param resp  task response
     * @param chain filter chain
     * @throws ServletException
     * @throws IOException
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {


        HttpServletRequest servletRequest = (HttpServletRequest) req;
        HttpServletResponse servletResponse = (HttpServletResponse) resp;
        boolean isAdmin = false;


        //read auth token
        String authToken = AuthUtil.getAuthToken(servletRequest.getSession());

        //check if exists
        if (authToken != null && !authToken.trim().equals("")) {
            //check if valid admin auth token
            String userType = AuthDB.isAuthorized(AuthUtil.getUserId(servletRequest.getSession()), authToken);
            if (userType != null) {
                String uri = servletRequest.getRequestURI();
                if (Auth.MANAGER.equals(userType)) {
                    isAdmin = true;
                } else if (uri.matches("^"+servletRequest.getContextPath().replaceAll("/","\\\\/")+"\\/admin\\/.*") && Auth.ADMINISTRATOR.equals(userType)) {
                    isAdmin = true;
                }
                AuthUtil.setUserType(servletRequest.getSession(), userType);

                //check to see if user has timed out
                String timeStr = AuthUtil.getTimeout(servletRequest.getSession());
                try {
                    if (timeStr != null && !timeStr.trim().equals("")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss");
                        Date cookieTimeout = sdf.parse(timeStr);
                        Date currentTime = new Date();

                        //if current time > timeout then redirect to login page
                        if (cookieTimeout == null || currentTime.after(cookieTimeout)) {
                            isAdmin = false;
                        } else {
                            AuthUtil.setTimeout(servletRequest.getSession());
                        }
                    } else {
                        isAdmin = false;
                    }

                } catch (Exception ex) {
                    log.error(ex.toString(), ex);
                    isAdmin = false;
                }
            }

        }

        //if not admin redirect to login page
        if (!isAdmin) {
            AuthUtil.deleteAllSession(servletRequest.getSession());
            servletResponse.sendRedirect(servletRequest.getContextPath() + "/login.action");
        }
        else{
            chain.doFilter(req, resp);
        }
    }


}
