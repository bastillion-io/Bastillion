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
package io.bastillion.manage.model;

import java.util.List;

/**
 * Value object that contains host system information
 */
public class HostSystem {
    Long id;
    String displayNm;
    String user = "root";
    String host;
    Integer port = 22;
    String displayLabel;
    String authorizedKeys="~/.ssh/authorized_keys";
    Boolean checked=false;
    String statusCd=INITIAL_STATUS;
    String errorMsg;
    List<String> publicKeyList;
    Integer instanceId;

    public static final String INITIAL_STATUS="INITIAL";
    public static final String AUTH_FAIL_STATUS="AUTHFAIL";
    public static final String PUBLIC_KEY_FAIL_STATUS="KEYAUTHFAIL";
    public static final String GENERIC_FAIL_STATUS="GENERICFAIL";
    public static final String SUCCESS_STATUS="SUCCESS";
    public static final String HOST_FAIL_STATUS="HOSTFAIL";


    public Long getId() {
        return id;

    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayNm() {
        return displayNm;
    }

    public void setDisplayNm(String displayNm) {
        this.displayNm = displayNm;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host){
        this.host = host;
    }

    public String getDisplayLabel() {
        return getDisplayNm() +" - ( " +getUser() +"@"+getHost()+":"+getPort()+" )";
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getAuthorizedKeys() {
        return authorizedKeys;
    }

    public void setAuthorizedKeys(String authorizedKeys) {
        this.authorizedKeys = authorizedKeys;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<String> getPublicKeyList() {
        return publicKeyList;
    }

    public void setPublicKeyList(List<String> publicKeyList) {
        this.publicKeyList = publicKeyList;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }
}
