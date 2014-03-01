package com.keybox.manage.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionsOutput {

    Map<Long, StringBuilder> sessionOutputMap = new ConcurrentHashMap<Long,StringBuilder>();


    public Map<Long, StringBuilder> getSessionOutputMap() {
        return sessionOutputMap;
    }

    public void setSessionOutputMap(Map<Long, StringBuilder> sessionOutputMap) {
        this.sessionOutputMap = sessionOutputMap;
    }
}
