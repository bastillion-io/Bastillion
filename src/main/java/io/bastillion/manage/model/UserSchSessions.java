/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSchSessions {

    Map<Integer, SchSession> schSessionMap = new ConcurrentHashMap<>();


    public Map<Integer, SchSession> getSchSessionMap() {
        return Collections.unmodifiableMap(new ConcurrentHashMap<>(schSessionMap));
    }

    public void setSchSessionMap(Map<Integer, SchSession> schSessionMap) {
        this.schSessionMap = (schSessionMap == null) ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(schSessionMap);
    }

}
