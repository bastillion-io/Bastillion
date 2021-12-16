/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.ApplicationKey;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO that returns public / private key for the system generated private key
 */
public class PrivateKeyDB {

    private static Logger log = LoggerFactory.getLogger(PrivateKeyDB.class);

    private PrivateKeyDB() {
    }

    /**
     * returns public private key for application
     * @return app key values
     */
    public static ApplicationKey getApplicationKey() {

        ApplicationKey appKey = null;

        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from  application_key");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                appKey= new ApplicationKey();
                appKey.setId(rs.getLong("id"));
                appKey.setPassphrase(EncryptionUtil.decrypt(rs.getString("passphrase")));
                appKey.setPrivateKey(EncryptionUtil.decrypt(rs.getString("private_key")));
                appKey.setPublicKey(rs.getString("public_key"));

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return appKey;
    }





}
