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
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO to manage profile information
 */
public class ProfileSystemsDB {

    /**
     * adds a host system to profile
     * @param profileId profile id
     * @param systemId host system id
     */
    public static void addSystemToProfile(Long profileId, Long systemId) {


        Connection con = null;

        try {
            con = DBUtils.getConn();


            PreparedStatement stmt = con.prepareStatement("insert into system_map (profile_id, system_id) values (?,?)");
            stmt.setLong(1, profileId);
            stmt.setLong(2, systemId);

            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


    }

    /**
     * deletes all systems for a given profile
     * @param profileId profile id
     */
     public static void deleteAllSystemsFromProfile(Long profileId) {


        Connection con = null;


        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from system_map where profile_id=?");
            stmt.setLong(1, profileId);

            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


    }

    /**
     * deletes individual system for profile
     * @param profileId profile id
     * @param systemId host system id
     */
    public static void deleteSystemFromProfile(Long profileId, Long systemId) {


        Connection con = null;


        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from system_map where profile_id=? and system_id=?");
            stmt.setLong(1, profileId);
            stmt.setLong(2, systemId);

            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


    }


    /**
     * returns a list of systems for a given profile
     * @param profileId profile id
     * @return list of host systems
     */
    public static List<HostSystem> getSystemsByProfile(Long profileId) {


        Connection con = null;
        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
        try {
            con = DBUtils.getConn();
            hostSystemList = getSystemsByProfile(con, profileId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return hostSystemList;


    }

    /**
     * returns a list of systems for a given profile
     * @param con DB connection
     * @param profileId profile id
     * @return list of host systems
     */
    public static List<HostSystem> getSystemsByProfile(Connection con, Long profileId) {

        ArrayList<HostSystem> hostSystemList = new ArrayList<HostSystem>();


        try {
            PreparedStatement stmt = con.prepareStatement("select * from  system s, system_map m where s.id=m.system_id and m.profile_id=? order by display_nm asc");
            stmt.setLong(1, profileId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HostSystem hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setDisplayNm(rs.getString("display_nm"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setAuthorizedKeys(rs.getString("authorized_keys"));
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
     * returns a list of systems for a given profile
     * @param con DB connection
     * @param profileId profile id
     * @return list of host systems
     */
    public static List<Long> getSystemIdsByProfile(Connection con, Long profileId) {

        List<Long> systemIdList = new ArrayList<Long>();


        try {
            PreparedStatement stmt = con.prepareStatement("select * from  system s, system_map m where s.id=m.system_id and m.profile_id=? order by display_nm asc");
            stmt.setLong(1, profileId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                systemIdList.add(rs.getLong("id"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return systemIdList;
    }


    /**
     * returns a list of profiles for a given system
     * @param systemId system id
     * @return profile list
     */
    public static List<Profile> getProfilesBySystem(Long systemId) {


        Connection con = null;
        List<Profile> profileList = new ArrayList<Profile>();
        try {
            con = DBUtils.getConn();
             profileList = getProfilesBySystem(con, systemId);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return profileList;


    }


    /**
     * returns a list of profiles for a given system
     * @param con DB connection
     * @param systemId system id
     * @return profile list
     */
    public static List<Profile> getProfilesBySystem(Connection con, Long systemId) {

        ArrayList<Profile> profileList = new ArrayList<Profile>();


        try {
            PreparedStatement stmt = con.prepareStatement("select * from  profiles r, system_map m where r.id=m.profile_id and m.system_id=? order by nm asc");
            stmt.setLong(1, systemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Profile profile = new Profile();
                profile.setId(rs.getLong("id"));
                profile.setNm(rs.getString("nm"));
                profile.setDesc(rs.getString("desc"));
                profileList.add(profile);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return profileList;
    }

}
