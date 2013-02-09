/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.db;

import com.keybox.manage.model.SortedSet;
import com.keybox.manage.model.User;
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;


/**
 * DAO class to manage users
 */
public class UserDB {

    public static final String SORT_BY_FIRST_NM="first_nm";
    public static final String SORT_BY_LAST_NM="last_nm";
    public static final String SORT_BY_EMAIL="email";

    /**
     * returns users based on sort order defined
     * @param sortedSet object that defines sort order
     * @return sorted user list
     */
    public static SortedSet getUserSet(SortedSet sortedSet) {

        ArrayList<User> userList = new ArrayList<User>();


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
                user.setFirstNm(rs.getString("first_nm"));
                user.setLastNm(rs.getString("last_nm"));
                user.setEmail(rs.getString("email"));
                user.setPublicKey(rs.getString("public_key"));
                userList.add(user);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

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
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

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
                user.setFirstNm(rs.getString("first_nm"));
                user.setLastNm(rs.getString("last_nm"));
                user.setEmail(rs.getString("email"));
                user.setPublicKey(rs.getString("public_key"));
                user.setProfileList(UserProfileDB.getProfilesByUser(con, userId));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * inserts new user
     * @param user user object
     */
    public static void insertUser(User user) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("insert into users (first_nm, last_nm, email, public_key) values (?,?,?,?)");
            stmt.setString(1, user.getFirstNm());
            stmt.setString(2, user.getLastNm());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPublicKey());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * updates existing user
     * @param user user object
     */
    public static void updateUser(User user) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update users set first_nm=?, last_nm=?, email=?, public_key=? where id=?");
            stmt.setString(1, user.getFirstNm());
            stmt.setString(2, user.getLastNm());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPublicKey());
            stmt.setLong(5, user.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

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
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


}
