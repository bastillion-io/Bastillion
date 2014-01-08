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
package com.keybox.manage.task;


import com.google.gson.Gson;
import com.keybox.manage.model.SessionOutput;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.SessionOutputUtil;

import java.sql.Connection;
import java.util.List;
import javax.websocket.Session;

/**
 * prints output to socket connection
 */
public class SendWSOutputTask implements Runnable {


    Long sessionId;
    Session session;


    public SendWSOutputTask(Long sessionId, Session session) {
        this.sessionId = sessionId;
        this.session = session;
    }

    public void run() {

        Connection con = DBUtils.getConn();
        //this checks to see if session is valid
        while (session.isOpen()) {
            try {
                List<SessionOutput> outputList = SessionOutputUtil.getOutput(con, sessionId);
                if (outputList != null && !outputList.isEmpty()) {
                    String json = new Gson().toJson(outputList);
                    //send json to session
                    session.getBasicRemote().sendText(json);
                    Thread.sleep(200);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


        DBUtils.closeConn(con);


    }


}
