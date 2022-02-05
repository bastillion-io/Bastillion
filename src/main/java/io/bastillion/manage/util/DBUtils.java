/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Class to create and close database resources
 */
public class DBUtils {

    private DBUtils() {
    }

    /**
     * returns DB connection
     *
     * @return DB connection
     */
    public static Connection getConn() throws SQLException, GeneralSecurityException {
        return DSPool.getDataSource().getConnection();
    }

    /**
     * close DB connection
     *
     * @param con DB connection
     */
    public static void closeConn(Connection con) throws SQLException {
        if (con != null) {
            con.close();
        }
    }

    /**
     * Close DB statement
     *
     * @param stmt DB statement
     */
    public static void closeStmt(Statement stmt) throws SQLException {
        if (stmt != null) {
            stmt.close();
        }
    }

    /**
     * close DB result set
     *
     * @param rs DB result set
     */
    public static void closeRs(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

}
