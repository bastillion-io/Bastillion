/**
 * Copyright 2015 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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
package com.keybox.manage.util;


import com.keybox.common.util.AppConfig;
import com.keybox.manage.db.AuthDB;
import com.keybox.manage.db.UserDB;
import com.keybox.manage.model.Auth;
import com.keybox.manage.model.User;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * External authentication utility for JAAS
 */
public class ExternalAuthUtil {

    private static Logger log = LoggerFactory.getLogger(ExternalAuthUtil.class);

    public static final boolean externalAuthEnabled = StringUtils.isNotEmpty(AppConfig.getProperty("jaasModule"));
    private static final String JAAS_CONF = "jaas.conf";
    private static final String JAAS_MODULE = AppConfig.getProperty("jaasModule");


    static {
        if (externalAuthEnabled) {
            System.setProperty("java.security.auth.login.config", ExternalAuthUtil.class.getClassLoader().getResource(".").getPath() + JAAS_CONF);
        }
    }

    private ExternalAuthUtil() {
    }


    /**
     * external auth login method
     *
     * @param auth contains username and password
     * @return auth token if success
     */
    public static String login(final Auth auth) {

        String authToken = null;
        if (externalAuthEnabled && auth != null && StringUtils.isNotEmpty(auth.getUsername()) && StringUtils.isNotEmpty(auth.getPassword())) {

            CallbackHandler handler = new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException,
                        UnsupportedCallbackException {
                    for (Callback callback : callbacks) {
                        if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(auth
                                    .getUsername());
                        } else if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword(auth
                                    .getPassword().toCharArray());
                        }
                    }
                }
            };

            Connection con = null;
            try {
                LoginContext loginContext = new LoginContext(JAAS_MODULE, handler);
                //will throw exception if login fail
                loginContext.login();
                Subject subject = loginContext.getSubject();

                con = DBUtils.getConn();
                User user = AuthDB.getUserByUID(con, auth.getUsername());

                if (user == null) {
                    user = new User();

                    user.setUserType(User.ADMINISTRATOR);
                    user.setUsername(auth.getUsername());

                    //if it looks like name is returned default it
                    for (Principal p : subject.getPrincipals()) {
                        if (p.getName().contains(" ")) {
                            String[] name = p.getName().split(" ");
                            if (name.length > 1) {
                                user.setFirstNm(name[0]);
                                user.setLastNm(name[name.length - 1]);
                            }
                        }
                    }

                    //set email
                    if (auth.getUsername().contains("@")) {
                        user.setEmail(auth.getUsername());
                    }

                    user.setId(UserDB.insertUser(con, user));
                }

                authToken = UUID.randomUUID().toString();
                user.setAuthToken(authToken);
                user.setAuthType(Auth.AUTH_EXTERNAL);
                //set auth token
                AuthDB.updateLogin(con, user);


            } catch (LoginException e) {
                //auth failed return empty
                authToken = null;
            } catch (Exception e) {
                log.error(e.toString(), e);
            } finally {
                DBUtils.closeConn(con);
            }
        }
        return authToken;
    }
}
