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
package com.keybox.manage.db;

import com.keybox.manage.model.ApplicationKey;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO that returns public / private key for the system generated private key
 */
public class PrivateKeyDB {


    /**
     * returns public private key for applcation
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
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return appKey;
    }





}
