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

import com.keybox.manage.model.PublicKey;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.SSHUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO to manage public keys
 */
public class PublicKeyDB {

    public static final String PUBLIC_KEY = "public_key";
    private static Logger log = LoggerFactory.getLogger(PublicKeyDB.class);

    public static final String FILTER_BY_USER_ID = "user_id";
    public static final String PROFILE_ID = "profile_id";
    public static final String FILTER_BY_PROFILE_ID = PROFILE_ID;
    public static final String FILTER_BY_ENABLED= "enabled";

    public static final String KEY_NM = "key_nm";
    public static final String SORT_BY_KEY_NM = KEY_NM;
    public static final String SORT_BY_PROFILE = PROFILE_ID;
    public static final String SORT_BY_TYPE= "type";
    public static final String SORT_BY_FINGERPRINT= "fingerprint";
    public static final String CREATE_DT = "create_dt";
    public static final String SORT_BY_CREATE_DT= CREATE_DT;
    public static final String SORT_BY_USERNAME= "username";

    private PublicKeyDB() {
    }

    /**
     * Deletes all SSH keys for users that are not assigned in a profile
     *
     * @param con DB connection
     * @param userId user id
     */
    public static void deleteUnassignedKeysByUser(Connection con, Long userId){

        try {
            PreparedStatement stmt = con.prepareStatement("delete from public_keys where (profile_id is null or profile_id not in (select profile_id from user_map where user_id=?)) and user_id=?");
            stmt.setLong(1, userId);
            stmt.setLong(2, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    /**
     * Deletes all SSH keys for users that are not assigned in a profile
     *
     * @param con DB connection
     * @param profileId profile id
     */
    public static void deleteUnassignedKeysByProfile(Connection con, Long profileId){

        try {
            PreparedStatement stmt = con.prepareStatement("delete from public_keys where profile_id=? and user_id not in (select user_id from user_map where profile_id=?)");
            stmt.setLong(1, profileId);
            stmt.setLong(2, profileId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }
    
    /**
     * disables SSH key
     *
     * @param id key id
     */
    public static void disableKey(Long id){

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update public_keys set enabled=false where id=?");
            stmt.setLong(1, id);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
      * re-enables SSH key
      * 
      * @param id key id
     */
    public static void enableKey(Long id){

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update public_keys set enabled=true where id=?");
            stmt.setLong(1, id);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * checks fingerprint to determine if key is disabled
     * 
     * @param fingerprint public key fingerprint
     * @return true if disabled
     */
    public static boolean isKeyDisabled(String fingerprint) {
        boolean isDisabled=false;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from  public_keys where fingerprint like ? and enabled=false");
            stmt.setString(1, fingerprint);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                isDisabled=true;
            }
            
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return isDisabled;
    }


    /**
     * returns public keys based on sort order defined
     *
     * @param sortedSet object that defines sort order
     * @return sorted script list
     */
    public static SortedSet getPublicKeySet(SortedSet sortedSet) {

        ArrayList<PublicKey> publicKeysList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = " order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select p.*, u.username from public_keys p, users u where u.id=p.user_id  ";

        sql+= StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_USER_ID)) ? " and p.user_id=? " : "";
        sql+= StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) ? " and p.profile_id=? " : "";
        sql+= StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_ENABLED)) ? " and p.enabled=? " : " and p.enabled=true";
        sql=sql+orderBy;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            int i=1;
            //set filters in prepared statement
            if(StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_USER_ID))){
                stmt.setLong(i++, Long.parseLong(sortedSet.getFilterMap().get(FILTER_BY_USER_ID)));
            }
            if(StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID))){
                stmt.setLong(i++, Long.parseLong(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)));
            }
            if(StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_ENABLED))){
                stmt.setBoolean(i, Boolean.valueOf(sortedSet.getFilterMap().get(FILTER_BY_ENABLED)));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PublicKey publicKey = new PublicKey();
                publicKey.setId(rs.getLong("id"));
                publicKey.setKeyNm(rs.getString(KEY_NM));
                publicKey.setPublicKey(rs.getString(PUBLIC_KEY));
                publicKey.setProfile(ProfileDB.getProfile(con, rs.getLong(PROFILE_ID)));
                publicKey.setType(SSHUtil.getKeyType(publicKey.getPublicKey()));
                publicKey.setFingerprint(SSHUtil.getFingerprint(publicKey.getPublicKey()));
                publicKey.setCreateDt(rs.getTimestamp(CREATE_DT));
                publicKey.setUsername(rs.getString("username"));
                publicKey.setUserId(rs.getLong("user_id"));
                publicKey.setEnabled(rs.getBoolean("enabled"));
                publicKeysList.add(publicKey);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        sortedSet.setItemList(publicKeysList);
        return sortedSet;
    }

    /**
     * returns public keys based on sort order defined
     *
     * @param sortedSet object that defines sort order
     * @param userId user id
     * @return sorted script list
     */
    public static SortedSet getPublicKeySet(SortedSet sortedSet, Long userId) {

        ArrayList<PublicKey> publicKeysList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from public_keys where user_id = ? and enabled=true " + orderBy;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PublicKey publicKey = new PublicKey();
                publicKey.setId(rs.getLong("id"));
                publicKey.setKeyNm(rs.getString(KEY_NM));
                publicKey.setPublicKey(rs.getString(PUBLIC_KEY));
                publicKey.setProfile(ProfileDB.getProfile(con, rs.getLong(PROFILE_ID)));
                publicKey.setType(SSHUtil.getKeyType(publicKey.getPublicKey()));
                publicKey.setFingerprint(SSHUtil.getFingerprint(publicKey.getPublicKey()));
                publicKey.setCreateDt(rs.getTimestamp(CREATE_DT));
                publicKeysList.add(publicKey);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

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
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

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
                publicKey.setKeyNm(rs.getString(KEY_NM));
                publicKey.setPublicKey(rs.getString(PUBLIC_KEY));
                publicKey.setProfile(ProfileDB.getProfile(con, rs.getLong(PROFILE_ID)));
                publicKey.setType(rs.getString("type"));
                publicKey.setFingerprint(rs.getString("fingerprint"));
                publicKey.setCreateDt(rs.getTimestamp(CREATE_DT));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
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
            PreparedStatement stmt = con.prepareStatement("insert into public_keys(key_nm, type, fingerprint, public_key, profile_id, user_id) values (?,?,?,?,?,?)");
            stmt.setString(1, publicKey.getKeyNm());
            stmt.setString(2, SSHUtil.getKeyType(publicKey.getPublicKey()));
            stmt.setString(3, SSHUtil.getFingerprint(publicKey.getPublicKey()));
            stmt.setString(4, publicKey.getPublicKey().trim());
            if (publicKey.getProfile() == null || publicKey.getProfile().getId() == null) {
                stmt.setNull(5, Types.NULL);
            } else {
                stmt.setLong(5, publicKey.getProfile().getId());
            }
            stmt.setLong(6, publicKey.getUserId());
            stmt.execute();

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

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
            PreparedStatement stmt = con.prepareStatement("update public_keys set key_nm=?, type=?, fingerprint=?, public_key=?, profile_id=? where id=? and user_id=? and enabled=true");
            stmt.setString(1, publicKey.getKeyNm());
            stmt.setString(2, SSHUtil.getKeyType(publicKey.getPublicKey()));
            stmt.setString(3, SSHUtil.getFingerprint(publicKey.getPublicKey()));
            stmt.setString(4, publicKey.getPublicKey().trim());
            if (publicKey.getProfile() == null || publicKey.getProfile().getId() == null) {
                stmt.setNull(5, Types.NULL);
            } else {
                stmt.setLong(5, publicKey.getProfile().getId());
            }
            stmt.setLong(6, publicKey.getId());
            stmt.setLong(7, publicKey.getUserId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }


    /**
     * deletes public key
     *
     * @param publicKeyId key id
     * @param userId  user id
     */
    public static void deletePublicKey(Long publicKeyId, Long userId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from public_keys where id=? and user_id=? and enabled=true");
            stmt.setLong(1, publicKeyId);
            stmt.setLong(2, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * deletes all public keys for user
     *
     * @param userId  user id
     */
    public static void deleteUserPublicKeys(Long userId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from public_keys where user_id=? and enabled=true");
            stmt.setLong(1, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * deletes all public keys for a profile
     *
     * @param profileId profile id
     */
    public static void deleteProfilePublicKeys(Long profileId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from public_keys where profile_id=?");
            stmt.setLong(1, profileId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }


    public static List<String> getPublicKeysForSystem(Long systemId) {

        Connection con = null;
        List<String> publicKeyList = new ArrayList<>();
        try {
            con = DBUtils.getConn();

            publicKeyList = getPublicKeysForSystem(con, systemId);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return publicKeyList;


    }

    public static List<String> getPublicKeysForSystem(Connection con, Long systemId) {
        List<String> publicKeyList = new ArrayList<>();

        if(systemId==null){
            systemId=-99L;
        }
        try {
            PreparedStatement stmt = con.prepareStatement("select * from public_keys where (profile_id is null or profile_id in (select profile_id from system_map where system_id=?)) and enabled=true");
            stmt.setLong(1, systemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                publicKeyList.add(rs.getString(PUBLIC_KEY));
            }
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return publicKeyList;


    }
    
    /**
     * checks if key has already been registered under user's profile
     *
     * @param userId user id
     * @param publicKey public key 
     * @return true if duplicate
     */
    public static boolean isKeyRegistered(Long userId, PublicKey publicKey) {
        boolean isDuplicate=false;
        PreparedStatement stmt;
        Connection con = null;
        try {
          con = DBUtils.getConn();

          stmt = con.prepareStatement("select * from public_keys where user_id=? and fingerprint like ? and profile_id is ? and id is not ?");
          stmt.setLong(1, userId);
          stmt.setString(2, SSHUtil.getFingerprint(publicKey.getPublicKey()));
          if(publicKey.getProfile()!=null && publicKey.getProfile().getId()!=null){
            stmt.setLong(3, publicKey.getProfile().getId());
          } else {
            stmt.setNull(3,Types.NULL);
          }
          if(publicKey.getId()!=null ){
            stmt.setLong(4, publicKey.getId());
          } else {
            stmt.setNull(4,Types.NULL);
          }

          ResultSet rs = stmt.executeQuery();
          if (rs.next()) {
              isDuplicate=true;
          }
          DBUtils.closeRs(rs);
          DBUtils.closeStmt(stmt);

        } catch (Exception ex) {
            log.error(ex.toString(), ex);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return isDuplicate;
    }

    /**
     * select all unique public keys for user
     *
     * @param userId user id
     * @return public  key list for user
     */
    public static List<PublicKey> getUniquePublicKeysForUser(Long userId) {


        Connection con = null;
        Map<String, PublicKey> keyMap = new LinkedHashMap();
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from public_keys where user_id=? and enabled=true order by key_nm asc");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){

                PublicKey publicKey = new PublicKey();
                publicKey.setId(rs.getLong("id"));
                publicKey.setKeyNm(rs.getString(KEY_NM));
                publicKey.setPublicKey(rs.getString(PUBLIC_KEY));
                publicKey.setProfile(ProfileDB.getProfile(con, rs.getLong(PROFILE_ID)));
                publicKey.setType(SSHUtil.getKeyType(publicKey.getPublicKey()));
                publicKey.setFingerprint(SSHUtil.getFingerprint(publicKey.getPublicKey()));
                publicKey.setCreateDt(rs.getTimestamp(CREATE_DT));
                keyMap.put(publicKey.getKeyNm() + " (" + publicKey.getFingerprint() + ")", publicKey);
                
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return new ArrayList(keyMap.values());

    }
}
