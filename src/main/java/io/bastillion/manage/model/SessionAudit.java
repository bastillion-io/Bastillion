/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;


import java.util.Date;
import java.util.List;

/**
 * value object for terminal logs and history
 */
public class SessionAudit extends User {
    Long id;
    List<HostSystem> hostSystemList;
    Date sessionTm;

    public Date getSessionTm() {
        return sessionTm;
    }

    public void setSessionTm(Date sessionTm) {
        this.sessionTm = sessionTm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<HostSystem> getHostSystemList() {
        return hostSystemList;
    }

    public void setHostSystemList(List<HostSystem> hostSystemList) {
        this.hostSystemList = hostSystemList;
    }
}
