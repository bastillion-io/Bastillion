/**
 * Copyright (C) 2015 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.bastillion.manage.model.AuditWrapper;

import java.lang.reflect.Type;
import java.util.Date;

public class SessionOutputSerializer implements JsonSerializer<Object> {
    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        if (typeOfSrc.equals(AuditWrapper.class)) {
            AuditWrapper auditWrapper = (AuditWrapper) src;
            object.addProperty("user_id", auditWrapper.getUser().getId());
            object.addProperty("username", auditWrapper.getUser().getUsername());
            object.addProperty("user_type", auditWrapper.getUser().getUserType());
            object.addProperty("first_nm", auditWrapper.getUser().getFirstNm());
            object.addProperty("last_nm", auditWrapper.getUser().getLastNm());
            object.addProperty("email", auditWrapper.getUser().getEmail());
            object.addProperty("session_id", auditWrapper.getSessionOutput().getSessionId());
            object.addProperty("instance_id", auditWrapper.getSessionOutput().getInstanceId());
            object.addProperty("host_id", auditWrapper.getSessionOutput().getId());
            object.addProperty("host", auditWrapper.getSessionOutput().getDisplayLabel());
            object.addProperty("output", auditWrapper.getSessionOutput().getOutput().toString());
            object.addProperty("timestamp", new Date().getTime());
        }
        return object;
    }
}
