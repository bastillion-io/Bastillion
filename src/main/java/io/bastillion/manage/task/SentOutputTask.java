/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.task;

import com.google.gson.Gson;
import io.bastillion.manage.model.SessionOutput;
import io.bastillion.manage.model.User;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.SessionOutputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * class to send output to web socket client
 */
public class SentOutputTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SentOutputTask.class);

    Session session;
    Long sessionId;
    User user;

    public SentOutputTask(Long sessionId, Session session, User user) {
        this.sessionId = sessionId;
        this.session = session;
        this.user = user;
    }

    public void run() {
        Gson gson = new Gson();
        while (session.isOpen()) {
            try {
                Connection con = DBUtils.getConn();
                List<SessionOutput> outputList = SessionOutputUtil.getOutput(con, sessionId, user);
                if (!outputList.isEmpty()) {
                    String json = gson.toJson(outputList);
                    //send json to session
                    this.session.getBasicRemote().sendText(json);
                }
                Thread.sleep(25);
                DBUtils.closeConn(con);
            } catch (SQLException | GeneralSecurityException | IOException | InterruptedException ex) {
                log.error(ex.toString(), ex);
            }
        }
    }
}
