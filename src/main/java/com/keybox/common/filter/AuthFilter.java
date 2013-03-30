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

import com.keybox.manage.db.AdminDB;
import com.keybox.manage.util.CookieUtil;
import com.keybox.manage.util.EncryptionUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Filter determines if admin user is authenticated
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/manage/*"},
        dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE})
public class AuthFilter implements Filter {

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


        boolean isAdmin = false;


        //read auth token
        String authToken = CookieUtil.get((HttpServletRequest) req, "authToken");

        //check if exists
        if (authToken != null && !authToken.trim().equals("")) {
            //decrypt auth token
            authToken = EncryptionUtil.decrypt(authToken);
            //check if valid admin auth token
            isAdmin = AdminDB.isAdmin(authToken);


            //check to see if user has timed out
            String timeStr = CookieUtil.get((HttpServletRequest) req, "timeout");
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss");
            try {
                if (timeStr != null && !timeStr.trim().equals("")) {
                    Date cookieTimeout = sdf.parse(timeStr);
                    Date currentTime = new Date();

                    //if current time > timeout then redirect to login page
                    if (cookieTimeout == null || currentTime.after(cookieTimeout)) {
                        isAdmin = false;
                    } else {
                        //set new timeout cookie for 15 min
                        Calendar timeout = Calendar.getInstance();
                        timeout.add(Calendar.MINUTE, 15);
                        CookieUtil.add((HttpServletResponse) resp, "timeout", sdf.format(timeout.getTime()));
                    }
                } else {
                    isAdmin = false;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                isAdmin = false;
            }
        }



        //if not admin redirect to login page
        if (!isAdmin) {
            CookieUtil.deleteAll((HttpServletRequest) req, (HttpServletResponse) resp);
            ((HttpServletResponse) resp).sendRedirect(((HttpServletRequest) req).getContextPath() + "/login.action");
        }
        chain.doFilter(req, resp);
    }


}
