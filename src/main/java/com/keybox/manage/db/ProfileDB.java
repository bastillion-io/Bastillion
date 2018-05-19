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
package com.keybox.manage.db;

import com.keybox.manage.model.Profile;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * DAO to manage profile
 */
public class ProfileDB {

    private static Logger log = LoggerFactory.getLogger(ProfileDB.class);

    public static final String FILTER_BY_SYSTEM = "system";
    public static final String FILTER_BY_USER = "user";
    public static final String SORT_BY_PROFILE_NM="nm";

    private ProfileDB() {
    }

    /**
     * method to do order by based on the sorted set object for profiles
     * @return list of profiles
     */
    public static SortedSet getProfileSet(SortedSet sortedSet) {

        ArrayList<Profile> profileList = new ArrayList<>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = " order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select distinct p.* from  profiles p ";
        if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM))) {
           sql = sql + ", system_map m, system s where m.profile_id = p.id and m.system_id = s.id" +
                   " and (lower(s.display_nm) like ? or lower(s.host) like ?)";
        } else if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_USER))) {
            sql = sql + ", user_map m, users u where m.profile_id = p.id and m.user_id = u.id" +
                    " and (lower(u.first_nm) like ? or lower(u.last_nm) like ?" +
                    " or lower(u.email) like ? or lower(u.username) like ?)";
        }
        sql = sql + orderBy;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_SYSTEM))) {
                stmt.setString(1, "%" + sortedSet.getFilterMap().get(FILTER_BY_SYSTEM).toLowerCase() + "%");
                stmt.setString(2, "%" + sortedSet.getFilterMap().get(FILTER_BY_SYSTEM).toLowerCase() + "%");
            } else if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_USER))) {
                stmt.setString(1, "%" + sortedSet.getFilterMap().get(FILTER_BY_USER).toLowerCase() + "%");
                stmt.setString(2, "%" + sortedSet.getFilterMap().get(FILTER_BY_USER).toLowerCase() + "%");
                stmt.setString(3, "%" + sortedSet.getFilterMap().get(FILTER_BY_USER).toLowerCase() + "%");
                stmt.setString(4, "%" + sortedSet.getFilterMap().get(FILTER_BY_USER).toLowerCase() + "%");
            }
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
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        sortedSet.setItemList(profileList);
        return sortedSet;
    }


    /**
     * returns all profile information
     *
     * @return list of profiles
     */
    public static List<Profile> getAllProfiles() {

        ArrayList<Profile> profileList = new ArrayList<>();
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from  profiles order by nm asc");
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
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return profileList;
    }

    /**
     * returns profile based on id
     *
     * @param profileId profile id
     * @return profile
     */
    public static Profile getProfile(Long profileId) {

        Profile profile = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
           profile=getProfile(con, profileId);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return profile;
    }

    /**
     * returns profile based on id
     *
     * @param con db connection object
     * @param profileId profile id
     * @return profile
     */
    public static Profile getProfile(Connection con, Long profileId) {

        Profile profile = null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from profiles where id=?");
            stmt.setLong(1, profileId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                profile = new Profile();
                profile.setId(rs.getLong("id"));
                profile.setNm(rs.getString("nm"));
                profile.setDesc(rs.getString("desc"));
                profile.setHostSystemList(ProfileSystemsDB.getSystemsByProfile(con, profileId));

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return profile;
    }

    /**
     * inserts new profile
     *
     * @param profile profile object
     */
    public static void insertProfile(Profile profile) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("insert into profiles (nm, desc) values (?,?)");
            stmt.setString(1, profile.getNm());
            stmt.setString(2, profile.getDesc());
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
     * updates profile
     *
     * @param profile profile object
     */
    public static void updateProfile(Profile profile) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update profiles set nm=?, desc=? where id=?");
            stmt.setString(1, profile.getNm());
            stmt.setString(2, profile.getDesc());
            stmt.setLong(3, profile.getId());
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
     * deletes profile
     *
     * @param profileId profile id
     */
    public static void deleteProfile(Long profileId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from profiles where id=?");
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


}
