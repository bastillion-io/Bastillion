package com.keybox.manage.action;


import com.keybox.manage.db.UserDB;
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


    @Action(value = "/manage/viewUsers",
            results = {
                    @Result(name = "success", location = "/manage/view_users.jsp")
            }
    )
    public String viewSystems() {

        sortedSet = UserDB.getUserSet(sortedSet);
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
            addFieldError("user.lastNm", "Last Name is required");
        }

        if (user == null
                || user.getFirstNm() == null
                || user.getFirstNm().trim().equals("")) {
            addFieldError("user.firstNm", "First Name is required");
        }
        if (user == null
                || user.getPublicKey() == null
                || user.getPublicKey().trim().equals("")) {
            addFieldError("user.publicKey", "Public Key is required");

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
}
