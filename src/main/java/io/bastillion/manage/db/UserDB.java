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

import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.bastillion.manage.db.AuthDB.EXPIRATION_DAYS;


/**
 * DAO class to manage users
 */
public class UserDB {

    private static Logger log = LoggerFactory.getLogger(UserDB.class);

    public static final String PASSWORD = "password";
    public static final String FIRST_NM = "first_nm";
    public static final String LAST_NM = "last_nm";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String USER_TYPE = "user_type";
    public static final String AUTH_TYPE = "auth_type";
    public static final String PROFILE_ID = "profile_id";
    public static final String LAST_LOGIN_TM = "last_login_tm";
    public static final String EXPIRATION_TM = "expiration_tm";

    private UserDB() {
    }

    /**
     * returns users based on sort order defined
     * @param sortedSet object that defines sort order
     * @return sorted user list
     */
    public static SortedSet getUserSet(SortedSet sortedSet) {

        ArrayList<User> userList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from  users " + orderBy;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setFirstNm(rs.getString(FIRST_NM));
                user.setLastNm(rs.getString(LAST_NM));
                user.setEmail(rs.getString(EMAIL));
                user.setUsername(rs.getString(USERNAME));
                user.setPassword(rs.getString(PASSWORD));
                user.setAuthType(rs.getString(AUTH_TYPE));
                user.setUserType(rs.getString(USER_TYPE));
                user.setLastLoginTm(rs.getTimestamp(LAST_LOGIN_TM));
                user.setExpirationTm(rs.getTimestamp(EXPIRATION_TM));
				if (EXPIRATION_DAYS > 0 && user.getExpirationTm() != null && user.getExpirationTm().before(new Date())) {
                    user.setExpired(true);
                }
                else {
                    user.setExpired(false);
                }
                userList.add(user);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        sortedSet.setItemList(userList);
        return sortedSet;
    }

    /**
     * returns all admin users based on sort order defined
     * @param sortedSet object that defines sort order
     * @profileId check if user is apart of given profile
     * @return sorted user list
     */
    public static SortedSet getAdminUserSet(SortedSet sortedSet, Long profileId) {

        ArrayList<User> userList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select u.*, m.profile_id from users u left join user_map  m on m.user_id = u.id and m.profile_id = ? where u.user_type like '" + User.ADMINISTRATOR + "'" + orderBy;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1, profileId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setFirstNm(rs.getString(FIRST_NM));
                user.setLastNm(rs.getString(LAST_NM));
                user.setEmail(rs.getString(EMAIL));
                user.setUsername(rs.getString(USERNAME));
                user.setPassword(rs.getString(PASSWORD));
                user.setAuthType(rs.getString(AUTH_TYPE));
                user.setUserType(rs.getString(USER_TYPE));
                user.setLastLoginTm(rs.getTimestamp(LAST_LOGIN_TM));
                user.setExpirationTm(rs.getTimestamp(EXPIRATION_TM));
				if (EXPIRATION_DAYS > 0 && user.getExpirationTm() != null && user.getExpirationTm().before(new Date())) {
                    user.setExpired(true);
                }
                else {
                    user.setExpired(false);
                }
                if (profileId != null && profileId.equals(rs.getLong(PROFILE_ID))) {
                    user.setChecked(true);
                } else {
                    user.setChecked(false);
                }
                userList.add(user);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        sortedSet.setItemList(userList);
        return sortedSet;
    }


    /**
     * returns user base on id
     * @param userId user id
     * @return user object
     */
    public static User getUser(Long userId) {

        User user = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            user = getUser(con, userId);


        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return user;
    }

    /**
     * returns user base on id
     * @param con DB connection
     * @param userId user id
     * @return user object
     */
    public static User getUser(Connection con, Long userId) {

        User user = null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from  users where id=?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                user = new User();
                user.setId(rs.getLong("id"));
                user.setFirstNm(rs.getString(FIRST_NM));
                user.setLastNm(rs.getString(LAST_NM));
                user.setEmail(rs.getString(EMAIL));
                user.setUsername(rs.getString(USERNAME));
                user.setPassword(rs.getString(PASSWORD));
                user.setAuthType(rs.getString(AUTH_TYPE));
                user.setUserType(rs.getString(USER_TYPE));
                user.setLastLoginTm(rs.getTimestamp(LAST_LOGIN_TM));
                user.setExpirationTm(rs.getTimestamp(EXPIRATION_TM));
				if (EXPIRATION_DAYS > 0 && user.getExpirationTm() != null && user.getExpirationTm().before(new Date())) {
                    user.setExpired(true);
                }
                else {
                    user.setExpired(false);
                }
                user.setSalt(rs.getString("salt"));
                user.setProfileList(UserProfileDB.getProfilesByUser(con, userId));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return user;
    }

    /**
     * inserts new user
     *
     * @param user user object
     */
    public static Long insertUser(User user) {

        Long userId = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            userId = insertUser(con, user);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return userId;

    }

    /**
     * inserts new user
     * 
     * @param con DB connection 
     * @param user user object
     */
    public static Long insertUser(Connection con, User user) {

        Long userId=null;
        
        try {
            PreparedStatement stmt = con.prepareStatement("insert into users (first_nm, last_nm, email, username, auth_type, user_type, password, salt, expiration_tm) values (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getFirstNm());
            stmt.setString(2, user.getLastNm());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getAuthType());
            stmt.setString(6, user.getUserType());
            if(StringUtils.isNotEmpty(user.getPassword())) {
                String salt= EncryptionUtil.generateSalt();
                stmt.setString(7, EncryptionUtil.hash(user.getPassword() + salt));
                stmt.setString(8, salt);
            }else {
				stmt.setString(7, null);
				stmt.setString(8, null);
			}
            if(Auth.MANAGER.equals(user.getUserType()) || EXPIRATION_DAYS <=0) {
                stmt.setTimestamp(9, null);
            } else {
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.DATE, EXPIRATION_DAYS);
                stmt.setTimestamp(9, new Timestamp(c.getTime().getTime()));
            }
            stmt.execute();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs != null && rs.next()) {
                userId = rs.getLong(1);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        
        return userId;

    }

    /**
     * updates existing user
     * @param user user object
     */
    public static void updateUserNoCredentials(User user) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update users set first_nm=?, last_nm=?, email=?, username=?, user_type=? where id=?");
            stmt.setString(1, user.getFirstNm());
            stmt.setString(2, user.getLastNm());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getUserType());
            stmt.setLong(6, user.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);
            if (User.ADMINISTRATOR.equals(user.getUserType())) {
                PublicKeyDB.deleteUnassignedKeysByUser(con, user.getId());
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * updates existing user
     * @param user user object
     */
    public static void updateUserCredentials(User user) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            String salt=EncryptionUtil.generateSalt();
            PreparedStatement stmt = con.prepareStatement("update users set first_nm=?, last_nm=?, email=?, username=?, user_type=?, password=?, salt=? where id=?");
            stmt.setString(1, user.getFirstNm());
            stmt.setString(2, user.getLastNm());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getUserType());
            stmt.setString(6, EncryptionUtil.hash(user.getPassword()+salt));
            stmt.setString(7, salt);
            stmt.setLong(8, user.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);
            if(User.ADMINISTRATOR.equals(user.getUserType())) {
                PublicKeyDB.deleteUnassignedKeysByUser(con, user.getId());
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * deletes user
     * @param userId user id
     */
    public static void deleteUser(Long userId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from users where id=?");
            stmt.setLong(1, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * resets shared secret for user
     * @param userId user id
     */
    public static void resetSharedSecret(Long userId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update users set otp_secret=null where id=?");
            stmt.setLong(1, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * checks to see if username is unique while ignoring current user
     *
     * @param userId user id
     * @param username username
     * @return true false indicator
     */
    public static boolean isUnique(Long userId, String username){

        boolean isUnique=true;
        if(userId==null){
            userId=-99L;
        }

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from users where lower(username) like lower(?) and id != ?");
            stmt.setString(1,username);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                isUnique=false;
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
        } catch(Exception ex){
            log.error(ex.toString(), ex);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return isUnique;

    }

    /**
	 * Unlock account that has expired due to inactivity.
     * @param userId user Id
     */
    public static void unlockAccount(Long userId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update users set expiration_tm=? where id=?");
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, EXPIRATION_DAYS);
            stmt.setTimestamp(1, new Timestamp(c.getTime().getTime()));
            stmt.setLong(2, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }



}
