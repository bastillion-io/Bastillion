/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.manage.db;

import com.keybox.manage.model.Profile;
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
 * DAO to manage user profile information
 */
public class UserProfileDB {

    private static Logger log = LoggerFactory.getLogger(UserProfileDB.class);

    private UserProfileDB() {
    }

    /**
     * sets users for profile
     *
     * @param profileId  profile id
     * @param userIdList list of user ids
     */
    public static void setUsersForProfile(Long profileId, List<Long> userIdList) {

        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBUtils.getConn();
            stmt = con.prepareStatement("delete from user_map where profile_id=?");
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

        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * return a list of profiles for user
     *
     * @param userId user id
     * @return profile list
     */
    public static List<Profile> getProfilesByUser(Long userId) {


        Connection con = null;
        List<Profile> profileList = new ArrayList<>();
        try {
            con = DBUtils.getConn();
            profileList = getProfilesByUser(con, userId);

        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBUtils.closeConn(con);
        }
        return profileList;


    }

    /**
     * return a list of profiles for user
     *
     * @param userId user id
     * @return profile list
     */
    public static List<Profile> getProfilesByUser(Connection con, Long userId) {

        ArrayList<Profile> profileList = new ArrayList<>();


        try {
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

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return profileList;
    }

    /**
     * checks to determine if user belongs to profile
     *
     * @param userId    user id
     * @param profileId profile id
     * @return true if user belongs to profile
     */
    public static boolean checkIsUsersProfile(Long userId, Long profileId) {
        boolean isUsersProfile = false;

        Connection con = null;

        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from user_map where profile_id=? and user_id=?");
            stmt.setLong(1, profileId);
            stmt.setLong(2, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                isUsersProfile = true;
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBUtils.closeConn(con);
        }

        return isUsersProfile;

    }

    /**
     * assigns profiles to given user
     *
     * @param userId                 user id
     * @param allProfilesNmList      list of all profiles
     * @param assignedProfilesNmList list of assigned profiles
     */
    public static void assignProfilesToUser(Connection con, Long userId, List<String> allProfilesNmList, List<String> assignedProfilesNmList) {

        PreparedStatement stmt = null;

        try {

            for (String profileNm : allProfilesNmList) {
                if (StringUtils.isNotEmpty(profileNm)) {

                    Long profileId = null;
                    stmt = con.prepareStatement("select id from  profiles p where lower(p.nm) like ?");
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

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

}
