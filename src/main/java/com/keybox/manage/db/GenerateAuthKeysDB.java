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
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.SystemStatus;
import com.keybox.manage.model.User;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.SSHUtil;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO used to generate a list of public keys and systems associated
 * with them based on system profiles and users assigned to the profiles.
 */
public class GenerateAuthKeysDB {


    /**
     * returns the host systems and an array of public keys
     *
     * @param userIdList list of user ids
     * @return host system with array of public keys
     */
    public static List<HostSystem> findAuthKeysForUsers(List<Long> userIdList) {


        Connection con = null;
        List<HostSystem> hostSystemListReturn = new ArrayList<HostSystem>();

        try {
            con = DBUtils.getConn();

            List<Long> profileIdList = new ArrayList<Long>();
            for (Long userId : userIdList) {

                //get user
                User user = UserDB.getUser(con, userId);

                //get profiles for user
                for (Profile profile : user.getProfileList()) {
                    //add profiles to list if not already defined
                    if (!profileIdList.contains(profile.getId())) {
                        profileIdList.add(profile.getId());
                    }
                }

            }

            //get host systems based on profile
            hostSystemListReturn = findAuthKeysForProfile(con, profileIdList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return hostSystemListReturn;

    }


    /**
     * returns the host systems and an array of public keys
     *
     * @param systemIdList list of host system ids
     * @return host system with array of public keys
     */
    public static List<HostSystem> findAuthKeysForSystems(List<Long> systemIdList) {


        Connection con = null;
        List<HostSystem> hostSystemListReturn = new ArrayList<HostSystem>();

        try {
            con = DBUtils.getConn();
            for (Long systemId : systemIdList) {
                HostSystem hostSystem = findAuthKeysForSystem(con, systemId);
                hostSystemListReturn.add(hostSystem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return hostSystemListReturn;

    }

    /**
     * returns the host system and an array of public keys
     *
     * @param systemId the host system id
     * @return host system with array of public keys
     */
    public static HostSystem findAuthKeysForSystem(Long systemId) {

        HostSystem hostSystem = null;
        Connection con = null;

        try {
            con = DBUtils.getConn();
            hostSystem = findAuthKeysForSystem(con, systemId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        return hostSystem;

    }


    /**
     * returns the host system and an array of public keys
     *
     * @param con      DB connection
     * @param systemId the host system id
     * @return host system with array of public keys
     */
    private static HostSystem findAuthKeysForSystem(Connection con, Long systemId) {


        HostSystem hostSystem = null;

        try {


            //get host system
            hostSystem = SystemDB.getSystem(con, systemId);


            //get profiles associated with host system
            List<Profile> profileList = ProfileSystemsDB.getProfilesBySystem(con, systemId);
            Map<Long, User> userMap = new HashMap<Long, User>();
            for (Profile profile : profileList) {

                //get users associated with profile
                List<User> userList = UserProfileDB.getUsersByProfile(con, profile.getId());

                for (User user : userList) {
                    if (!userMap.containsKey(user.getId())) {
                        userMap.put(user.getId(), user);
                    }

                }
            }
            List<User> userList = new ArrayList<User>(userMap.values());
            //get public keys from user list
            hostSystem.setPublicKeyList(getPublicKeysFromUserList(userList));


        } catch (Exception e) {
            e.printStackTrace();
        }


        return hostSystem;

    }

    /**
     * returns a list of host systems based on a profile
     *
     * @param con        DB connection
     * @param profileIdList list of profile ids
     * @return list of host systems with arrays of public keys
     */
    private static List<HostSystem> findAuthKeysForProfile(Connection con, List<Long> profileIdList) {


        Map<Long, HostSystem> hostSystemMap = new HashMap<Long, HostSystem>();
        try {
            for (Long profileId : profileIdList) {

                //get host systems assigned to profile
                List<HostSystem> hostSystemList = ProfileSystemsDB.getSystemsByProfile(con, profileId);
                for (HostSystem hostSystem : hostSystemList) {
                    if (!hostSystemMap.containsKey(hostSystem.getId())) {

                        //get users associated with profile
                        hostSystem = findAuthKeysForSystem(con, hostSystem.getId());
                        hostSystemMap.put(hostSystem.getId(), hostSystem);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        List<HostSystem> hostSystemList = new ArrayList<HostSystem>(hostSystemMap.values());
        return hostSystemList;

    }


    /**
     * returns list of public keys for users and the system public key
     *
     * @param userList user list
     * @return list of public keys
     */
    private static List<String> getPublicKeysFromUserList(List<User> userList) {

        List<String> publicKeyList = new ArrayList<String>();
        //add keybox public key
        if (SSHUtil.getPublicKey() != null) {
            publicKeyList.add(SSHUtil.getPublicKey().replace("\n", "").trim());
        }
        //add user's public key
        for (User user : userList) {
            if (user.getPublicKey() != null) {
                publicKeyList.add(user.getPublicKey().replace("\n", "").trim());
            }
        }

        return publicKeyList;
    }


    /**
     * set the initial system_key_gen for selected systems
     *
     * @param hostSystemList systems to set initial status
     */
    public static List<SystemStatus> setInitialSystemKeyGen(List<HostSystem> hostSystemList) {
        Connection con = null;
        List<SystemStatus> SystemStatusList = new ArrayList<SystemStatus>();
        try {
            con = DBUtils.getConn();

            //deletes all old systems
            deleteAllSystemKeyGen(con);
            for (HostSystem hostSystem : hostSystemList) {

                //create auth keys file
                String authKeyVal = "";
                for (String pubKey : hostSystem.getPublicKeyList()) {
                    authKeyVal = authKeyVal + pubKey + "\n";
                }
                SystemStatus SystemStatus = new SystemStatus();
                SystemStatus.setId(hostSystem.getId());
                SystemStatus.setAuthKeyVal(authKeyVal);
                SystemStatus.setStatusCd(SystemStatus.INITIAL_STATUS);

                //insert new status
                insertSystemKeyGen(con, SystemStatus);

                //get update status list
                SystemStatusList = getAllSystemKeyGen(con);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return SystemStatusList;
    }

    /**
     * deletes all records from system_key_gen table
     *
     * @param con DB connection object
     */
    private static void deleteAllSystemKeyGen(Connection con) {

        try {

            PreparedStatement stmt = con.prepareStatement("delete from system_key_gen");
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * inserts into the system_key_gen table to keep track of key placement status
     *
     * @param con                DB connection object
     * @param SystemStatus systems for authorized_keys replacement
     */
    private static void insertSystemKeyGen(Connection con, SystemStatus SystemStatus) {

        try {

            PreparedStatement stmt = con.prepareStatement("insert into system_key_gen (id, auth_keys_val, status_cd) values (?,?,?)");
            stmt.setLong(1, SystemStatus.getId());
            stmt.setString(2, SystemStatus.getAuthKeyVal());
            stmt.setString(3, SystemStatus.getStatusCd());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * updates the system_key_gen table to keep track of key placement status
     *
     * @param SystemStatus systems for authorized_keys replacement
     */
    public static void updateSystemKeyGen(SystemStatus SystemStatus) {

        Connection con = null;
        try {
            con = DBUtils.getConn();

            updateSystemKeyGen(con, SystemStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


    /**
     * updates the system_key_gen table to keep track of key placement status
     *
     * @param con                DB connection
     * @param SystemStatus systems for authorized_keys replacement
     */
    public static void updateSystemKeyGen(Connection con, SystemStatus SystemStatus) {

        try {

            PreparedStatement stmt = con.prepareStatement("update system_key_gen set auth_keys_val=?, status_cd=? where id=?");
            stmt.setString(1, SystemStatus.getAuthKeyVal());
            stmt.setString(2, SystemStatus.getStatusCd());
            stmt.setLong(3, SystemStatus.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * returns all key placement statuses
     */
    public static List<SystemStatus> getAllSystemKeyGen() {

        List<SystemStatus> SystemStatusList = new ArrayList<SystemStatus>();
        Connection con = null;
        try {
            con = DBUtils.getConn();
            SystemStatusList = getAllSystemKeyGen(con);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return SystemStatusList;

    }

    /**
     * returns all key placement statuses
     *
     * @param con DB connection object
     */
    private static List<SystemStatus> getAllSystemKeyGen(Connection con) {

        List<SystemStatus> SystemStatusList = new ArrayList<SystemStatus>();
        try {

            PreparedStatement stmt = con.prepareStatement("select * from system_key_gen");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SystemStatus SystemStatus = new SystemStatus();
                SystemStatus.setId(rs.getLong("id"));
                SystemStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                SystemStatus.setStatusCd(rs.getString("status_cd"));
                SystemStatus.setHostSystem(SystemDB.getSystem(con, SystemStatus.getId()));
                SystemStatusList.add(SystemStatus);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemStatusList;

    }


    /**
     * returns key placement status of system
     *
     * @param systemId system id
     */
    public static SystemStatus getSystemKeyGen(Long systemId) {

        Connection con = null;
        SystemStatus SystemStatus = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from system_key_gen where id=?");
            stmt.setLong(1, systemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SystemStatus = new SystemStatus();
                SystemStatus.setId(rs.getLong("id"));
                SystemStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                SystemStatus.setStatusCd(rs.getString("status_cd"));
                SystemStatus.setHostSystem(SystemDB.getSystem(con, SystemStatus.getId()));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return SystemStatus;


    }


    /**
     * returns the first system that authorized keys has not been tried
     *
     * @return SystemStatus systems for authorized_keys replacement
     */
    public static SystemStatus getNextPendingSystem() {

        SystemStatus SystemStatus = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from system_key_gen where status_cd like 'A' or status_cd like 'I'");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                SystemStatus = new SystemStatus();
                SystemStatus.setId(rs.getLong("id"));
                SystemStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                SystemStatus.setStatusCd(rs.getString("status_cd"));
                SystemStatus.setHostSystem(SystemDB.getSystem(con, SystemStatus.getId()));

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return SystemStatus;

    }
}
