/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSchSessions {

    Map<Integer, SchSession> schSessionMap = new ConcurrentHashMap<>();


    public Map<Integer, SchSession> getSchSessionMap() {
        return schSessionMap;
    }

    public void setSchSessionMap(Map<Integer, SchSession> schSessionMap) {
        this.schSessionMap = schSessionMap;
    }

}
