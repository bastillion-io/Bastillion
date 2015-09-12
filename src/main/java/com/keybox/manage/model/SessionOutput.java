/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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


/**
 * Output from ssh session
 */
public class SessionOutput extends HostSystem {
    Long sessionId;
    StringBuilder output = new StringBuilder();

    public SessionOutput() {


    }
    public SessionOutput(Long sessionId, HostSystem hostSystem) {
        this.sessionId=sessionId;
        this.setId(hostSystem.getId());
        this.setInstanceId(hostSystem.getInstanceId());
        this.setUser(hostSystem.getUser());
        this.setHost(hostSystem.getHost());
        this.setPort(hostSystem.getPort());
        this.setDisplayNm(hostSystem.getDisplayNm());
        this.setAuthorizedKeys(hostSystem.getAuthorizedKeys());

    }
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public StringBuilder getOutput() {
        return output;
    }

    public void setOutput(StringBuilder output) {
        this.output = output;
    }

}