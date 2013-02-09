/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.common.filter;

import com.keybox.manage.db.AdminDB;
import com.keybox.manage.util.CookieUtil;
import com.keybox.manage.util.EncryptionUtil;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Filter determines if admin user is authenticated
 */
public class AuthFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {

    }

    public void destroy() {
    }

    /**
     * doFilter determines if user is an administrator or redirect to login page
     *
     * @param req   servlet request
     * @param resp  servlet response
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
