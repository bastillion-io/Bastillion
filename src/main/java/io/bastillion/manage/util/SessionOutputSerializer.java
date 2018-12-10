/**
 *    Copyright (C) 2015 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
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
