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
package io.bastillion.manage.model;

import org.apache.commons.lang3.StringUtils;


/**
 * User theme value object
 */
public class UserSettings {

    String[] colors = null;
    String bg;
    String fg;
    String plane;
    String theme;
    Integer ptyWidth;
    Integer ptyHeight;

    public String[] getColors() {
        return colors;
    }

    public void setColors(String[] colors) {
        this.colors = colors;
    }

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public String getFg() {
        return fg;
    }

    public void setFg(String fg) {
        this.fg = fg;
    }


    public String getPlane() {
        if(StringUtils.isNotEmpty(bg) && StringUtils.isNotEmpty(fg)){
            plane=bg+","+fg;
        }
        return plane;
    }

    public void setPlane(String plane) {
        if(StringUtils.isNotEmpty(plane) && plane.split(",").length==2){
            this.setBg(plane.split(",")[0]);
            this.setFg(plane.split(",")[1]);
        }
        this.plane = plane;
    }

    public String getTheme() {
        if(this.colors!=null && this.colors.length==16){
            theme=StringUtils.join(this.colors,",");
        }
        return theme;
    }

    public void setTheme(String theme) {
        if(StringUtils.isNotEmpty(theme) && theme.split(",").length==16){
            this.setColors(theme.split(","));
        }
        this.theme = theme;
    }

    public Integer getPtyWidth() {
        return ptyWidth;
    }

    public void setPtyWidth(Integer ptyWidth) {
        this.ptyWidth = ptyWidth;
    }

    public Integer getPtyHeight() {
        return ptyHeight;
    }

    public void setPtyHeight(Integer ptyHeight) {
        this.ptyHeight = ptyHeight;
    }
}
