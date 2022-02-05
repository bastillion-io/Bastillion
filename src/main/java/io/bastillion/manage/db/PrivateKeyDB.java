/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.ApplicationKey;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO that returns public / private key for the system generated private key
 */
public class PrivateKeyDB {

    private PrivateKeyDB() {
    }

    /**
     * returns public private key for application
     *
     * @return app key values
     */
    public static ApplicationKey getApplicationKey() throws SQLException, GeneralSecurityException {

        ApplicationKey appKey = null;

        Connection con = DBUtils.getConn();

        PreparedStatement stmt = con.prepareStatement("select * from  application_key");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {

            appKey = new ApplicationKey();
            appKey.setId(rs.getLong("id"));
            appKey.setPassphrase(EncryptionUtil.decrypt(rs.getString("passphrase")));
            appKey.setPrivateKey(EncryptionUtil.decrypt(rs.getString("private_key")));
            appKey.setPublicKey(rs.getString("public_key"));

        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return appKey;
    }
}
