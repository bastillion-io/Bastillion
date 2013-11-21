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


import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.PublicKey;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO to manage public keys
 */
public class PublicKeyDB {


    public static final String SORT_BY_KEY_NM = "key_nm";
    public static final String SORT_BY_PROFILE = "profile_id";


    /**
     * returns public keys based on sort order defined
     *
     * @param sortedSet object that defines sort order
     * @return sorted script list
     */
    public static SortedSet getPublicKeySet(SortedSet sortedSet) {

        ArrayList<PublicKey> publicKeysList = new ArrayList<PublicKey>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from public_keys " + orderBy;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PublicKey publicKey = new PublicKey();
                publicKey.setId(rs.getLong("id"));
                publicKey.setKeyNm(rs.getString("key_nm"));
                publicKey.setPublicKey(rs.getString("public_key"));
                publicKey.setProfile(ProfileDB.getProfile(con, rs.getLong("profile_id")));
                publicKeysList.add(publicKey);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        sortedSet.setItemList(publicKeysList);
        return sortedSet;
    }


    /**
     * returns public key base on id
     *
     * @param publicKeyId key id
     * @return script object
     */
    public static PublicKey getPublicKey(Long publicKeyId) {

        PublicKey publicKey = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            publicKey = getPublicKey(con, publicKeyId);


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return publicKey;
    }

    /**
     * returns public key base on id
     *
     * @param con         DB connection
     * @param publicKeyId key id
     * @return script object
     */
    public static PublicKey getPublicKey(Connection con, Long publicKeyId) {

        PublicKey publicKey = null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from  public_keys where id=?");
            stmt.setLong(1, publicKeyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                publicKey = new PublicKey();
                publicKey.setId(rs.getLong("id"));
                publicKey.setKeyNm(rs.getString("key_nm"));
                publicKey.setPublicKey(rs.getString("public_key"));
                publicKey.setProfile(ProfileDB.getProfile(con, rs.getLong("profile_id")));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return publicKey;
    }

    /**
     * inserts new public key
     *
     * @param publicKey key object
     */
    public static void insertPublicKey(PublicKey publicKey) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("insert into public_keys(key_nm, public_key, profile_id) values (?,?,?)");
            stmt.setString(1, publicKey.getKeyNm());
            stmt.setString(2, publicKey.getPublicKey());
            if (publicKey.getProfile() == null || publicKey.getProfile().getId() == null) {
                stmt.setNull(3, Types.NULL);
            } else {
                stmt.setLong(3, publicKey.getProfile().getId());
            }
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * updates existing public key
     *
     * @param publicKey key object
     */
    public static void updatePublicKey(PublicKey publicKey) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update public_keys set key_nm=?, public_key=?, profile_id=? where id=?");
            stmt.setString(1, publicKey.getKeyNm());
            stmt.setString(2, publicKey.getPublicKey());
            if (publicKey.getProfile() == null || publicKey.getProfile().getId() == null) {
                stmt.setNull(3, Types.NULL);
            } else {
                stmt.setLong(3, publicKey.getProfile().getId());
            }
            stmt.setLong(4, publicKey.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * deletes public key
     *
     * @param publicKeyId key id
     */
    public static void deletePublicKey(Long publicKeyId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from public_keys where id=?");
            stmt.setLong(1, publicKeyId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    public static List<Long> getSystemsByPublicKey(List<Long> publicKeyIdList) {

        List<Long> systemIdList = new ArrayList<Long>();

        Connection con = null;
        try {
            con = DBUtils.getConn();

            systemIdList = getSystemsByPublicKey(con, publicKeyIdList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return systemIdList;


    }


    public static List<Long> getSystemsByPublicKey(Connection con, List<Long> publicKeyIdList) {

        List<Long> systemIdList = new ArrayList<Long>();

        try {

            for (Long publicKeyId : publicKeyIdList) {

                PreparedStatement stmt = con.prepareStatement("select * from public_keys where id=?");
                stmt.setLong(1, publicKeyId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Long profileId = rs.getLong("profile_id");
                    if (profileId != null) {
                        systemIdList.addAll(ProfileSystemsDB.getSystemIdsByProfile(con, publicKeyId));
                    }
                    else {
                        systemIdList.addAll(SystemDB.getAllSystemIds(con));
                        break;
                    }
                }
                DBUtils.closeStmt(stmt);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return systemIdList;


    }




    public static List<String> getPublicKeysForSystem(Long systemId) {

        Connection con = null;
        List<String> publicKeyList = new ArrayList<String>();
        try {
            con = DBUtils.getConn();

            publicKeyList = getPublicKeysForSystem(con, systemId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return publicKeyList;


    }

    public static List<String> getPublicKeysForSystem(Connection con, Long systemId) {
        List<String> publicKeyList = new ArrayList<String>();

        if(systemId==null){
            systemId=-99L;
        }
        try {
            PreparedStatement stmt = con.prepareStatement("select * from public_keys where profile_id is null or profile_id in (select profile_id from system_map where system_id=?)");
            stmt.setLong(1, systemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                publicKeyList.add(rs.getString("public_key"));
            }
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return publicKeyList;


    }
}
