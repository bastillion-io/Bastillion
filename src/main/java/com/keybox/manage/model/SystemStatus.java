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
package com.keybox.manage.model;

/**
 * System status value object
 */
public class SystemStatus {


    public static final String INITIAL_STATUS="I";
    public static final String AUTH_FAIL_STATUS="A";
    public static final String IN_PROGRESS_STATUS="P";
    public static final String GENERIC_FAIL_STATUS="F";
    public static final String TIMEOUT_STATUS="T";
    public static final String SUCCESS_STATUS="S";


    Long id;
    String authKeyVal;
    String statusCd=INITIAL_STATUS;
    String errorMsg;
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

}
