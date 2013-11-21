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
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * DAO to login administrative users
 */
public class AuthDB {

    /**
     * auth user and return auth token if valid auth
     *
     * @param auth username and password object
     * @return auth token if success
     */
    public static String login(Auth auth) {
        String authToken = null;


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and username=? and password=?");
            stmt.setString(1, auth.getUsername());
            stmt.setString(2, EncryptionUtil.hash(auth.getPassword()));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                auth.setId(rs.getLong("id"));

                authToken = UUID.randomUUID().toString();
                auth.setAuthToken(authToken);

                //set auth token
                updateLogin(con, auth);


            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return authToken;

    }


    /**
     * checks to see if user is an admin based on auth token
     *
     * @param authToken auth token string
     * @return user type if authorized, null if not authorized
     */
    public static String isAuthorized(String authToken) {

        String authorized = null;

        Connection con = null;
        if (authToken != null && !authToken.trim().equals("")) {

            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and auth_token=?");
                stmt.setString(1, authToken);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    authorized = rs.getString("user_type");

                }
                DBUtils.closeRs(rs);

                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DBUtils.closeConn(con);
        return authorized;


    }

    /**
     * updates the admin table based on auth id
     *
     * @param con   DB connection
     * @param auth username and password object
     */
    private static void updateLogin(Connection con, Auth auth) {


        try {
            PreparedStatement stmt = con.prepareStatement("update users set username=?, password=?, auth_token=? where id=?");
            stmt.setString(1, auth.getUsername());
            stmt.setString(2, EncryptionUtil.hash(auth.getPassword()));
            stmt.setString(3, auth.getAuthToken());
            stmt.setLong(4, auth.getId());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * updates password for admin using auth token
     */
    public static boolean updatePassword(Auth auth) {
        boolean success=false;

        Connection con = null;
        try {
            con = DBUtils.getConn();


            PreparedStatement stmt = con.prepareStatement("select * from users where auth_token like ? and password like ?");
            stmt.setString(1, auth.getAuthToken());
            stmt.setString(2, EncryptionUtil.hash(auth.getPrevPassword()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                stmt = con.prepareStatement("update users set password=? where auth_token like ?");
                stmt.setString(1, EncryptionUtil.hash(auth.getPassword()));
                stmt.setString(2, auth.getAuthToken());
                stmt.execute();
                success = true;
            }

            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return success;
    }

    /**
     * returns user id based on auth token
     *
     * @param authToken auth token
     * @param con DB connection
     * @return user id
     */
    public static Long getUserIdByAuthToken(Connection con, String authToken) {


        Long userId=null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from users where enabled=true and auth_token like ?");
            stmt.setString(1, authToken);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
               userId=rs.getLong("id");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return userId;

    }
    /**
     * returns user id based on auth token
     *
     * @param authToken auth token
     * @return user id
     */
    public static Long getUserIdByAuthToken(String authToken) {

        Long userId=null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            userId=getUserIdByAuthToken(con, authToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return userId;

    }
}
