/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.Profile;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.DBUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * DAO to manage profile
 */
public class ProfileDB {

    public static final String FILTER_BY_SYSTEM = "system";
    public static final String FILTER_BY_USER = "username";
    public static final String SORT_BY_PROFILE_NM = "nm";

    private ProfileDB() {
    }

    /**
     * method to do order by based on the sorted set object for profiles
     *
     * @return list of profiles
     */
    public static SortedSet getProfileSet(SortedSet sortedSet) throws SQLException, GeneralSecurityException {

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

        Connection con = DBUtils.getConn();
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
        DBUtils.closeConn(con);

        sortedSet.setItemList(profileList);
        return sortedSet;
    }


    /**
     * returns all profile information
     *
     * @return list of profiles
     */
    public static List<Profile> getAllProfiles() throws SQLException, GeneralSecurityException {

        ArrayList<Profile> profileList = new ArrayList<>();
        Connection con = DBUtils.getConn();
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
        DBUtils.closeConn(con);

        return profileList;
    }

    /**
     * returns profile based on id
     *
     * @param profileId profile id
     * @return profile
     */
    public static Profile getProfile(Long profileId) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        Profile profile = getProfile(con, profileId);
        DBUtils.closeConn(con);

        return profile;
    }

    /**
     * returns profile based on id
     *
     * @param con       db connection object
     * @param profileId profile id
     * @return profile
     */
    public static Profile getProfile(Connection con, Long profileId) throws SQLException {

        Profile profile = null;
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

        return profile;
    }

    /**
     * inserts new profile
     *
     * @param profile profile object
     */
    public static void insertProfile(Profile profile) throws SQLException, GeneralSecurityException {


        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("insert into profiles (nm, desc) values (?,?)");
        stmt.setString(1, profile.getNm());
        stmt.setString(2, profile.getDesc());
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * updates profile
     *
     * @param profile profile object
     */
    public static void updateProfile(Profile profile) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("update profiles set nm=?, desc=? where id=?");
        stmt.setString(1, profile.getNm());
        stmt.setString(2, profile.getDesc());
        stmt.setLong(3, profile.getId());
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }

    /**
     * deletes profile
     *
     * @param profileId profile id
     */
    public static void deleteProfile(Long profileId) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("delete from profiles where id=?");
        stmt.setLong(1, profileId);
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }


}
