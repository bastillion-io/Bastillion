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
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO to manage profile information
 */
public class ProfileSystemsDB {

    private static Logger log = LoggerFactory.getLogger(ProfileSystemsDB.class);

	private ProfileSystemsDB() {
	}


	/**
	 * sets host systems for profile
	 * 
	 * @param profileId profile id
	 * @param systemIdList list of host system ids
	 */
	public static void setSystemsForProfile(Long profileId, List<Long> systemIdList) {


		Connection con = null;

		try {

			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("delete from system_map where profile_id=?");
			stmt.setLong(1, profileId);
			stmt.execute();
			DBUtils.closeStmt(stmt);


			for(Long systemId : systemIdList) {
				stmt = con.prepareStatement("insert into system_map (profile_id, system_id) values (?,?)");
				stmt.setLong(1, profileId);
				stmt.setLong(2, systemId);
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

	/**
	 * returns a list of systems for a given profile
	 *
	 * @param con       DB connection
	 * @param profileId profile id
	 * @return list of host systems
	 */
	public static List<HostSystem> getSystemsByProfile(Connection con, Long profileId) {

		List<HostSystem> hostSystemList = new ArrayList<>();


		try {
			PreparedStatement stmt = con.prepareStatement("select * from  system s, system_map m where s.id=m.system_id and m.profile_id=? order by display_nm asc");
			stmt.setLong(1, profileId);
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
			log.error(e.toString(), e);
		}

		return hostSystemList;
	}

	/**
	 * returns a list of systems for a given profile
	 *
	 * @param profileId profile id
	 * @return list of host systems
	 */
	public static List<HostSystem> getSystemsByProfile(Long profileId) {

		List<HostSystem> hostSystemList = new ArrayList<>();

		Connection con = null;

		try {
			con=DBUtils.getConn();
			hostSystemList=getSystemsByProfile(con,profileId);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		return hostSystemList;
	}

	/**
	 * returns a list of system ids for a given profile
	 *
	 * @param con       DB con
	 * @param profileId profile id
	 * @return list of host systems
	 */
	public static List<Long> getSystemIdsByProfile(Connection con, Long profileId) {

		List<Long> systemIdList = new ArrayList<>();


		try {
			PreparedStatement stmt = con.prepareStatement("select * from  system s, system_map m where s.id=m.system_id and m.profile_id=? order by display_nm asc");
			stmt.setLong(1, profileId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				systemIdList.add(rs.getLong("id"));
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}

		return systemIdList;
	}

	/**
	 * returns a list of system ids for a given profile
	 *
	 * @param profileId profile id
	 * @return list of host systems
	 */
	public static List<Long> getSystemIdsByProfile(Long profileId) {

		List<Long> systemIdList = new ArrayList<>();

		Connection con = null;

		try {
			con = DBUtils.getConn();
			systemIdList = getSystemIdsByProfile(con, profileId);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		return systemIdList;
	}

	/**
	 * returns a list of system ids for a given profile
	 *
	 * @param con       DB con
	 * @param profileId profile id
	 * @param userId user id
	 * @return list of host systems
	 */
	public static List<Long> getSystemIdsByProfile(Connection con, Long profileId, Long userId) {

		List<Long> systemIdList = new ArrayList<>();


		try {
			PreparedStatement stmt = con.prepareStatement("select sm.system_id from  system_map sm, user_map um where um.profile_id=sm.profile_id and sm.profile_id=? and um.user_id=?");
			stmt.setLong(1, profileId);
			stmt.setLong(2, userId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				systemIdList.add(rs.getLong("system_id"));
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}

		return systemIdList;
	}

	/**
	 * returns a list of system ids for a given profile
	 *
	 * @param profileId profile id
	 * @param userId user id
	 * @return list of host systems
	 */
	public static List<Long> getSystemIdsByProfile(Long profileId, Long userId) {

		List<Long> systemIdList = new ArrayList<>();

		Connection con = null;

		try {
			con = DBUtils.getConn();
			systemIdList = getSystemIdsByProfile(con, profileId, userId);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		return systemIdList;
	}
}
