/**
 * Copyright (C) 2015 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

public class AuditWrapper {

    User user;
    SessionOutput sessionOutput;

    public AuditWrapper(User user, SessionOutput sessionOutput) {
        this.user = user;
        this.sessionOutput = sessionOutput;
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
