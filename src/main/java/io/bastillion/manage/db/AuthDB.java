/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
 */
package io.bastillion.manage.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;
import io.bastillion.manage.util.ExternalAuthUtil;
import io.bastillion.manage.util.ProxyAuthUtil;

/**
 * DAO to login administrative users
 */
public class AuthDB {

    private static Logger log = LoggerFactory.getLogger(AuthDB.class);

    public static final int EXPIRATION_DAYS = StringUtils.isNumeric(AppConfig.getProperty("accountExpirationDays")) ? Integer.parseInt(AppConfig.getProperty("accountExpirationDays")) : -1;

    private AuthDB() {
    }

    /**
     * auth user and return auth token if valid auth
     *
     * @param auth username and password object
     * @return auth token if success
     */
    public static String login(Auth auth) {
    	
		// if proxy, check it first
		String authToken = ProxyAuthUtil.login(auth);

		// check ldap second
		if (StringUtils.isEmpty(authToken)) {
			authToken = ExternalAuthUtil.login(auth);
		}
        
        if (StringUtils.isEmpty(authToken)) {

            Connection con = null;

            try {
                con = DBUtils.getConn();

                //get salt for user
                String salt = getSaltByUsername(con, auth.getUsername());
                //login
                PreparedStatement stmt = con.prepareStatement("select * from users where username=? and password=?");
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
                PreparedStatement stmt = con.prepareStatement("select * from users where id=? and auth_token=?");
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
     * updates the last login and expiration time
     *
     * @param con  DB connection
     * @param auth username and password object
     */
    public static void updateLastLogin(Connection con, Auth auth) {

        try {
            PreparedStatement stmt = con.prepareStatement("update users set last_login_tm=?, expiration_tm=? where id=?");

            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            stmt.setTimestamp(1, new Timestamp(c.getTime().getTime()));
            if(Auth.MANAGER.equals(auth.getUserType()) || EXPIRATION_DAYS <=0) {
                stmt.setTimestamp(2, null);
            } else {
                c.add(Calendar.DATE, EXPIRATION_DAYS);
                stmt.setTimestamp(2, new Timestamp(c.getTime().getTime()));
            }
            stmt.setLong(3, auth.getId());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

    }

    /**
     * updates the last login and expiration time
     *
     * @param auth username and password object
     */
    public static void updateLastLogin(Auth auth) {

        Connection con = null;
        try {
            con = DBUtils.getConn();
            updateLastLogin(con, auth);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
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
                PreparedStatement updateStmt = con.prepareStatement("update users set password=?, salt=? where auth_token like ?");
                updateStmt.setString(1, EncryptionUtil.hash(auth.getPassword() + salt));
                updateStmt.setString(2, salt);
                updateStmt.setString(3, auth.getAuthToken());
                updateStmt.execute();
                DBUtils.closeStmt(updateStmt);
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
            PreparedStatement stmt = con.prepareStatement("select * from users where auth_token like ?");
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
            PreparedStatement stmt = con.prepareStatement("select salt from users where username=?");
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
            PreparedStatement stmt = con.prepareStatement("select salt from users where auth_token=?");
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
            PreparedStatement stmt = con.prepareStatement("select * from  users where lower(username) like lower(?)");
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
                user.setLastLoginTm(rs.getTimestamp("last_login_tm"));
                user.setExpirationTm(rs.getTimestamp("expiration_tm"));
                if (EXPIRATION_DAYS > 0 && user.getExpirationTm() != null && user.getExpirationTm().before(new Date())) {
                    user.setExpired(true);
                }
                else {
                    user.setExpired(false);
                }
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
