/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
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
