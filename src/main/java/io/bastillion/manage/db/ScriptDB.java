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
package io.bastillion.manage.db;


import io.bastillion.manage.model.Script;
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO to manage scripts
 */
public class ScriptDB {

    private static Logger log = LoggerFactory.getLogger(ScriptDB.class);

    public static final String DISPLAY_NM = "display_nm";
    public static final String SORT_BY_DISPLAY_NM= DISPLAY_NM;

    private ScriptDB() {
    }


    /**
     * returns scripts based on sort order defined
     * @param sortedSet object that defines sort order
     * @param userId user id
     * @return sorted script list
     */
    public static SortedSet getScriptSet(SortedSet sortedSet, Long userId) {

        ArrayList<Script> scriptList = new ArrayList<>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }
        String sql = "select * from scripts where user_id=? " + orderBy;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Script script = new Script();
                script.setId(rs.getLong("id"));
                script.setDisplayNm(rs.getString(DISPLAY_NM));
                script.setScript(rs.getString("script"));

                scriptList.add(script);

            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        sortedSet.setItemList(scriptList);
        return sortedSet;
    }


    /**
     * returns script base on id
     * @param scriptId script id
     * @param userId user id
     * @return script object
     */
    public static Script getScript(Long scriptId, Long userId) {

        Script script = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            script = getScript(con, scriptId, userId);


        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return script;
    }

    /**
     * returns script base on id
     * @param con DB connection
     * @param scriptId script id
     * @param userId user id
     * @return script object
     */
    public static Script getScript(Connection con, Long scriptId, Long userId) {

        Script script = null;
        try {
            PreparedStatement stmt = con.prepareStatement("select * from  scripts where id=? and user_id=?");
            stmt.setLong(1, scriptId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                script = new Script();
                script.setId(rs.getLong("id"));
                script.setDisplayNm(rs.getString(DISPLAY_NM));
                script.setScript(rs.getString("script"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return script;
    }

    /**
     * inserts new script
     * @param script script object
     * @param userId user id
     */
    public static void insertScript(Script script, Long userId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("insert into scripts (display_nm, script, user_id) values (?,?,?)");
            stmt.setString(1, script.getDisplayNm());
            stmt.setString(2, script.getScript());
            stmt.setLong(3, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * updates existing script
     * @param script script object
     * @param userId user id
     */
    public static void updateScript(Script script, Long userId) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("update scripts set display_nm=?, script=? where id=? and user_id=?");
            stmt.setString(1, script.getDisplayNm());
            stmt.setString(2, script.getScript());
            stmt.setLong(3, script.getId());
            stmt.setLong(4, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }

    /**
     * deletes script
     * @param scriptId script id
     * @param userId user id
     */
    public static void deleteScript(Long scriptId, Long userId) {

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from scripts where id=? and user_id=?");
            stmt.setLong(1, scriptId);
            stmt.setLong(2, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }
    }
}
