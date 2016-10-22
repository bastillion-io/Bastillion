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
package com.keybox.manage.db;

import com.keybox.manage.model.Auth;
import com.keybox.manage.model.User;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.ExternalAuthUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO to login administrative users
 */
public class AuthDB {

    private static Logger log = LoggerFactory.getLogger(AuthDB.class);

    private AuthDB() {
    }

    /**
     * auth user and return auth token if valid auth
     *
     * @param auth username and password object
     * @return auth token if success
     */
    public static String login(Auth auth) {
        //check ldap first
        String authToken = ExternalAuthUtil.login(auth);
        
        if (StringUtils.isEmpty(authToken)) {

            Connection con = null;

            try {
                con = DBUtils.getConn();

                //get salt for user
                String salt = getSaltByUsername(con, auth.getUsername());
                //login
                PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and username=? and password=?");
                stmt.setString(1, auth.getUsername());
                stmt.setString(2, EncryptionUtil.hash(auth.getPassword() + salt));
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {

                    auth.setId(rs.getLong("id"));
                    authToken = UUID.randomUUID().toString();
                    auth.setAuthToken(authToken);
                    auth.setAuthType(Auth.AUTH_BASIC);
                    updateLogin(con, auth);

                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);


            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            finally {
                DBUtils.closeConn(con);
            }
        }

        return authToken;

    }


    /**
     * checks to see if user is an admin based on auth token
     *
     * @param userId    user id
     * @param authToken auth token string
     * @return user type if authorized, null if not authorized
     */
    public static String isAuthorized(Long userId, String authToken) {

        String authorized = null;

        if (authToken != null && !authToken.trim().equals("")) {

            Connection con = null;
            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and id=? and auth_token=?");
                stmt.setLong(1, userId);
                stmt.setString(2, authToken);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    authorized = rs.getString("user_type");

                }
                DBUtils.closeRs(rs);

                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            finally {
                DBUtils.closeConn(con);
            }
        }
        return authorized;


    }

    /**
     * updates the admin table based on auth id
     *
     * @param con  DB connection
     * @param auth username and password object
     */
    public static void updateLogin(Connection con, Auth auth) {


        try {
            PreparedStatement stmt = con.prepareStatement("update users set username=?, auth_type=?, auth_token=?, password=?, salt=? where id=?");
            stmt.setString(1, auth.getUsername());
            stmt.setString(2, auth.getAuthType());
            stmt.setString(3, auth.getAuthToken());
            if (StringUtils.isNotEmpty(auth.getPassword())) {
                String salt = EncryptionUtil.generateSalt();
                stmt.setString(4, EncryptionUtil.hash(auth.getPassword() + salt));
                stmt.setString(5, salt);
            } else {
                stmt.setString(4, null);
                stmt.setString(5, null);
            }
            stmt.setLong(6, auth.getId());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }


    }

    /**
     * updates password for admin using auth token
     */
    public static boolean updatePassword(Auth auth) {
        boolean success = false;

        Connection con = null;
        try {
            con = DBUtils.getConn();


            String prevSalt = getSaltByAuthToken(con, auth.getAuthToken());
            PreparedStatement stmt = con.prepareStatement("select * from users where auth_token like ? and password like ?");
            stmt.setString(1, auth.getAuthToken());
            stmt.setString(2, EncryptionUtil.hash(auth.getPrevPassword() + prevSalt));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                String salt = EncryptionUtil.generateSalt();
                stmt = con.prepareStatement("update users set password=?, salt=? where auth_token like ?");
                stmt.setString(1, EncryptionUtil.hash(auth.getPassword() + salt));
                stmt.setString(2, salt);
                stmt.setString(3, auth.getAuthToken());
                stmt.execute();
                success = true;
            }

            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
        return success;
    }

    /**
     * returns user id based on auth token
     *
     * @param authToken auth token
     * @param con       DB connection
     * @return user
     */
    public static User getUserByAuthToken(Connection con, String authToken) {


        User user = null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and auth_token like ?");
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long userId = rs.getLong("id");
                
                user=UserDB.getUser(con, userId);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }


        return user;

    }

    /**
     * returns user based on auth token
     *
     * @param authToken auth token
     * @return user
     */
    public static User getUserByAuthToken(String authToken) {

        User user = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            user = getUserByAuthToken(con, authToken);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return user;

    }

    /**
     * returns the shared secret based on user id
     *
     * @param userId user id
     * @return auth object
     */
    public static String getSharedSecret(Long userId) {

        String sharedSecret = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from users where id like ?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sharedSecret = EncryptionUtil.decrypt(rs.getString("otp_secret"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return sharedSecret;

    }

    /**
     * updates shared secret based on auth token
     *
     * @param secret    OTP shared secret
     * @param authToken auth token
     */
    public static void updateSharedSecret(String secret, String authToken) {

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update users set otp_secret=? where auth_token=?");
            stmt.setString(1, EncryptionUtil.encrypt(secret));
            stmt.setString(2, authToken);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        DBUtils.closeConn(con);

    }


    /**
     * get salt by user name
     *
     * @param con      DB connection
     * @param username username
     * @return salt
     */
    private static String getSaltByUsername(Connection con, String username) {

        String salt = "";
        try {
            PreparedStatement stmt = con.prepareStatement("select salt from users where enabled=true and username=?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("salt") != null) {
                salt = rs.getString("salt");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return salt;
    }


    /**
     * get salt by authentication token
     *
     * @param con       DB connection
     * @param authToken auth token
     * @return salt
     */
    private static String getSaltByAuthToken(Connection con, String authToken) {

        String salt = "";
        try {
            PreparedStatement stmt = con.prepareStatement("select salt from users where enabled=true and auth_token=?");
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getString("salt") != null) {
                salt = rs.getString("salt");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return salt;
    }


    /**
     * returns user base on username
     *
     * @param con DB connection
     * @param uid username id
     * @return user object
     */
    public static User getUserByUID(Connection con, String uid) {

        User user = null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from  users where lower(username) like lower(?) and enabled=true");
            stmt.setString(1, uid);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                user = new User();
                user.setId(rs.getLong("id"));
                user.setFirstNm(rs.getString("first_nm"));
                user.setLastNm(rs.getString("last_nm"));
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                user.setUserType(rs.getString("user_type"));
                user.setProfileList(UserProfileDB.getProfilesByUser(con, user.getId()));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return user;
    }
}
