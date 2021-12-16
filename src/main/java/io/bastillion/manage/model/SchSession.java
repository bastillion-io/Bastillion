/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

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
