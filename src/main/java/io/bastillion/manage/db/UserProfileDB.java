/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.Profile;
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
 * DAO to manage user profile information
 */
public class UserProfileDB {

    private UserProfileDB() {
    }

    /**
     * sets users for profile
     *
     * @param profileId  profile id
     * @param userIdList list of user ids
     */
    public static void setUsersForProfile(Long profileId, List<Long> userIdList) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("delete from user_map where profile_id=?");
        stmt.setLong(1, profileId);
        stmt.execute();
        DBUtils.closeStmt(stmt);

        for (Long userId : userIdList) {
            stmt = con.prepareStatement("insert into user_map (profile_id, user_id) values (?,?)");
            stmt.setLong(1, profileId);
            stmt.setLong(2, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);
        }
        //delete all unassigned keys by profile
        PublicKeyDB.deleteUnassignedKeysByProfile(con, profileId);

        DBUtils.closeConn(con);
    }

    /**
     * return a list of profiles for user
     *
     * @param userId user id
     * @return profile list
     */
    public static List<Profile> getProfilesByUser(Long userId) throws SQLException, GeneralSecurityException {


        Connection con = DBUtils.getConn();
        List<Profile> profileList = getProfilesByUser(con, userId);
        DBUtils.closeConn(con);

        return profileList;
    }

    /**
     * return a list of profiles for user
     *
     * @param userId user id
     * @return profile list
     */
    public static List<Profile> getProfilesByUser(Connection con, Long userId) throws SQLException {

        ArrayList<Profile> profileList = new ArrayList<>();


        PreparedStatement stmt = con.prepareStatement("select * from  profiles g, user_map m where g.id=m.profile_id and m.user_id=? order by nm asc");
        stmt.setLong(1, userId);
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


        return profileList;
    }

    /**
     * checks to determine if user belongs to profile
     *
     * @param userId    user id
     * @param profileId profile id
     * @return true if user belongs to profile
     */
    public static boolean checkIsUsersProfile(Long userId, Long profileId) throws SQLException, GeneralSecurityException {
        boolean isUsersProfile = false;

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("select * from user_map where profile_id=? and user_id=?");
        stmt.setLong(1, profileId);
        stmt.setLong(2, userId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            isUsersProfile = true;
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return isUsersProfile;
    }

    /**
     * assigns profiles to given user
     *
     * @param userId                 user id
     * @param allProfilesNmList      list of all profiles
     * @param assignedProfilesNmList list of assigned profiles
     */
    public static void assignProfilesToUser(Connection con, Long userId, List<String> allProfilesNmList, List<String> assignedProfilesNmList) throws SQLException {

        for (String profileNm : allProfilesNmList) {
            if (StringUtils.isNotEmpty(profileNm)) {

                Long profileId = null;
                PreparedStatement stmt = con.prepareStatement("select id from  profiles p where lower(p.nm) like ?");
                stmt.setString(1, profileNm.toLowerCase());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    profileId = rs.getLong("id");
                }
                DBUtils.closeRs(rs);
                DBUtils.closeStmt(stmt);

                if (profileId != null) {
                    stmt = con.prepareStatement("delete from user_map where profile_id=?");
                    stmt.setLong(1, profileId);
                    stmt.execute();
                    DBUtils.closeStmt(stmt);

                    if (assignedProfilesNmList.contains(profileNm)) {
                        stmt = con.prepareStatement("insert into user_map (profile_id, user_id) values (?,?)");
                        stmt.setLong(1, profileId);
                        stmt.setLong(2, userId);
                        stmt.execute();
                        DBUtils.closeStmt(stmt);
                    }

                    //delete all unassigned keys by profile
                    PublicKeyDB.deleteUnassignedKeysByProfile(con, profileId);
                }

            }
        }

    }

    /**
     * assigns profiles to given user
     *
     * @param userId    user id
     * @param profileNm profile name
     */
    public static void assignProfileToUser(Connection con, Long userId, String profileNm) throws SQLException {

        if (StringUtils.isNotEmpty(profileNm)) {

            Long profileId = null;
            PreparedStatement stmt = con.prepareStatement("select id from  profiles p where lower(p.nm) like ?");
            stmt.setString(1, profileNm.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                profileId = rs.getLong("id");
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

            if (profileId != null) {
                stmt = con.prepareStatement("delete from user_map where profile_id=?");
                stmt.setLong(1, profileId);
                stmt.execute();
                DBUtils.closeStmt(stmt);

                stmt = con.prepareStatement("insert into user_map (profile_id, user_id) values (?,?)");
                stmt.setLong(1, profileId);
                stmt.setLong(2, userId);
                stmt.execute();
                DBUtils.closeStmt(stmt);

                //delete all unassigned keys by profile
                PublicKeyDB.deleteUnassignedKeysByProfile(con, profileId);
            }
        }
    }
}
