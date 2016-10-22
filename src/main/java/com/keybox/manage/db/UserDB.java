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

import com.keybox.manage.model.SortedSet;
import com.keybox.manage.model.User;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
        String sql = "select * from  users where enabled=true " + orderBy;

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
     * @return sorted user list
     */
    public static SortedSet getAdminUserSet(SortedSet sortedSet) {

        ArrayList<User> userList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from  users where enabled=true and user_type like '" + User.ADMINISTRATOR + "' " + orderBy;

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
            PreparedStatement stmt = con.prepareStatement("insert into users (first_nm, last_nm, email, username, auth_type, user_type, password, salt) values (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getFirstNm());
            stmt.setString(2, user.getLastNm());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getAuthType());
            stmt.setString(6, user.getUserType());
            if(StringUtils.isNotEmpty(user.getPassword())) {
                String salt=EncryptionUtil.generateSalt();
                stmt.setString(7, EncryptionUtil.hash(user.getPassword() + salt));
                stmt.setString(8, salt);
            }else {
                stmt.setString(7, null);
                stmt.setString(8, null);
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
    public static void disableUser(Long userId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update users set enabled=false where id=?");
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
            PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and lower(username) like lower(?) and id != ?");
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



}
