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

import io.bastillion.manage.model.HostSystem;
import io.bastillion.manage.util.DBUtils;

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
