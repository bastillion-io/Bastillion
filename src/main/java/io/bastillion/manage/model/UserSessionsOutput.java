/**
 * Copyright (C) 2015 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionsOutput {

    //instance id, host output
    Map<Integer, SessionOutput> sessionOutputMap = new ConcurrentHashMap<>();

    /**
     * Intentionally returns the live map, not a defensive copy - SessionOutputUtil
     * (addOutput/addToOutput/removeOutput/getOutput) reads and mutates session output
     * through this reference from multiple threads (the websocket thread and the SSH
     * output-reader thread). Wrapping this in an unmodifiable/copied view breaks that
     * (see the "exposing internal representation" autofix reverted here - it silently
     * broke every terminal session with an UnsupportedOperationException on write).
     */
    public Map<Integer, SessionOutput> getSessionOutputMap() {
        return sessionOutputMap;
    }

    public void setSessionOutputMap(Map<Integer, SessionOutput> sessionOutputMap) {
        this.sessionOutputMap = sessionOutputMap;
    }
}



