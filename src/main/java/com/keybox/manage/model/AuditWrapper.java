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
package com.keybox.manage.model;

public class AuditWrapper {

    User user;
    SessionOutput sessionOutput;

    public AuditWrapper(User user, SessionOutput sessionOutput) {
        this.user=user;
        this.sessionOutput=sessionOutput;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SessionOutput getSessionOutput() {
        return sessionOutput;
    }

    public void setSessionOutput(SessionOutput sessionOutput) {
        this.sessionOutput = sessionOutput;
    }
}
