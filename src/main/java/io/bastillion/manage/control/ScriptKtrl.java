/**
 *    Copyright (C) 2013 Loophole, LLC
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects for
 *    all of the code used other than as permitted herein. If you modify file(s)
 *    with this exception, you may extend this exception to your version of the
 *    file(s), but you are not obligated to do so. If you do not wish to do so,
 *    delete this exception statement from your version. If you delete this
 *    exception statement from all source files in the program, then also delete
 *    it in the license file.
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
