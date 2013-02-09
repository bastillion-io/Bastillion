/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
package com.keybox.manage.model;

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
    boolean checked=false;
    List<String> publicKeyList;

    public Long getId() {
        return id;

    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayNm() {
        return displayNm;
    }

    public List<String> getPublicKeyList() {
        return publicKeyList;
    }

    public void setPublicKeyList(List<String> publicKeyList) {
        this.publicKeyList = publicKeyList;
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

    public void setHost(String host) {
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

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
