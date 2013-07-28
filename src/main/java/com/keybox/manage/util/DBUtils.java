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
package com.keybox.manage.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


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

            Class.forName("org.h2.Driver");
            // create a database connection
            String user="keybox";
            String password="filepwd wHevzQ23uJst/Qg3V+4P+g1/L+rgwKQELW+QUne1";
            con = DriverManager.getConnection("jdbc:h2:" + DB_PATH + "/keybox;CIPHER=AES", user, password);


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
