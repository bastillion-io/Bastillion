package com.keybox.manage.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionsOutput {

    Map<Long, SessionOutput> sessionOutputMap = new ConcurrentHashMap<Long,SessionOutput>();


    public Map<Long, SessionOutput> getSessionOutputMap() {
        return sessionOutputMap;
    }

    public void setSessionOutputMap(Map<Long, SessionOutput> sessionOutputMap) {
        this.sessionOutputMap = sessionOutputMap;
    }
}
