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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;


/**
 * contains information for an ssh session
 */
public class SchSession {


    Long userId;
    Session session;
    Channel channel;
    PrintStream commander;
    InputStream outFromChannel;
    OutputStream inputToChannel;
    HostSystem hostSystem;


    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public PrintStream getCommander() {
        return commander;
    }

    public void setCommander(PrintStream commander) {
        this.commander = commander;
    }

    public InputStream getOutFromChannel() {
        return outFromChannel;
    }

    public void setOutFromChannel(InputStream outFromChannel) {
        this.outFromChannel = outFromChannel;
    }

    public OutputStream getInputToChannel() {
        return inputToChannel;
    }

    public void setInputToChannel(OutputStream inputToChannel) {
        this.inputToChannel = inputToChannel;
    }

    public HostSystem getHostSystem() {
        return hostSystem;
    }

    public void setHostSystem(HostSystem hostSystem) {
        this.hostSystem = hostSystem;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
