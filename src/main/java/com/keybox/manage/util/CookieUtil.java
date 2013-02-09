/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages session cookies
 */
public class CookieUtil {

    //set all cookies to expire after browser closes
    public static final Integer MAX_AGE = -1;

    /**
     * get value of cookie based on cookie name
     *
     * @param request  HttpServletRequest object
     * @param cookieNm cookie name
     * @return value of cookie
     */
    public static String get(HttpServletRequest request, String cookieNm) {
        String value = null;
        if (cookieNm != null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieNm)) {
                    value = cookie.getValue();

                }
            }
        }
        return value;
    }

    /**
     * add new cookie
     *
     * @param response  HttpServletResponse object
     * @param cookieNm  cookie name
     * @param cookieVal cookie value
     */
    public static void add(HttpServletResponse response, String cookieNm, String cookieVal) {


        Cookie cookie = new Cookie(cookieNm, cookieVal);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE);

        response.addCookie(cookie);

    }

    /**
     * deletes all cookies
     *
     * @param request  HttpServletRequest object
     * @param response HttpServletResponse object
     */
    public static void deleteAll(HttpServletRequest request, HttpServletResponse response) {

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);

            }
        }
    }

    /**
     * deletes cookie by name
     *
     * @param request  HttpServletRequest object
     * @param response HttpServletResponse object
     * @param cookieNm cookie name
     */
    public static void delete(HttpServletRequest request, HttpServletResponse response, String cookieNm) {

        if (cookieNm != null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieNm)) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }
    }


}
