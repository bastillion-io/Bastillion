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
package com.keybox.manage.action;


import com.keybox.manage.db.ScriptDB;
import com.keybox.manage.db.UserDB;
import com.keybox.manage.model.Script;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.model.User;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;

import java.util.List;

/**
 * Action to manage users
 */
public class UsersAction extends ActionSupport {

    SortedSet sortedSet=new SortedSet();
    User user = new User();
    Script script=null;


    @Action(value = "/manage/viewUsers",
            results = {
                    @Result(name = "success", location = "/manage/view_users.jsp")
            }
    )
    public String viewUsers() {

        sortedSet = UserDB.getUserSet(sortedSet);
        if(script!=null && script.getId()!=null){
            script=ScriptDB.getScript(script.getId());
        }
        return SUCCESS;
    }

    @Action(value = "/manage/saveUser",
            results = {
                    @Result(name = "input", location = "/manage/view_users.jsp"),
                    @Result(name = "success", location = "/manage/viewUsers.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type="redirect")
            }
    )
    public String saveUser() {

        if (user.getId() != null) {
            UserDB.updateUser(user);
        } else {
            UserDB.insertUser(user);
        }
        return SUCCESS;
    }

    @Action(value = "/manage/deleteUser",
            results = {
                    @Result(name = "success", location = "/manage/viewUsers.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type="redirect")
            }
    )
    public String deleteUser() {

        if (user.getId() != null) {
            UserDB.deleteUser(user.getId());
        }
        return SUCCESS;
    }

    /**
     * Validates all fields for adding a user
     */
    public void validateSaveUser() {
        if (user == null
                || user.getLastNm() == null
                || user.getLastNm().trim().equals("")) {
            addFieldError("user.lastNm", "Required");
        }

        if (user == null
                || user.getFirstNm() == null
                || user.getFirstNm().trim().equals("")) {
            addFieldError("user.firstNm", "Required");
        }
        if (user == null
                || user.getPublicKey() == null
                || user.getPublicKey().trim().equals("")) {
            addFieldError("user.publicKey", "Required");

        }


        if (!this.getFieldErrors().isEmpty()) {
            sortedSet = UserDB.getUserSet(sortedSet);
        }

    }


    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
