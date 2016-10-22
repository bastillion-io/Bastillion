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

import com.keybox.manage.model.Profile;
import com.keybox.manage.model.User;
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
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
     * @param profileId profile id
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

            for(Long userId : userIdList) {
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
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * return a list of profiles for user
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
        }
        finally {
            DBUtils.closeConn(con);
        }
        return profileList;


    }

    /**
     * return a list of profiles for user
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
     * get users associated with profile
     * @param profileId profile id
     * @return user list
     */
    public static List<User> getUsersByProfile(Long profileId) {


        Connection con = null;
        List<User> userList = new ArrayList<>();
        try {
            con = DBUtils.getConn();
            userList = getUsersByProfile(con, profileId);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
        return userList;


    }

    /**
     * get users associated with profile
     * @param con DB connection
     * @param profileId profile id
     * @return user list
     */
    public static List<User> getUsersByProfile(Connection con, Long profileId) {

        ArrayList<User> userList = new ArrayList<>();


        try {
            PreparedStatement stmt = con.prepareStatement("select * from  users u, user_map m where u.id=m.user_id and m.profile_id=? order by last_nm asc");
            stmt.setLong(1, profileId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setFirstNm(rs.getString("first_nm"));
                user.setLastNm(rs.getString("last_nm"));
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                userList.add(user);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return userList;
    }


    /**
     * checks to determine if user belongs to profile
     *
     * @param userId user id
     * @param profileId profile id
     * @return true if user belongs to profile
     */
    public static boolean checkIsUsersProfile(Long userId, Long profileId){
        boolean isUsersProfile=false;

        Connection con = null;

        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from user_map where profile_id=? and user_id=?");
            stmt.setLong(1, profileId);
            stmt.setLong(2, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
               isUsersProfile=true;
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return isUsersProfile;

    }

}
