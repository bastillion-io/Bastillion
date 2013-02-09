/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.common.db;

import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;
import com.keybox.manage.util.SSHUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initial startup servlet.  Creates an SQLite DB and generates
 * the system public/private key pair if none exists
 */
public class DBInitServlet extends javax.servlet.http.HttpServlet {

    /**
     * servlet init method that created DB and generated public/private keys
     * @param config servlet config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {

        super.init(config);


        try {
            Connection connection = DBUtils.getConn();
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("select * from sqlite_master where type='table' and name='admin'");
            if (rs == null || !rs.next()) {
                statement.executeUpdate("create table if not exists admin (id INTEGER PRIMARY KEY AUTOINCREMENT, username string unique not null, password string not null, auth_token string)");
                //insert default admin user
                statement.executeUpdate("insert or ignore into admin (username, password) values('admin', '" + EncryptionUtil.hash("changeme") + "')");

                statement.executeUpdate("create table if not exists system (id INTEGER PRIMARY KEY AUTOINCREMENT, display_nm string not null, user not null, host string not null, port INTEGER not null, authorized_keys string not null)");
                statement.executeUpdate("create table if not exists users (id INTEGER PRIMARY KEY AUTOINCREMENT, first_nm string not null, last_nm string not null, email string, public_key string)");
                statement.executeUpdate("create table if not exists profiles (id INTEGER PRIMARY KEY AUTOINCREMENT, nm string not null, desc string not null)");
                statement.executeUpdate("create table if not exists system_map (profile_id INTEGER, system_id INTEGER, foreign key (profile_id) references profiles(id) on delete cascade , foreign key (system_id) references system(id) on delete cascade, primary key (profile_id, system_id))");
                statement.executeUpdate("create table if not exists user_map (user_id INTEGER, profile_id INTEGER, foreign key (user_id) references users(id) on delete cascade, foreign key (profile_id) references profiles(id) on delete cascade, primary key (user_id, profile_id))");
                statement.executeUpdate("create table if not exists private_key (passphrase string unique not null)");
                statement.executeUpdate("create table if not exists system_key_gen (id INTEGER, auth_keys_val not null,status_cd string not null default 'I', foreign key (id) references system(id) on delete cascade)");

                //generate new key and insert passphrase
                statement.executeUpdate("insert or ignore into private_key (passphrase) values('" + SSHUtil.keyGen() + "')");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(statement);
            DBUtils.closeConn(connection);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
