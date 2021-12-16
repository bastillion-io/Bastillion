/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import java.util.List;

/**
 * Value object that contains profile information
 */
public class Profile {
    Long id;
    String nm;
    String desc;
    List<HostSystem> hostSystemList;

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

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
