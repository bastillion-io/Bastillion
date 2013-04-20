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
public class SystemStatusDB {


    /**
     * returns a list of host systems based on a profile
     *
     * @param profileIdList list of profile ids
     * @return list of host systems with arrays of public keys
     */
    public static List<HostSystem> findAuthKeysForProfile(List<Long> profileIdList) {


        Connection con = null;
        List<HostSystem> hostSystemListReturn = new ArrayList<HostSystem>();

        try {
            con = DBUtils.getConn();
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
     * set the initial status for selected systems
     *
     * @param hostSystemList systems to set initial status
     */
    public static List<SystemStatus> setInitialSystemStatus(List<HostSystem> hostSystemList) {
        Connection con = null;
        List<SystemStatus> systemStatusList = new ArrayList<SystemStatus>();
        try {
            con = DBUtils.getConn();

            //deletes all old systems
            deleteAllSystemStatus(con);
            for (HostSystem hostSystem : hostSystemList) {

                //create auth keys file
                String authKeyVal = "";
                for (String pubKey : hostSystem.getPublicKeyList()) {
                    authKeyVal = authKeyVal + pubKey + "\n";
                }
                SystemStatus systemStatus = new SystemStatus();
                systemStatus.setId(hostSystem.getId());
                systemStatus.setAuthKeyVal(authKeyVal);
                systemStatus.setStatusCd(SystemStatus.INITIAL_STATUS);

                //insert new status
                insertSystemStatus(con, systemStatus);

                //get update status list
                systemStatusList = getAllSystemStatus(con);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemStatusList;
    }

    /**
     * deletes all records from status table
     *
     * @param con DB connection object
     */
    private static void deleteAllSystemStatus(Connection con) {

        try {

            PreparedStatement stmt = con.prepareStatement("delete from status");
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * inserts into the status table to keep track of key placement status
     *
     * @param con                DB connection object
     * @param systemStatus systems for authorized_keys replacement
     */
    private static void insertSystemStatus(Connection con, SystemStatus systemStatus) {

        try {

            PreparedStatement stmt = con.prepareStatement("insert into status (id, auth_keys_val, status_cd) values (?,?,?)");
            stmt.setLong(1, systemStatus.getId());
            stmt.setString(2, systemStatus.getAuthKeyVal());
            stmt.setString(3, systemStatus.getStatusCd());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * updates the status table to keep track of key placement status
     *
     * @param systemStatus systems for authorized_keys replacement
     */
    public static void updateSystemStatus(SystemStatus systemStatus) {

        Connection con = null;
        try {
            con = DBUtils.getConn();

            updateSystemStatus(con, systemStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


    /**
     * updates the status table to keep track of key placement status
     *
     * @param con                DB connection
     * @param systemStatus systems for authorized_keys replacement
     */
    public static void updateSystemStatus(Connection con, SystemStatus systemStatus) {

        try {

            PreparedStatement stmt = con.prepareStatement("update status set auth_keys_val=?, status_cd=? where id=?");
            stmt.setString(1, systemStatus.getAuthKeyVal());
            stmt.setString(2, systemStatus.getStatusCd());
            stmt.setLong(3, systemStatus.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * returns all key placement statuses
     */
    public static List<SystemStatus> getAllSystemStatus() {

        List<SystemStatus> systemStatusList = new ArrayList<SystemStatus>();
        Connection con = null;
        try {
            con = DBUtils.getConn();
            systemStatusList = getAllSystemStatus(con);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemStatusList;

    }

    /**
     * returns all key placement statuses
     *
     * @param con DB connection object
     */
    private static List<SystemStatus> getAllSystemStatus(Connection con) {

        List<SystemStatus> systemStatusList = new ArrayList<SystemStatus>();
        try {

            PreparedStatement stmt = con.prepareStatement("select * from status");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SystemStatus systemStatus = new SystemStatus();
                systemStatus.setId(rs.getLong("id"));
                systemStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                systemStatus.setStatusCd(rs.getString("status_cd"));
                systemStatus.setHostSystem(SystemDB.getSystem(con, systemStatus.getId()));
                systemStatusList.add(systemStatus);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return systemStatusList;

    }


    /**
     * returns key placement status of system
     *
     * @param systemId system id
     */
    public static SystemStatus getSystemStatus(Long systemId) {

        Connection con = null;
        SystemStatus systemStatus = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from status where id=?");
            stmt.setLong(1, systemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                systemStatus = new SystemStatus();
                systemStatus.setId(rs.getLong("id"));
                systemStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                systemStatus.setStatusCd(rs.getString("status_cd"));
                systemStatus.setHostSystem(SystemDB.getSystem(con, systemStatus.getId()));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemStatus;


    }


    /**
     * returns the first system that authorized keys has not been tried
     *
     * @return systemStatus systems for authorized_keys replacement
     */
    public static SystemStatus getNextPendingSystem() {

        SystemStatus systemStatus = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from status where status_cd like ? or status_cd like ? or status_cd like ?");
            stmt.setString(1,SystemStatus.INITIAL_STATUS);
            stmt.setString(2,SystemStatus.AUTH_FAIL_STATUS);
            stmt.setString(3,SystemStatus.PUBLIC_KEY_FAIL_STATUS);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                systemStatus = new SystemStatus();
                systemStatus.setId(rs.getLong("id"));
                systemStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                systemStatus.setStatusCd(rs.getString("status_cd"));
                systemStatus.setHostSystem(SystemDB.getSystem(con, systemStatus.getId()));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemStatus;

    }

}
