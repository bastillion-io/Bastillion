/**
 *    Copyright (C) 2017 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.db.UserDB;
import io.bastillion.manage.db.UserProfileDB;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.jaas.callback.ObjectCallback;
import org.eclipse.jetty.jaas.spi.LdapLoginModule;
import org.eclipse.jetty.jaas.spi.UserInfo;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * External authentication utility for JAAS
 */
public class ExternalAuthUtil {

    private static Logger log = LoggerFactory.getLogger(ExternalAuthUtil.class);

    public static final boolean externalAuthEnabled = StringUtils.isNotEmpty(AppConfig.getProperty("jaasModule"));
    private static final String JAAS_CONF = "jaas.conf";
    private static final String JAAS_MODULE = AppConfig.getProperty("jaasModule");
    private static final String DEFAULT_LDAP_PROFILE = AppConfig.getProperty("defaultProfileForLdap");


    static {
        if (externalAuthEnabled) {
            System.setProperty("java.security.auth.login.config", AppConfig.CONFIG_DIR + "/" + JAAS_CONF);
        }
    }

    private ExternalAuthUtil() {

    }


    /**
     * external auth login method
     *
     * @return auth token if success
     * @auth authentication credentials
     */
    public static String login(final Auth auth) {
        Connection con = null;
        String authToken = null;

        if (externalAuthEnabled && auth != null && StringUtils.isNotEmpty(auth.getUsername()) && StringUtils.isNotEmpty(auth.getPassword())) {

            try {
                //create login context
                LoginContext loginContext = new LoginContext(JAAS_MODULE, new CallbackHandler() {
                    @Override
                    public void handle(Callback[] callbacks) throws IOException,
                            UnsupportedCallbackException {
                        for (Callback callback : callbacks) {
                            if (callback instanceof NameCallback) {
                                ((NameCallback) callback).setName(auth
                                        .getUsername());
                            } else if (callback instanceof ObjectCallback) {
                                ((ObjectCallback) callback).setObject(auth
                                        .getPassword().toCharArray());
                            } else if (callback instanceof PasswordCallback) {
                                ((PasswordCallback) callback).setPassword(auth
                                        .getPassword().toCharArray());

                            }
                        }
                    }
                });

                //will throw exception if login fail
                loginContext.login();

                con = DBUtils.getConn();

                User user = AuthDB.getUserByUID(con, auth.getUsername());

                Field field = LoginContext.class.getDeclaredField("moduleStack");
                field.setAccessible(true);
                Object[] modules = (Object[]) field.get(loginContext);

                for (Object entry : modules) {
                    field = entry.getClass().getDeclaredField("module");
                    field.setAccessible(true);
                    Object module = field.get(entry);

                    field = entry.getClass().getDeclaredField("entry");
                    field.setAccessible(true);
                    AppConfigurationEntry appEntry = (AppConfigurationEntry) field.get(entry);

                    if (module instanceof LdapLoginModule) {

                        //get callback handler
                        field = LoginContext.class.getDeclaredField("callbackHandler");
                        field.setAccessible(true);
                        CallbackHandler callbackHandler = (CallbackHandler) field.get(loginContext);

                        //get state
                        field = LoginContext.class.getDeclaredField("state");
                        field.setAccessible(true);
                        Map state = (Map) field.get(loginContext);

                        LdapLoginModule loginModule = (LdapLoginModule) module;
                        loginModule.initialize(loginContext.getSubject(), callbackHandler, state, appEntry.getOptions());
                        UserInfo userInfo = loginModule.getUserInfo(auth.getUsername());

                        //fetch assigned roles
                        userInfo.fetchRoles();

                        //dir context context
                        field = loginModule.getClass().getDeclaredField("_rootContext");
                        field.setAccessible(true);
                        DirContext dirContext = (DirContext) field.get(loginModule);

                        //role name attribute
                        field = loginModule.getClass().getDeclaredField("_roleNameAttribute");
                        field.setAccessible(true);
                        String roleNameAttribute = (String) field.get(loginModule);

                        //base dn for role
                        field = loginModule.getClass().getDeclaredField("_roleBaseDn");
                        field.setAccessible(true);
                        String roleBaseDn = (String) field.get(loginModule);

                        //role object class
                        field = loginModule.getClass().getDeclaredField("_roleObjectClass");
                        field.setAccessible(true);
                        String roleObjectClass = (String) field.get(loginModule);

                        //all attributes for user
                        field = LdapLoginModule.LDAPUserInfo.class.getDeclaredField("attributes");
                        field.setAccessible(true);
                        Attributes userAttributes = (Attributes) field.get(userInfo);

                        List<String> allRoles = getAllRoles(dirContext, roleBaseDn, roleNameAttribute, roleObjectClass);

                        if (user == null) {
                            user = new User();
                            user.setUserType(User.ADMINISTRATOR);
                            user.setUsername(auth.getUsername());

                            // set attributes from ldap
                            String givenName = userAttributes.get("givenName") != null ? (String) userAttributes.get("givenName").get() : null;
                            String sn = userAttributes.get("sn") != null ? (String) userAttributes.get("sn").get() : null;
                            String displayName = userAttributes.get("displayName") != null ? (String) userAttributes.get("displayName").get() : null;
                            String cn = userAttributes.get("cn") != null ? (String) userAttributes.get("cn").get() : null;
                            String email = userAttributes.get("mail") != null ? (String) userAttributes.get("mail").get() : null;

                            if (StringUtils.isNotEmpty(givenName) && StringUtils.isNotEmpty(sn)) {
                                user.setFirstNm(givenName);
                                user.setLastNm(sn);
                            } else if (StringUtils.isNotEmpty(displayName) && displayName.contains(" ")) {
                                String[] name = displayName.split(" ");
                                if (name.length > 1) {
                                    user.setFirstNm(name[0]);
                                    user.setLastNm(name[name.length - 1]);
                                }
                            } else if (StringUtils.isNotEmpty(cn) && cn.contains(" ")) {
                                String[] name = cn.split(" ");
                                if (name.length > 1) {
                                    user.setFirstNm(name[0]);
                                    user.setLastNm(name[name.length - 1]);
                                }
                            }

                            //set email
                            if (StringUtils.isNotEmpty(email)) {
                                user.setEmail(email);
                            } else if (auth.getUsername().contains("@")) {
                                user.setEmail(auth.getUsername());
                            }
                            user.setId(UserDB.insertUser(con, user));
                        }

                        //assign profiles for user
                        UserProfileDB.assignProfilesToUser(con, user.getId(), allRoles, userInfo.getRoleNames());

                        dirContext.close();
                        loginModule.commit();

                    } else {

                        Subject subject = loginContext.getSubject();

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

                    }
                    if(StringUtils.isNotEmpty(DEFAULT_LDAP_PROFILE)) {
                        UserProfileDB.assignProfileToUser(con, user.getId(), DEFAULT_LDAP_PROFILE);
                    }

                    authToken = UUID.randomUUID().toString();
                    user.setAuthToken(authToken);
                    user.setAuthType(Auth.AUTH_EXTERNAL);
                    //set auth token
                    AuthDB.updateLogin(con, user);
                }
            } catch (LoginException le) {
                authToken = null;
                log.debug(le.toString(), le);
            } catch (Exception e) {
                authToken = null;
                log.error(e.toString(), e);
            } finally {
                DBUtils.closeConn(con);
            }
        }
        return authToken;
    }

    /**
     * returns all possible roles for a user
     *
     * @param dirContext        ldap directory context
     * @param roleBaseDn        base dn for roles
     * @param roleNameAttribute role name
     * @param roleObjectClass   role object class
     * @return all roles under base dn
     */
    private static List<String> getAllRoles(DirContext dirContext, String roleBaseDn, String roleNameAttribute, String roleObjectClass) throws NamingException {
        List<String> allRoles = new ArrayList<String>();
        SearchControls ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setReturningAttributes(new String[]{roleNameAttribute});

        String filter = "(objectClass={0})";
        Object[] filterArguments = {roleObjectClass};
        NamingEnumeration<SearchResult> results = dirContext.search(roleBaseDn, filter, filterArguments, ctls);

        while (results.hasMoreElements()) {
            SearchResult result = results.nextElement();

            Attributes attributes = result.getAttributes();

            if (attributes != null) {
                Attribute roleAttribute = attributes.get(roleNameAttribute);

                if (roleAttribute != null) {

                    NamingEnumeration<?> roles = roleAttribute.getAll();
                    while (roles.hasMore()) {
                        allRoles.add(roles.next().toString());
                    }
                }
            }
        }
        return allRoles;
    }
}