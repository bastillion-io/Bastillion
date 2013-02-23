/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.model;

public class SystemStatus {


    public static final String INITIAL_STATUS="I";
    public static final String AUTH_FAIL_STATUS="A";
    public static final String IN_PROGRESS_STATUS="P";
    public static final String GENERIC_FAIL_STATUS="F";
    public static final String SUCCESS_STATUS="S";


    Long id;
    String authKeyVal;
    String statusCd=INITIAL_STATUS;
    String errorMsg;
    String output;
    HostSystem hostSystem;



    public String getAuthKeyVal() {
        return authKeyVal;
    }

    public void setAuthKeyVal(String authKeyVal) {
        this.authKeyVal = authKeyVal;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HostSystem getHostSystem() {
        return hostSystem;
    }

    public void setHostSystem(HostSystem hostSystem) {
        this.hostSystem = hostSystem;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
