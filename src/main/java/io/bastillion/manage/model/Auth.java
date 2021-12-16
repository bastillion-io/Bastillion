/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import java.util.Date;

/**
 * Value object that contains login information
 */
public class Auth {


    public static final String ADMINISTRATOR="A";
    public static final String MANAGER="M";
    
    public static final String AUTH_BASIC="BASIC";
    public static final String AUTH_EXTERNAL="EXTERNAL";

    Long id;
    String username;
    String password;
    String passwordConfirm;
    String prevPassword;
    String authToken;
    String otpSecret;
    Long otpToken;
    String salt;
    String userType=ADMINISTRATOR;
    String authType=AUTH_BASIC;
    Date lastLoginTm;
    Date expirationTm;
    boolean expired=false;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOtpSecret() {
        return otpSecret;
    }

    public void setOtpSecret(String otpSecret) {
        this.otpSecret = otpSecret;
    }

    public Long getOtpToken() {
        return otpToken;
    }

    public void setOtpToken(Long otpToken) {
        this.otpToken = otpToken;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getPrevPassword() {
        return prevPassword;
    }

    public void setPrevPassword(String prevPassword) {
        this.prevPassword = prevPassword;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public Date getLastLoginTm() {
        return lastLoginTm;
    }

    public void setLastLoginTm(Date lastLoginTm) {
        this.lastLoginTm = lastLoginTm;
    }

    public Date getExpirationTm() {
        return expirationTm;
    }

    public void setExpirationTm(Date expirationTm) {
        this.expirationTm = expirationTm;
    }

    public boolean isExpired() {

        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }
}
