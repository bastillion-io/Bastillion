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
