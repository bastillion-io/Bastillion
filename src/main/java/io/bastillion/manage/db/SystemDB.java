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
import io.bastillion.manage.model.SortedSet;
import io.bastillion.manage.util.DBUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * DAO used to manage systems
 */
public class SystemDB {

	public static final String AUTHORIZED_KEYS = "authorized_keys";
	private static Logger log = LoggerFactory.getLogger(SystemDB.class);

	public static final String FILTER_BY_PROFILE_ID = "profile_id";

	public static final String DISPLAY_NM = "display_nm";
	public static final String SORT_BY_NAME = DISPLAY_NM;
	public static final String SORT_BY_USER = "user";
	public static final String SORT_BY_HOST = "host";
	public static final String STATUS_CD = "status_cd";
	public static final String PROFILE_ID = "profile_id";
	public static final String SORT_BY_STATUS = STATUS_CD;

	private SystemDB() {
	}


	/**
	 * method to do order by based on the sorted set object for systems for user
	 *
	 * @param sortedSet sorted set object
	 * @param userId    user id
	 * @return sortedSet with list of host systems
	 */
	public static SortedSet getUserSystemSet(SortedSet sortedSet, Long userId) {
		List<HostSystem> hostSystemList = new ArrayList<>();

		String orderBy = "";
		if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
			orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
		}
		String sql = "select * from system where id in (select distinct system_id from  system_map m, user_map um where m.profile_id=um.profile_id and um.user_id=? ";
		//if profile id exists add to statement
		sql += StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) ? " and um.profile_id=? " : "";
		sql += ") " + orderBy;

		//get user for auth token
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setLong(1, userId);
			//filter by profile id if exists
			if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID))) {
				stmt.setLong(2, Long.parseLong(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)));
			}

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				HostSystem hostSystem = new HostSystem();
				hostSystem.setId(rs.getLong("id"));
				hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
				hostSystem.setUser(rs.getString("user"));
				hostSystem.setHost(rs.getString("host"));
				hostSystem.setPort(rs.getInt("port"));
				hostSystem.setAuthorizedKeys(rs.getString(AUTHORIZED_KEYS));
				hostSystem.setStatusCd(rs.getString(STATUS_CD));
				hostSystemList.add(hostSystem);
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		sortedSet.setItemList(hostSystemList);
		return sortedSet;

	}


	/**
	 * method to do order by based on the sorted set object for systems
	 *
	 * @param sortedSet sorted set object
	 * @profileId check if system is apart of given profile
	 * @return sortedSet with list of host systems
	 */
	public static SortedSet getSystemSet(SortedSet sortedSet, Long profileId) {
		List<HostSystem> hostSystemList = new ArrayList<>();

		String orderBy = "";
		if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
			orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
		}
		String sql = "select s.*, m.profile_id from  system s left join system_map  m on m.system_id = s.id and m.profile_id = ? " + orderBy;

		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setLong(1, profileId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				HostSystem hostSystem = new HostSystem();
				hostSystem.setId(rs.getLong("id"));
				hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
				hostSystem.setUser(rs.getString("user"));
				hostSystem.setHost(rs.getString("host"));
				hostSystem.setPort(rs.getInt("port"));
				hostSystem.setAuthorizedKeys(rs.getString(AUTHORIZED_KEYS));
				hostSystem.setStatusCd(rs.getString(STATUS_CD));
				if (profileId !=null && profileId.equals(rs.getLong(PROFILE_ID))) {
					hostSystem.setChecked(true);
				} else {
					hostSystem.setChecked(false);
				}
				hostSystemList.add(hostSystem);
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		sortedSet.setItemList(hostSystemList);
		return sortedSet;
	}

	/**
	 * method to do order by based on the sorted set object for systems
	 *
	 * @param sortedSet sorted set object
	 * @return sortedSet with list of host systems
	 */
	public static SortedSet getSystemSet(SortedSet sortedSet) {
		List<HostSystem> hostSystemList = new ArrayList<>();

		String orderBy = "";
		if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
			orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
		}
		String sql = "select * from  system s ";
		//if profile id exists add to statement
		sql += StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) ? ",system_map m where s.id=m.system_id and m.profile_id=? " : "";
		sql += orderBy;

		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement(sql);
			if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID))) {
				stmt.setLong(1, Long.parseLong(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)));
			}
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				HostSystem hostSystem = new HostSystem();
				hostSystem.setId(rs.getLong("id"));
				hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
				hostSystem.setUser(rs.getString("user"));
				hostSystem.setHost(rs.getString("host"));
				hostSystem.setPort(rs.getInt("port"));
				hostSystem.setAuthorizedKeys(rs.getString(AUTHORIZED_KEYS));
				hostSystem.setStatusCd(rs.getString(STATUS_CD));
				hostSystemList.add(hostSystem);
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		sortedSet.setItemList(hostSystemList);
		return sortedSet;

	}


	/**
	 * returns system by id
	 *
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
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		return hostSystem;
	}


	/**
	 * returns system by id
	 *
	 * @param con DB connection
	 * @param id  system id
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
				hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
				hostSystem.setUser(rs.getString("user"));
				hostSystem.setHost(rs.getString("host"));
				hostSystem.setPort(rs.getInt("port"));
				hostSystem.setAuthorizedKeys(rs.getString(AUTHORIZED_KEYS));
				hostSystem.setStatusCd(rs.getString(STATUS_CD));
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}


		return hostSystem;
	}


	/**
	 * inserts host system into DB
	 *
	 * @param hostSystem host system object
	 * @return user id
	 */
	public static Long insertSystem(HostSystem hostSystem) {


		Connection con = null;

		Long userId = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("insert into system (display_nm, user, host, port, authorized_keys, status_cd) values (?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
			stmt.setString(1, hostSystem.getDisplayNm());
			stmt.setString(2, hostSystem.getUser());
			stmt.setString(3, hostSystem.getHost());
			stmt.setInt(4, hostSystem.getPort());
			stmt.setString(5, hostSystem.getAuthorizedKeys());
			stmt.setString(6, hostSystem.getStatusCd());
			stmt.execute();

			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				userId = rs.getLong(1);
			}
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}
		return userId;
	}

	/**
	 * updates host system record
	 *
	 * @param hostSystem host system object
	 */
	public static void updateSystem(HostSystem hostSystem) {


		Connection con = null;

		try {
			con = DBUtils.getConn();

			PreparedStatement stmt = con.prepareStatement("update system set display_nm=?, user=?, host=?, port=?, authorized_keys=?, status_cd=?  where id=?");
			stmt.setString(1, hostSystem.getDisplayNm());
			stmt.setString(2, hostSystem.getUser());
			stmt.setString(3, hostSystem.getHost());
			stmt.setInt(4, hostSystem.getPort());
			stmt.setString(5, hostSystem.getAuthorizedKeys());
			stmt.setString(6, hostSystem.getStatusCd());
			stmt.setLong(7, hostSystem.getId());
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
	 * deletes host system
	 *
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
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}
	}

	/**
	 * returns the host systems
	 *
	 * @param systemIdList list of host system ids
	 * @return host system with array of public keys
	 */
	public static List<HostSystem> getSystems(List<Long> systemIdList) {


		Connection con = null;
		List<HostSystem> hostSystemListReturn = new ArrayList<>();

		try {
			con = DBUtils.getConn();
			for (Long systemId : systemIdList) {
				HostSystem hostSystem = getSystem(con, systemId);
				hostSystemListReturn.add(hostSystem);
			}

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		return hostSystemListReturn;

	}



	/**
	 * returns all systems
	 *
	 * @return system list
	 */
	public static List<HostSystem> getAllSystems() {

		List<HostSystem> hostSystemList = new ArrayList<>();

		Connection con = null;

		try {
			con=DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("select * from system");
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				HostSystem hostSystem = new HostSystem();
				hostSystem.setId(rs.getLong("id"));
				hostSystem.setDisplayNm(rs.getString(DISPLAY_NM));
				hostSystem.setUser(rs.getString("user"));
				hostSystem.setHost(rs.getString("host"));
				hostSystem.setPort(rs.getInt("port"));
				hostSystem.setAuthorizedKeys(rs.getString(AUTHORIZED_KEYS));
				hostSystem.setStatusCd(rs.getString(STATUS_CD));
				hostSystemList.add(hostSystem);
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			log.error(e.toString(), e);
		}
		finally {
			DBUtils.closeConn(con);
		}

		return hostSystemList;
	}


	/**
	 * returns all system ids
	 *
	 * @param con DB connection
	 * @return system
	 */
	public static List<Long> getAllSystemIds(Connection con) {

		List<Long> systemIdList = new ArrayList<>();


		try {
			PreparedStatement stmt = con.prepareStatement("select * from system");
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
	 * returns all system ids for user
	 *
	 * @param con    DB connection
	 * @param userId user id
	 * @return system
	 */
	public static List<Long> getAllSystemIdsForUser(Connection con, Long userId) {

		List<Long> systemIdList = new ArrayList<>();


		try {
			PreparedStatement stmt = con.prepareStatement("select distinct system_id from system_map m, user_map um, system s where m.profile_id=um.profile_id and um.user_id=?");
			stmt.setLong(1, userId);
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
	 * returns all system ids for user
	 *
	 * @param userId user id
	 * @return system
	 */
	public static List<Long> getAllSystemIdsForUser(Long userId) {
		Connection con = null;
		List<Long> systemIdList = new ArrayList<>();
		try {
			con = DBUtils.getConn();
			systemIdList = getAllSystemIdsForUser(con, userId);

		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
		finally {
			DBUtils.closeConn(con);
		}
		return systemIdList;
	}

	/**
	 * returns all system ids
	 *
	 * @return system
	 */
	public static List<Long> getAllSystemIds() {
		Connection con = null;
		List<Long> systemIdList = new ArrayList<>();
		try {
			con = DBUtils.getConn();
			systemIdList = getAllSystemIds(con);

		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		}
		finally {
			DBUtils.closeConn(con);
		}
		return systemIdList;

	}

	/**
	 * method to check system permissions for user
	 *
	 * @param con DB connection
	 * @param systemSelectIdList list of system ids to check
	 * @param userId             user id
	 * @return only system ids that user has perms for
	 */
	public static List<Long> checkSystemPerms(Connection con, List<Long> systemSelectIdList, Long userId) {

		List<Long> systemIdList = new ArrayList<>();
		List<Long> userSystemIdList = getAllSystemIdsForUser(con, userId);

		for (Long systemId : userSystemIdList) {
			if (systemSelectIdList.contains(systemId)) {
				systemIdList.add(systemId);
			}
		}

		return systemIdList;

	}

}
