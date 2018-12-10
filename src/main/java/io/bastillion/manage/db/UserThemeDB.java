/**
 *    Copyright (C) 2015 Loophole, LLC
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

import io.bastillion.manage.model.UserSettings;
import io.bastillion.manage.util.DBUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO to manage user themes 
 */
public class UserThemeDB {

    private static Logger log = LoggerFactory.getLogger(UserThemeDB.class);

    private UserThemeDB() {
    }

    /**
     * get user theme
     *
     * @param userId object
     * @return user theme object
     */
    public static UserSettings getTheme(Long userId) {

        UserSettings theme=null;
        Connection con = null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("select * from user_theme where user_id=?");
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                theme= new UserSettings();
                theme.setBg(rs.getString("bg"));
                theme.setFg(rs.getString("fg"));
                if(StringUtils.isNotEmpty(rs.getString("d1"))) {
                    String[] colors= new String[16];
                    colors[0] = rs.getString("d1");
                    colors[1] = rs.getString("d2");
                    colors[2] = rs.getString("d3");
                    colors[3] = rs.getString("d4");
                    colors[4] = rs.getString("d5");
                    colors[5] = rs.getString("d6");
                    colors[6] = rs.getString("d7");
                    colors[7] = rs.getString("d8");
                    colors[8] = rs.getString("b1");
                    colors[9] = rs.getString("b2");
                    colors[10] = rs.getString("b3");
                    colors[11] = rs.getString("b4");
                    colors[12] = rs.getString("b5");
                    colors[13] = rs.getString("b6");
                    colors[14] = rs.getString("b7");
                    colors[15] = rs.getString("b8");
                    theme.setColors(colors);
                }
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

        return theme;

    }
    

    /**
     * saves user theme
     * 
     * @param userId object
     */
    public static void saveTheme(Long userId, UserSettings theme) {


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from user_theme where user_id=?");
            stmt.setLong(1, userId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

            if(org.apache.commons.lang.StringUtils.isNotEmpty(theme.getPlane())|| org.apache.commons.lang.StringUtils.isNotEmpty(theme.getTheme())) {

                stmt = con.prepareStatement("insert into user_theme(user_id, bg, fg, d1, d2, d3, d4, d5, d6, d7, d8, b1, b2, b3, b4, b5, b6, b7, b8) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                stmt.setLong(1, userId);
                stmt.setString(2, theme.getBg());
                stmt.setString(3, theme.getFg());
                //if contains all 16 theme colors insert
                if (theme.getColors() != null && theme.getColors().length == 16) {
                    for (int i = 0; i < 16; i++) {
                        stmt.setString(i + 4, theme.getColors()[i]);
                    }
                    //else set to null
                } else {
                    for (int i = 0; i < 16; i++) {
                        stmt.setString(i + 4, null);
                    }
                }
                stmt.execute();
                DBUtils.closeStmt(stmt);
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        finally {
            DBUtils.closeConn(con);
        }

    }
}
