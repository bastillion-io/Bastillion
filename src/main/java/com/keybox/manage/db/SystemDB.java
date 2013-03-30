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
package com.keybox.manage.db;

import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * DAO used to manage systems
 */
public class SystemDB {

    public static final String SORT_BY_NAME="display_nm";
    public static final String SORT_BY_USER="user";
    public static final String SORT_BY_HOST="host";


    /**
     * method to do order by based on the sorted set object for systems
     * @param sortedSet sorted set object
     * @return sortedSet with list of host systems
     */
    public static SortedSet getSystemSet(SortedSet sortedSet){
        List<HostSystem> hostSystemList = new ArrayList<HostSystem>();

        String orderBy="";
        if(sortedSet.getOrderByField()!=null && !sortedSet.getOrderByField().trim().equals("")){
            orderBy="order by " + sortedSet.getOrderByField()+ " " + sortedSet.getOrderByDirection();
        }
        String sql="select * from  system "+orderBy;

        Connection con=null;
        try {
            con=DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HostSystem hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setDisplayNm(rs.getString("display_nm"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setAuthorizedKeys(rs.getString("authorized_keys"));
                hostSystemList.add(hostSystem);
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        sortedSet.setItemList(hostSystemList);
        return sortedSet;

    }



    /**
     * returns system by id
     * @param id system id
     * @return system
     */
    public static HostSystem getSystem(Long id) {

        HostSystem hostSystem = null;

        Connection con = null;

        try {
            con = DBUtils.getConn();

            getSystem(con, id);


        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return hostSystem;
    }


    /**
     * returns system by id
     * @param con DB connection
     * @param id system id
     * @return system
     */
    public static HostSystem getSystem(Connection con, Long id) {

        HostSystem hostSystem = null;


        try {

            PreparedStatement stmt = con.prepareStatement("select * from  system where id=?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hostSystem = new HostSystem();
                hostSystem.setId(rs.getLong("id"));
                hostSystem.setDisplayNm(rs.getString("display_nm"));
                hostSystem.setUser(rs.getString("user"));
                hostSystem.setHost(rs.getString("host"));
                hostSystem.setPort(rs.getInt("port"));
                hostSystem.setAuthorizedKeys(rs.getString("authorized_keys"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return hostSystem;
    }


    /**
     * inserts host system into DB
     * @param hostSystem host system object
     */
    public static void insertSystem(HostSystem hostSystem) {


        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("insert into system (display_nm, user, host, port, authorized_keys) values (?,?,?,?,?)");
            stmt.setString(1, hostSystem.getDisplayNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getAuthorizedKeys());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * updates host system record
     * @param hostSystem host system object
     */
    public static void updateSystem(HostSystem hostSystem) {


        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("update system set display_nm=?, user=?, host=?, port=?,authorized_keys=?  where id=?");
            stmt.setString(1, hostSystem.getDisplayNm());
            stmt.setString(2, hostSystem.getUser());
            stmt.setString(3, hostSystem.getHost());
            stmt.setInt(4, hostSystem.getPort());
            stmt.setString(5, hostSystem.getAuthorizedKeys());
            stmt.setLong(6, hostSystem.getId());
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }

    /**
     * deletes host system
     * @param hostSystemId host system id
     */
    public static void deleteSystem(Long hostSystemId) {


        Connection con = null;

        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("delete from system where id=?");
            stmt.setLong(1, hostSystemId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

    }


}
