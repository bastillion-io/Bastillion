/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;
import io.bastillion.manage.util.ExternalAuthUtil;
import org.apache.commons.lang3.StringUtils;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * DAO to login administrative users
 */
public class AuthDB {

    public static final int EXPIRATION_DAYS = StringUtils.isNumeric(AppConfig.getProperty("accountExpirationDays")) ? Integer.parseInt(AppConfig.getProperty("accountExpirationDays")) : -1;

    private AuthDB() {
    }

    /**
     * auth user and return auth token if valid auth
     *
     * @param auth username and password object
     * @return auth token if success
     */
    public static String login(Auth auth) throws SQLException, GeneralSecurityException {
        //check ldap first
        String authToken = ExternalAuthUtil.login(auth);

        if (StringUtils.isEmpty(authToken)) {

            Connection con = DBUtils.getConn();

            //get salt for user
            String salt = getSaltByUsername(con, auth.getUsername());
            //login
            PreparedStatement stmt = con.prepareStatement("select * from users where username=? and password=?");
            stmt.setString(1, auth.getUsername());
            stmt.setString(2, EncryptionUtil.hash(auth.getPassword() + salt));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                auth.setId(rs.getLong("id"));
                authToken = UUID.randomUUID().toString();
                auth.setAuthToken(authToken);
                auth.setAuthType(Auth.AUTH_BASIC);
                updateLogin(con, auth);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
            DBUtils.closeConn(con);
        }

        return authToken;
    }


    /**
     * checks to see if user is an admin based on auth token
     *
     * @param userId    user id
     * @param authToken auth token string
     * @return user type if authorized, null if not authorized
     */
    public static String isAuthorized(Long userId, String authToken) throws SQLException, GeneralSecurityException {

        String authorized = null;

        if (authToken != null && !authToken.trim().equals("")) {

            Connection con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from users where id=? and auth_token=?");
            stmt.setLong(1, userId);
            stmt.setString(2, authToken);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                authorized = rs.getString("user_type");

            }
            DBUtils.closeRs(rs);

            DBUtils.closeStmt(stmt);
            DBUtils.closeConn(con);
        }
        return authorized;
    }

    /**
     * updates the admin table based on auth id
     *
     * @param con  DB connection
     * @param auth username and password object
     */
    public static void updateLogin(Connection con, Auth auth) throws SQLException, NoSuchAlgorithmException {

        PreparedStatement stmt = con.prepareStatement("update users set username=?, auth_type=?, auth_token=?, password=?, salt=? where id=?");
        stmt.setString(1, auth.getUsername());
        stmt.setString(2, auth.getAuthType());
        stmt.setString(3, auth.getAuthToken());
        if (StringUtils.isNotEmpty(auth.getPassword())) {
            String salt = EncryptionUtil.generateSalt();
            stmt.setString(4, EncryptionUtil.hash(auth.getPassword() + salt));
            stmt.setString(5, salt);
        } else {
            stmt.setString(4, null);
            stmt.setString(5, null);
        }
        stmt.setLong(6, auth.getId());
        stmt.execute();

        DBUtils.closeStmt(stmt);
    }

    /**
     * updates the last login and expiration time
     *
     * @param con  DB connection
     * @param auth username and password object
     */
    public static void updateLastLogin(Connection con, Auth auth) throws SQLException {

        PreparedStatement stmt = con.prepareStatement("update users set last_login_tm=?, expiration_tm=? where id=?");

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        stmt.setTimestamp(1, new Timestamp(c.getTime().getTime()));
        if (Auth.MANAGER.equals(auth.getUserType()) || EXPIRATION_DAYS <= 0) {
            stmt.setTimestamp(2, null);
        } else {
            c.add(Calendar.DATE, EXPIRATION_DAYS);
            stmt.setTimestamp(2, new Timestamp(c.getTime().getTime()));
        }
        stmt.setLong(3, auth.getId());
        stmt.execute();

        DBUtils.closeStmt(stmt);
    }

    /**
     * updates the last login and expiration time
     *
     * @param auth username and password object
     */
    public static void updateLastLogin(Auth auth) throws SQLException, GeneralSecurityException {
        Connection con = DBUtils.getConn();
        updateLastLogin(con, auth);
        DBUtils.closeConn(con);
    }

    /**
     * updates password for admin using auth token
     */
    public static boolean updatePassword(Auth auth) throws SQLException, GeneralSecurityException {
        boolean success = false;

        Connection con = DBUtils.getConn();

        String prevSalt = getSaltByAuthToken(con, auth.getAuthToken());
        PreparedStatement stmt = con.prepareStatement("select * from users where auth_token like ? and password like ?");
        stmt.setString(1, auth.getAuthToken());
        stmt.setString(2, EncryptionUtil.hash(auth.getPrevPassword() + prevSalt));
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {

            String salt = EncryptionUtil.generateSalt();
            PreparedStatement updateStmt = con.prepareStatement("update users set password=?, salt=? where auth_token like ?");
            updateStmt.setString(1, EncryptionUtil.hash(auth.getPassword() + salt));
            updateStmt.setString(2, salt);
            updateStmt.setString(3, auth.getAuthToken());
            updateStmt.execute();
            DBUtils.closeStmt(updateStmt);
            success = true;
        }

        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return success;
    }

    /**
     * returns user id based on auth token
     *
     * @param authToken auth token
     * @param con       DB connection
     * @return user
     */
    public static User getUserByAuthToken(Connection con, String authToken) throws SQLException {


        User user = null;
        PreparedStatement stmt = con.prepareStatement("select * from users where auth_token like ?");
        stmt.setString(1, authToken);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Long userId = rs.getLong("id");
            user = UserDB.getUser(con, userId);
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);

        return user;
    }

    /**
     * returns user based on auth token
     *
     * @param authToken auth token
     * @return user
     */
    public static User getUserByAuthToken(String authToken) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        User user = getUserByAuthToken(con, authToken);
        DBUtils.closeConn(con);

        return user;
    }

    /**
     * returns the shared secret based on user id
     *
     * @param userId user id
     * @return auth object
     */
    public static String getSharedSecret(Long userId) throws SQLException, GeneralSecurityException {

        String sharedSecret = null;
        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("select * from users where id like ?");
        stmt.setLong(1, userId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            sharedSecret = EncryptionUtil.decrypt(rs.getString("otp_secret"));
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);

        return sharedSecret;
    }

    /**
     * updates shared secret based on auth token
     *
     * @param secret    OTP shared secret
     * @param authToken auth token
     */
    public static void updateSharedSecret(String secret, String authToken) throws SQLException, GeneralSecurityException {

        Connection con = DBUtils.getConn();
        PreparedStatement stmt = con.prepareStatement("update users set otp_secret=? where auth_token=?");
        stmt.setString(1, EncryptionUtil.encrypt(secret));
        stmt.setString(2, authToken);
        stmt.execute();
        DBUtils.closeStmt(stmt);
        DBUtils.closeConn(con);
    }


    /**
     * get salt by user name
     *
     * @param con      DB connection
     * @param username username
     * @return salt
     */
    private static String getSaltByUsername(Connection con, String username) throws SQLException {

        String salt = "";
        PreparedStatement stmt = con.prepareStatement("select salt from users where username=?");
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next() && rs.getString("salt") != null) {
            salt = rs.getString("salt");
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);

        return salt;
    }


    /**
     * get salt by authentication token
     *
     * @param con       DB connection
     * @param authToken auth token
     * @return salt
     */
    private static String getSaltByAuthToken(Connection con, String authToken) throws SQLException {

        String salt = "";
        PreparedStatement stmt = con.prepareStatement("select salt from users where auth_token=?");
        stmt.setString(1, authToken);
        ResultSet rs = stmt.executeQuery();
        if (rs.next() && rs.getString("salt") != null) {
            salt = rs.getString("salt");
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);

        return salt;
    }


    /**
     * returns user base on username
     *
     * @param con DB connection
     * @param uid username id
     * @return user object
     */
    public static User getUserByUID(Connection con, String uid) throws SQLException {

        User user = null;
        PreparedStatement stmt = con.prepareStatement("select * from  users where lower(username) like lower(?)");
        stmt.setString(1, uid);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            user = new User();
            user.setId(rs.getLong("id"));
            user.setFirstNm(rs.getString("first_nm"));
            user.setLastNm(rs.getString("last_nm"));
            user.setEmail(rs.getString("email"));
            user.setUsername(rs.getString("username"));
            user.setUserType(rs.getString("user_type"));
            user.setLastLoginTm(rs.getTimestamp("last_login_tm"));
            user.setExpirationTm(rs.getTimestamp("expiration_tm"));
            user.setExpired(EXPIRATION_DAYS > 0 && user.getExpirationTm() != null && user.getExpirationTm().before(new Date()));
            user.setProfileList(UserProfileDB.getProfilesByUser(con, user.getId()));
        }
        DBUtils.closeRs(rs);
        DBUtils.closeStmt(stmt);

        return user;
    }
}
