package com.keybox.manage.db;

import com.keybox.manage.model.Profile;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * DAO to manage profile
 */
public class ProfileDB {

    public static final String SORT_BY_PROFILE_NM="nm";

    /**
     * method to do order by based on the sorted set object for profiles
     * @return list of profiles
     */
    public static SortedSet getProfileSet(SortedSet sortedSet) {

        ArrayList<Profile> profileList = new ArrayList<Profile>();

        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from  profiles " + orderBy;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
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
        DBUtils.closeConn(con);

        sortedSet.setItemList(profileList);
        return sortedSet;
    }


    /**
     * returns all profile information
     *
     * @return list of profiles
     */
    public static List<Profile> getAllProfiles() {

        ArrayList<Profile> profileList = new ArrayList<Profile>();
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
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

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
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

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
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

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
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

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
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


}
