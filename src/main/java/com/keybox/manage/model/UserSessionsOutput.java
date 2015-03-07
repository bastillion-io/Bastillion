package com.keybox.manage.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionsOutput {

    Map<Integer, StringBuilder> sessionOutputMap = new ConcurrentHashMap<Integer,StringBuilder>();


    public Map<Integer, StringBuilder> getSessionOutputMap() {
        return sessionOutputMap;
    }

    public void setSessionOutputMap(Map<Integer, StringBuilder> sessionOutputMap) {
        this.sessionOutputMap = sessionOutputMap;
    }
}
