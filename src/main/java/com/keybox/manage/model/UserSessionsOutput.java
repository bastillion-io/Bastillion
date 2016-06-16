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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionsOutput {

    //instance id, host output
    Map<Integer, SessionOutput> sessionOutputMap = new ConcurrentHashMap<>();


    public Map<Integer, SessionOutput> getSessionOutputMap() {
        return sessionOutputMap;
    }

    public void setSessionOutputMap(Map<Integer, SessionOutput> sessionOutputMap) {
        this.sessionOutputMap = sessionOutputMap;
    }
}



