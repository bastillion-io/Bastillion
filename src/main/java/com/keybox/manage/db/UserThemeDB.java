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
package com.keybox.manage.db;

import com.keybox.manage.model.UserSettings;
import com.keybox.manage.util.DBUtils;
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
