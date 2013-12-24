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
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.model.User;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.SSHUtil;
import org.apache.commons.lang3.StringUtils;

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
     * set the initial status for selected systems
     *
     * @param hostSystemList systems ids to set initial status
     * @param userId user id
     */
    public static void setInitialSystemStatusByHostSystem(List<HostSystem> hostSystemList, Long userId) {
        Connection con = null;
        try {
            con = DBUtils.getConn();

            //deletes all old systems
            deleteAllSystemStatus(con,userId);
            for (HostSystem hostSystem : hostSystemList) {

                hostSystem.setId(hostSystem.getId());
                hostSystem.setStatusCd(HostSystem.INITIAL_STATUS);

                //insert new status
                insertSystemStatus(con, hostSystem, userId);



            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
    }


    /**
     * set the initial status for selected systems
     *
     * @param systemSelectIds systems ids to set initial status
     * @param userId user id
     */
    public static void setInitialSystemStatus(List<Long> systemSelectIds, Long userId) {
        Connection con = null;
        try {
            con = DBUtils.getConn();

            //deletes all old systems
            deleteAllSystemStatus(con,userId);
            for (Long hostSystemId : systemSelectIds) {

                HostSystem hostSystem= new HostSystem();
                hostSystem.setId(hostSystemId);
                hostSystem.setStatusCd(HostSystem.INITIAL_STATUS);

                //insert new status
                insertSystemStatus(con, hostSystem, userId);



            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
    }

    /**
     * deletes all records from status table for user
     *
     * @param con DB connection object
     * @param userId user id
     */
    private static void deleteAllSystemStatus(Connection con, Long userId) {

        try {

            PreparedStatement stmt = con.prepareStatement("delete from status where user_id=?");
            stmt.setLong(1,userId);
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
     * @param hostSystem systems for authorized_keys replacement
     * @param userId user id
     */
    private static void insertSystemStatus(Connection con, HostSystem hostSystem, Long userId) {

        try {

            PreparedStatement stmt = con.prepareStatement("insert into status (id, status_cd, user_id) values (?,?,?)");
            stmt.setLong(1, hostSystem.getId());
            stmt.setString(2, hostSystem.getStatusCd());
            stmt.setLong(3, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * updates the status table to keep track of key placement status
     *
     * @param hostSystem systems for authorized_keys replacement
     * @param userId user id
     */
    public static void updateSystemStatus(HostSystem hostSystem, Long userId) {

        Connection con = null;
        try {
            con = DBUtils.getConn();

            updateSystemStatus(con, hostSystem, userId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


    /**
     * updates the status table to keep track of key placement status
     *
     * @param con                DB connection
     * @param hostSystem systems for authorized_keys replacement
     * @param userId user id
     */
    public static void updateSystemStatus(Connection con, HostSystem hostSystem, Long userId) {

        try {

            PreparedStatement stmt = con.prepareStatement("update status set status_cd=? where id=? and user_id=?");
            stmt.setString(1, hostSystem.getStatusCd());
            stmt.setLong(2, hostSystem.getId());
            stmt.setLong(3, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * returns all key placement statuses
     * @param userId user id
     */
    public static SortedSet getSortedSetStatus(Long userId){

        SortedSet sortedSet= new SortedSet();

        sortedSet.setItemList(getAllSystemStatus(userId));
        return sortedSet;

    }
    /**
     * returns all key placement statuses
     * @param userId user id
     */
    public static List<HostSystem> getAllSystemStatus(Long userId) {

        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
        Connection con = null;
        try {
            con = DBUtils.getConn();
            hostSystemList = getAllSystemStatus(con, userId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return hostSystemList;

    }

    /**
     * returns all key placement statuses
     *
     * @param con DB connection object
     * @param userId user id
     */
    private static List<HostSystem> getAllSystemStatus(Connection con, Long userId) {

        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
        try {

            PreparedStatement stmt = con.prepareStatement("select * from status where user_id=? order by id asc");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HostSystem hostSystem = SystemDB.getSystem(con, rs.getLong("id"));
                hostSystem.setStatusCd(rs.getString("status_cd"));
                hostSystemList.add(hostSystem);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostSystemList;

    }


    /**
     * returns key placement status of system
     *
     * @param systemId system id
     * @param userId user id
     */
    public static HostSystem getSystemStatus(Long systemId, Long userId) {

        Connection con = null;
        HostSystem hostSystem = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from status where id=? and user_id=?");
            stmt.setLong(1, systemId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hostSystem = SystemDB.getSystem(con, rs.getLong("id"));
                hostSystem.setStatusCd(rs.getString("status_cd"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return hostSystem;


    }


    /**
     * returns the first system that authorized keys has not been tried
     *
     * @param userId user id
     * @return hostSystem systems for authorized_keys replacement
     */
    public static HostSystem getNextPendingSystem(Long userId) {

        HostSystem hostSystem = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from status where (status_cd like ? or status_cd like ? or status_cd like ?) and user_id=? order by id asc");
            stmt.setString(1,HostSystem.INITIAL_STATUS);
            stmt.setString(2,HostSystem.AUTH_FAIL_STATUS);
            stmt.setString(3,HostSystem.PUBLIC_KEY_FAIL_STATUS);
            stmt.setLong(4, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                hostSystem = SystemDB.getSystem(con, rs.getLong("id"));
                hostSystem.setStatusCd(rs.getString("status_cd"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return hostSystem;

    }

    //todo
    public static List<Long> getHostSystemsIdsByKey(List<Long> keyIds){
       List<Long> systemIdList= new ArrayList<Long>();
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select unique system_id from system_map where profile_id in (select unique profile_id from public_keys where id in (?))");
            stmt.setString(1, StringUtils.join(keyIds,","));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
               systemIdList.add(rs.getLong("id"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return systemIdList;

    }






}





