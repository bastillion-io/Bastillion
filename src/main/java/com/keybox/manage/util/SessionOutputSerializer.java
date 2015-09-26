/**
 * Copyright 2015 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.manage.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.keybox.manage.model.AuditWrapper;
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
