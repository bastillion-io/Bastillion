/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.util;


import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;


/**
 * Class to create and close database resources
 */
public class DBUtils {

    //system path to the sqlite DB
    private static String DB_PATH = DBUtils.class.getClassLoader().getResource("com/keybox/common/db").getPath();

    /**
     * returns DB connection
     *
     * @return DB connection
     */
    public static Connection getConn() {
        Connection con = null;
        try {

            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            Class.forName("org.sqlite.JDBC");
            // create a database connection

            con = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH + "/keybox.db", config.toProperties());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return con;

    }

    /**
     * close DB connection
     *
     * @param con DB connection
     */
    public static void closeConn(Connection con) {
        try {
            if (con != null) {
                con.close();
            }
            con = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    /**
     * Close DB statement
     *
     * @param stmt DB statement
     */
    public static void closeStmt(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
            stmt = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    /**
     * close DB result set
     *
     * @param rs DB result set
     */
    public static void closeRs(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

}
