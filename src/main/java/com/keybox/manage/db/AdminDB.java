package com.keybox.manage.db;

import com.keybox.manage.model.Login;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * DAO to login administrative users
 */
public class AdminDB {

    /**
     * login user and return auth token if valid login
     *
     * @param login username and password object
     * @return auth token if success
     */
    public static String loginAdmin(Login login) {
        String authToken = null;


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from  admin where username=? and password=?");
            stmt.setString(1, login.getUsername());
            stmt.setString(2, EncryptionUtil.hash(login.getPassword()));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                login.setId(rs.getLong("id"));

                authToken = UUID.randomUUID().toString();
                login.setAuthToken(authToken);

                //set auth token
                updateAdmin(con, login);


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
     */
    public static boolean isAdmin(String authToken) {

        boolean isAdmin = false;

        Connection con = null;
        if (authToken != null && !authToken.trim().equals("")) {

            try {
                con = DBUtils.getConn();
                PreparedStatement stmt = con.prepareStatement("select * from  admin where auth_token=?");
                stmt.setString(1, authToken);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    isAdmin = true;

                }
                DBUtils.closeRs(rs);

                DBUtils.closeStmt(stmt);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DBUtils.closeConn(con);
        return isAdmin;


    }

    /**
     * updates the admin table based on login id
     *
     * @param con   DB connection
     * @param login username and password object
     */
    private static void updateAdmin(Connection con, Login login) {


        try {
            PreparedStatement stmt = con.prepareStatement("update admin set username=?, password=?, auth_token=? where id=?");
            stmt.setString(1, login.getUsername());
            stmt.setString(2, EncryptionUtil.hash(login.getPassword()));
            stmt.setString(3, login.getAuthToken());
            stmt.setLong(4, login.getId());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * updates password for admin using auth token
     */
    public static boolean updatePassword(Login login) {
        boolean success=false;

        Connection con = null;
        try {
            con = DBUtils.getConn();


            PreparedStatement stmt = con.prepareStatement("select * from admin where auth_token like ? and password like ?");
            stmt.setString(1, login.getAuthToken());
            stmt.setString(2, EncryptionUtil.hash(login.getPrevPassword()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                stmt = con.prepareStatement("update admin set password=? where auth_token like ?");
                stmt.setString(1, EncryptionUtil.hash(login.getPassword()));
                stmt.setString(2, login.getAuthToken());
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
}
