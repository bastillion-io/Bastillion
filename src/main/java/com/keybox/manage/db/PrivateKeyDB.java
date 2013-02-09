/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.db;

import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO that returns passphrase for the system generated private key
 */
public class PrivateKeyDB {


    /**
     * returns passphrase for the system generated private key
     * @return passphrase
     */
    public static String getPassphrase() {

        String passphrase = null;

        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from  private_key");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                passphrase=rs.getString("passphrase");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return passphrase;
    }


}
