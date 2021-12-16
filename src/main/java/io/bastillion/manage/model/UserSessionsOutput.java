/**
 *    Copyright (C) 2015 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionsOutput {

    //instance id, host output
    Map<Integer, SessionOutput> sessionOutputMap = new ConcurrentHashMap<>();


    public Map<Integer, SessionOutput> getSessionOutputMap() {
        return sessionOutputMap;
    }

    public void setSessionOutputMap(Map<Integer, SessionOutput> sessionOutputMap) {
        this.sessionOutputMap = sessionOutputMap;
    }
}



