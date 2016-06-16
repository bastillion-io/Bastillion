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
import java.sql.ResultSet;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to create and close database resources
 */
public class DBUtils {

    private static Logger log = LoggerFactory.getLogger(DBUtils.class);

    private DBUtils() {
    }

    /**
     * returns DB connection
     *
     * @return DB connection
     */
    public static Connection getConn() {
        Connection con = null;
        try{
            con=DSPool.getDataSource().getConnection();

        } catch (Exception ex) {
            log.error(ex.toString(), ex);
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
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
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
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
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
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }


    }

}
