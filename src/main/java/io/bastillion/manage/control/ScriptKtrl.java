/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.control;

import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.db.ScriptDB;
import io.bastillion.manage.model.Script;
import io.bastillion.manage.model.SortedSet;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Action to manage scripts
 */
public class ScriptKtrl extends BaseKontroller {

    @Model(name = "sortedSet")
    SortedSet sortedSet = new SortedSet();
    @Model(name = "script")
    Script script = new Script();

    public ScriptKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/admin/viewScripts", method = MethodType.GET)
    public String viewScripts() {
        Long userId = AuthUtil.getUserId(getRequest().getSession());
        sortedSet = ScriptDB.getScriptSet(sortedSet, userId);

        return "/admin/view_scripts.html";
    }


    @Kontrol(path = "/admin/saveScript", method = MethodType.POST)
    public String saveScript() {
        Long userId = AuthUtil.getUserId(getRequest().getSession());
        if (script.getId() != null) {
            ScriptDB.updateScript(script, userId);
        } else {
            ScriptDB.insertScript(script, userId);
        }
        return "redirect:/admin/viewScripts.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }

    @Kontrol(path = "/admin/deleteScript", method = MethodType.GET)
    public String deleteScript() {

        Long userId = AuthUtil.getUserId(getRequest().getSession());
        if (script.getId() != null) {
            ScriptDB.deleteScript(script.getId(), userId);
        }
        return "redirect:/admin/viewScripts.ktrl?sortedSet.orderByDirection=" + sortedSet.getOrderByDirection() + "&sortedSet.orderByField=" + sortedSet.getOrderByField();
    }


    /**
     * Validates all fields for adding a user
     */
    @Validate(input = "/admin/view_scripts.html")
    public void validateSaveScript() {
        if (script == null
                || script.getDisplayNm() == null
                || script.getDisplayNm().trim().equals("")) {
            addFieldError("script.displayNm", "Required");
        }

        if (script == null
                || script.getScript() == null
                || script.getScript().trim().equals("")
                || (new Script()).getScript().trim().equals(script.getScript().trim())
                ) {
            addFieldError("script.script", "Required");
        }

        if (!this.getFieldErrors().isEmpty()) {
            Long userId = AuthUtil.getUserId(getRequest().getSession());
            sortedSet = ScriptDB.getScriptSet(sortedSet, userId);
        }

    }


}
