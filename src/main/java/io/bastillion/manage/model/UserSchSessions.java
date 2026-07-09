/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSchSessions {

    Map<Integer, SchSession> schSessionMap = new ConcurrentHashMap<>();

    /**
     * Intentionally returns the live map, not a defensive copy - SSHUtil, SecureShellKtrl,
     * SecureShellWS, and UploadAndPushKtrl all read and mutate (put/remove/clear) active
     * terminal sessions through this reference (see the "exposing internal representation"
     * autofix reverted here - it silently broke opening/closing every terminal session with
     * an UnsupportedOperationException).
     */
    public Map<Integer, SchSession> getSchSessionMap() {
        return schSessionMap;
    }

    public void setSchSessionMap(Map<Integer, SchSession> schSessionMap) {
        this.schSessionMap = (schSessionMap == null) ? new ConcurrentHashMap<>() : schSessionMap;
    }

}
