/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.db;

import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.Profile;
import com.keybox.manage.model.SystemKeyGenStatus;
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
    public static List<SystemKeyGenStatus> setInitialSystemKeyGen(List<HostSystem> hostSystemList) {
        Connection con = null;
        List<SystemKeyGenStatus> systemKeyGenStatusList = new ArrayList<SystemKeyGenStatus>();
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
                SystemKeyGenStatus systemKeyGenStatus = new SystemKeyGenStatus();
                systemKeyGenStatus.setId(hostSystem.getId());
                systemKeyGenStatus.setAuthKeyVal(authKeyVal);
                systemKeyGenStatus.setStatusCd("I");

                //insert new status
                insertSystemKeyGen(con, systemKeyGenStatus);

                //get update status list
                systemKeyGenStatusList = getAllSystemKeyGen(con);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemKeyGenStatusList;
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
     * @param systemKeyGenStatus systems for authorized_keys replacement
     */
    private static void insertSystemKeyGen(Connection con, SystemKeyGenStatus systemKeyGenStatus) {

        try {

            PreparedStatement stmt = con.prepareStatement("insert into system_key_gen (id, auth_keys_val, status_cd) values (?,?,?)");
            stmt.setLong(1, systemKeyGenStatus.getId());
            stmt.setString(2, systemKeyGenStatus.getAuthKeyVal());
            stmt.setString(3, systemKeyGenStatus.getStatusCd());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * updates the system_key_gen table to keep track of key placement status
     *
     * @param systemKeyGenStatus systems for authorized_keys replacement
     */
    public static void updateSystemKeyGen(SystemKeyGenStatus systemKeyGenStatus) {

        Connection con = null;
        try {
            con = DBUtils.getConn();

            updateSystemKeyGen(con, systemKeyGenStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


    /**
     * updates the system_key_gen table to keep track of key placement status
     *
     * @param con                DB connection
     * @param systemKeyGenStatus systems for authorized_keys replacement
     */
    public static void updateSystemKeyGen(Connection con, SystemKeyGenStatus systemKeyGenStatus) {

        try {

            PreparedStatement stmt = con.prepareStatement("update system_key_gen set auth_keys_val=?, status_cd=? where id=?");
            stmt.setString(1, systemKeyGenStatus.getAuthKeyVal());
            stmt.setString(2, systemKeyGenStatus.getStatusCd());
            stmt.setLong(3, systemKeyGenStatus.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * returns all key placement statuses
     */
    public static List<SystemKeyGenStatus> getAllSystemKeyGen() {

        List<SystemKeyGenStatus> systemKeyGenStatusList = new ArrayList<SystemKeyGenStatus>();
        Connection con = null;
        try {
            con = DBUtils.getConn();
            systemKeyGenStatusList = getAllSystemKeyGen(con);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemKeyGenStatusList;

    }

    /**
     * returns all key placement statuses
     *
     * @param con DB connection object
     */
    private static List<SystemKeyGenStatus> getAllSystemKeyGen(Connection con) {

        List<SystemKeyGenStatus> systemKeyGenStatusList = new ArrayList<SystemKeyGenStatus>();
        try {

            PreparedStatement stmt = con.prepareStatement("select * from system_key_gen");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SystemKeyGenStatus systemKeyGenStatus = new SystemKeyGenStatus();
                systemKeyGenStatus.setId(rs.getLong("id"));
                systemKeyGenStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                systemKeyGenStatus.setStatusCd(rs.getString("status_cd"));
                systemKeyGenStatus.setHostSystem(SystemDB.getSystem(con, systemKeyGenStatus.getId()));
                systemKeyGenStatusList.add(systemKeyGenStatus);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return systemKeyGenStatusList;

    }


    /**
     * returns key placement status of system
     *
     * @param systemId system id
     */
    public static SystemKeyGenStatus getSystemKeyGen(Long systemId) {

        Connection con = null;
        SystemKeyGenStatus systemKeyGenStatus = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from system_key_gen where id=?");
            stmt.setLong(1, systemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                systemKeyGenStatus = new SystemKeyGenStatus();
                systemKeyGenStatus.setId(rs.getLong("id"));
                systemKeyGenStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                systemKeyGenStatus.setStatusCd(rs.getString("status_cd"));
                systemKeyGenStatus.setHostSystem(SystemDB.getSystem(con, systemKeyGenStatus.getId()));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemKeyGenStatus;


    }


    /**
     * returns the first system that authorized keys has not been tried
     *
     * @return systemKeyGenStatus systems for authorized_keys replacement
     */
    public static SystemKeyGenStatus getNextPendingSystem() {

        SystemKeyGenStatus systemKeyGenStatus = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from system_key_gen where status_cd like 'A' or status_cd like 'I'");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                systemKeyGenStatus = new SystemKeyGenStatus();
                systemKeyGenStatus.setId(rs.getLong("id"));
                systemKeyGenStatus.setAuthKeyVal(rs.getString("auth_keys_val"));
                systemKeyGenStatus.setStatusCd(rs.getString("status_cd"));
                systemKeyGenStatus.setHostSystem(SystemDB.getSystem(con, systemKeyGenStatus.getId()));

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemKeyGenStatus;

    }
}
