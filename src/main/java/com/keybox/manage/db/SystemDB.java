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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.opsworks.model.DescribeUserProfilesRequest;
import com.amazonaws.services.opsworks.model.DescribeUserProfilesResult;
import com.keybox.common.util.AppConfig;
import com.keybox.manage.model.AWSCred;
import com.keybox.manage.model.ApplicationKey;
import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.util.AWSClientConfig;
import com.keybox.manage.util.DBUtils;

import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * DAO used to manage systems
 */
public class SystemDB {

	public static final String FILTER_BY_PROFILE_ID = "profile_id";
	public static final String FILTER_BY_REGION_ID = "region";

	public static final String SORT_BY_NAME = "display_nm";
	public static final String SORT_BY_USER = "user";
	public static final String SORT_BY_HOST = "host";
	public static final String SORT_BY_STATUS = "status_cd";
	public static final String SORT_BY_ENABLED = "enabled";
	public static final String SORT_BY_INSTANCE_ID = "instance_id";


	/**
	 * method to do order by based on the sorted set object for systems for user
	 *
	 * @param sortedSet sorted set object
	 * @param userId    user id
	 * @return sortedSet with list of host systems
	 */
	public static SortedSet getUserSystemSet(SortedSet sortedSet, Long userId) {
		List<HostSystem> hostSystemList = new ArrayList<HostSystem>();

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
				stmt.setLong(2, Long.valueOf(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)));
			}
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				HostSystem hostSystem = getSystem(rs.getLong("id"));
				hostSystem.setPublicKeyList(PublicKeyDB.getPublicKeysForUserandSystem(userId, hostSystem.getId()));
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
	 * method to do order by based on the sorted set object for systems
	 *
	 * @param sortedSet sorted set object
	 * @return sortedSet with list of host systems
	 */
	public static SortedSet getSystemSet(SortedSet sortedSet) {
		List<HostSystem> hostSystemList = new ArrayList<HostSystem>();

		String orderBy = "";
		if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
			orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
		}
		String sql = "select * from  system s ";
		//if profile id exists add to statement
		sql += StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) ? ",system_map m where s.id=m.system_id and m.profile_id=?" : "";
		
		sql += (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) & StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_REGION_ID))) ? " and " : "";
		sql += (!StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) & StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_REGION_ID))) ? " where " : "";
		
		sql += StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_REGION_ID)) ? "s.region = ?" : "";
		
		sql += orderBy;

		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement(sql);
			int i = 1;
			if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID))) {
				stmt.setLong(i, Long.valueOf(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)));
				i++;
			}
			if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_REGION_ID))) {
				stmt.setString(i, sortedSet.getFilterMap().get(FILTER_BY_REGION_ID));
			}
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				hostSystemList.add(getSystem(rs.getLong("id")));
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
	 *
	 * @param id system id
	 * @return system
	 */
	public static HostSystem getSystem(Long id) {

		HostSystem hostSystem = null;
		Connection con = null;
		try {
			con = DBUtils.getConn();
			hostSystem = getSystem(con, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
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
				hostSystem.setDisplayNm(rs.getString("display_nm"));
				hostSystem.setUser(rs.getString("user"));
				hostSystem.setHost(rs.getString("host"));
				hostSystem.setPort(rs.getInt("port"));
				hostSystem.setAuthorizedKeys(rs.getString("authorized_keys"));
				hostSystem.setStatusCd(rs.getString("status_cd"));
				hostSystem.setEnabled(rs.getBoolean("enabled"));
				hostSystem.setInstance(rs.getString("instance_id"));
				hostSystem.setEc2Region(rs.getString("region"));
				hostSystem.setApplicationKey(PrivateKeyDB.getApplicationKeyBySystemID(hostSystem.getId()));
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
	 *
	 * @param hostSystem host system object
	 * @return system id
	 */
	public static Long insertSystem(HostSystem hostSystem) {

		Connection con = null;
		Long systemId = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("insert into system (display_nm, user, host, port, authorized_keys, status_cd, enabled, instance_id, region) values (?,?,?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
			stmt.setString(1, hostSystem.getDisplayNm());
			stmt.setString(2, hostSystem.getUser());
			stmt.setString(3, hostSystem.getHost());
			stmt.setInt(4, hostSystem.getPort());
			stmt.setString(5, hostSystem.getAuthorizedKeys());
			stmt.setString(6, hostSystem.getStatusCd());
			stmt.setBoolean(7, hostSystem.isEnabled());
			stmt.setString(8, hostSystem.getInstance());
			stmt.setString(9, hostSystem.getEc2Region());
			stmt.execute();
			
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				systemId = rs.getLong(1);
			}
			DBUtils.closeStmt(stmt);
			PrivateKeyDB.setActiveApplicationKeyforSystemID(hostSystem.getApplicationKey().getId(), systemId);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return systemId;
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
			PreparedStatement stmt = con.prepareStatement("update system set display_nm=?, user=?, host=?, port=?, authorized_keys=?, status_cd=?, enabled=?, instance_id=?, region=? where id=?");
			stmt.setString(1, hostSystem.getDisplayNm());
			stmt.setString(2, hostSystem.getUser());
			stmt.setString(3, hostSystem.getHost());
			stmt.setInt(4, hostSystem.getPort());
			stmt.setString(5, hostSystem.getAuthorizedKeys());
			stmt.setString(6, hostSystem.getStatusCd());
			stmt.setBoolean(7, hostSystem.isEnabled());
			stmt.setString(8, hostSystem.getInstance());
			stmt.setString(9, hostSystem.getEc2Region());
			stmt.setLong(10, hostSystem.getId());
			if(hostSystem.getApplicationKey().getId()!=null){
				PrivateKeyDB.updateActiveApplicationKeyforSystemID(hostSystem.getApplicationKey().getId(), hostSystem.getId());
			}
			stmt.execute();
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
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
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
	}

	/**
	 * returns the host systems
	 *
	 * @param systemIdList list of host system ids
	 * @return host system with array of public keys
	 */
	public static List<HostSystem> getSystems(List<Long> systemIdList) {

		Connection con = null;
		List<HostSystem> hostSystemListReturn = new ArrayList<HostSystem>();
		try {
			con = DBUtils.getConn();
			for (Long systemId : systemIdList) {
				HostSystem hostSystem = getSystem(con, systemId);
				hostSystemListReturn.add(hostSystem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return hostSystemListReturn;
	}



	/**
	 * returns all systems
	 *
	 * @return system list
	 */
	public static List<HostSystem> getAllSystems() {

		List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
		Connection con = null;
		try {
			con=DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("select * from  system");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				hostSystemList.add(getSystem(rs.getLong("id")));
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return hostSystemList;
	}


	/**
	 * returns all system ids
	 *
	 * @param con DB connection
	 * @return system
	 */
	public static List<Long> getAllSystemIds(Connection con) {

		List<Long> systemIdList = new ArrayList<Long>();
		try {
			PreparedStatement stmt = con.prepareStatement("select * from  system");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				systemIdList.add(rs.getLong("id"));
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);
		} catch (Exception e) {
			e.printStackTrace();
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

		List<Long> systemIdList = new ArrayList<Long>();
		try {
			PreparedStatement stmt = con.prepareStatement("select distinct system_id from  system_map m, user_map um where m.profile_id=um.profile_id and um.user_id=?");
			stmt.setLong(1, userId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				systemIdList.add(rs.getLong("system_id"));
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);
		} catch (Exception e) {
			e.printStackTrace();
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
		List<Long> systemIdList = new ArrayList<Long>();
		try {
			con = DBUtils.getConn();
			systemIdList = getAllSystemIdsForUser(con, userId);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		DBUtils.closeConn(con);
		return systemIdList;
	}

	/**
	 * returns all system ids
	 *
	 * @return system
	 */
	public static List<Long> getAllSystemIds() {
		Connection con = null;
		List<Long> systemIdList = new ArrayList<Long>();
		try {
			con = DBUtils.getConn();
			systemIdList = getAllSystemIds(con);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		DBUtils.closeConn(con);
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

		List<Long> systemIdList = new ArrayList<Long>();
		List<Long> userSystemIdList = getAllSystemIdsForUser(con, userId);

		for (Long systemId : userSystemIdList) {
			if (systemSelectIdList.contains(systemId)) {
				systemIdList.add(systemId);
			}
		}
		return systemIdList;
	}
	
	
	/**
	 * method to disable System
	 * (Change enable-Attribute to false)
	 * 
	 * @param id System-ID
	 * @author Robert Vorkoeper
	 */
	public static void disableSystem(Long id) {
		
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("update system set enabled = false where id=?");
			stmt.setLong(1, id);
			stmt.execute();
			DBUtils.closeStmt(stmt);
			
		} catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
	}

	/**
	 * method to enable System
	 * (Change enable-Attribute to true)
	 * 
	 * @param id System-ID
	 * @author Robert Vorkoeper
	 */
	public static void enableSystem(Long id) {
		
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("update system set enabled = true where id=?");
			stmt.setLong(1, id);
			stmt.execute();
			DBUtils.closeStmt(stmt);
		} catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
	}

	/**
	 * method to do order by based on the sorted set object for systems for admin
	 *
	 * @param sortedSet sorted set object
	 * @param userId    user id
	 * @return sortedSet with list of host systems
	 */
	public static SortedSet getAdminSystemSet(SortedSet sortedSet, Long userId) {
		List<HostSystem> hostSystemList = new ArrayList<HostSystem>();

		String orderBy = "";
		if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
			orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
		}
		String sql = "select * from  system s ";
		//if profile id exists add to statement
		sql += StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)) ? ",system_map m where s.id=m.system_id and m.profile_id=?" : "";
		sql += orderBy;

		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement(sql);
			if (StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID))) {
				stmt.setLong(1, Long.valueOf(sortedSet.getFilterMap().get(FILTER_BY_PROFILE_ID)));
			}
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				HostSystem hostSystem = getSystem(rs.getLong("id"));
				hostSystem.setPublicKeyList(PublicKeyDB.getPublicKeysForAdminandSystem(userId, hostSystem.getId()));
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
	 * Get all HostSystems Where ApplicationKey older than X days and not InitialApplicationKeys
	 * 
	 * @param days older than X days
	 * @return HostSystems
	 */
	public static List<HostSystem> getAllSystemsWhereApplicationKeyOlderThan(Integer days) {
		
		List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
		Connection con = null;
		
		Calendar moment = Calendar.getInstance();
		moment.add(Calendar.DAY_OF_MONTH, -days);
		Timestamp momentTS = new Timestamp(moment.getTimeInMillis());
		
		try {
			con=DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("SELECT sys.ID FROM SYSTEM sys " +
									"JOIN APPLICATION_KEY_SYSTEM appkey_sys on appkey_sys.system_id = sys.id " +
									"JOIN APPLICATION_KEY appkey on appkey_sys.application_key_id = appkey.id " +
									"WHERE appkey_sys.active = true "+
									"AND appkey.initialkey = false " +
									"AND appkey.create_dt < ? ");
			stmt.setTimestamp(1, momentTS);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				hostSystemList.add(getSystem(rs.getLong("ID")));
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return hostSystemList;
	}

	
	/**
	 * Update AWS Systems
	 * <br><br>
	 * Update all EC2 System based on setting AWS Credentials and the EC2 Keys
	 * and added new System
	 */
	public static void updateAWSSystems() {

		List<String> ec2RegionList = PrivateKeyDB.getEC2Regions();
		
		try {
			//get AWS credentials from DB
	        for (AWSCred awsCred : AWSCredDB.getAWSCredList()) {
	
	            if (awsCred != null && awsCred.isValid()) {
	                //set  AWS credentials for service
	                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());
	
	                for (String ec2Region : ec2RegionList) {
	                    //create service
	
	                    AmazonEC2 service = new AmazonEC2Client(awsCredentials, AWSClientConfig.getClientConfig());
	                    service.setEndpoint(ec2Region);
	
	                    //only return systems that have keys set
	                    List<String> keyValueList = new ArrayList<String>();
	                    for (ApplicationKey ec2Key : PrivateKeyDB.getEC2KeyByRegion(ec2Region)) {
	                    	if(ec2Key.isEnabled())
	                    	{
	                    		keyValueList.add(ec2Key.getKeyname());
	                    	}
	                    }
	
	                    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
	                    
	                    Filter keyNmFilter = new Filter("key-name", keyValueList);
	                    
	                    describeInstancesRequest.withFilters(keyNmFilter);
	
	                    DescribeInstancesResult describeInstancesResult = service.describeInstances(describeInstancesRequest);
	                    
	                    for (Reservation res : describeInstancesResult.getReservations()) {
	                        for (Instance instance : res.getInstances()) {
	                            HostSystem hostSystem = transformerEC2InstanzToHostSystem(instance, ec2Region);
	                            
	                            setEC2System(hostSystem);
	                        }
	                    }
	                }
	            }
	        }			
		} catch (AmazonServiceException ex)
        {
            ex.printStackTrace();
        }	
	}
	
	/**
	 * Insert or Update EC2System
	 * 
	 * @param hostSystem EC2System
	 */
	private static void setEC2System(HostSystem hostSystem) {
		HostSystem hostSystemTmp = getSystemByInstance(hostSystem.getInstance());
        if (hostSystemTmp == null) {
            insertSystem(hostSystem);
        } else {
        	hostSystem.setId(hostSystemTmp.getId());
            updateSystem(hostSystem);
        }
	}


	/**
	 * returns system by system instance id
     *
     * @param instanceId system instance id
     * @return system
	 */
	public static HostSystem getSystemByInstance(String instanceId) {
		HostSystem hostSystem = null;
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("select * from  system where instance_id like ?");
			stmt.setString(1, instanceId);
            ResultSet rs = stmt.executeQuery();
            
            if(rs.next()){
            	hostSystem = getSystem(rs.getLong("id"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
		DBUtils.closeConn(con);
        return hostSystem;
	}


	/**
     * HelpMethode to Transform EC2Instanz in HostSystem 
     * 
     * @param instance EC2Instanz
     * @param ec2Region EC2Region
     * @param awsCrde AWS Credential
     * @return hostSystem
     */
    private static HostSystem transformerEC2InstanzToHostSystem(Instance instance, String ec2Region) {
    	HostSystem hostSystem = new HostSystem();
        hostSystem.setInstance(instance.getInstanceId());

        //check for public dns if doesn't exist set to ip or pvt dns
        if (!"true".equals(AppConfig.getProperty("useEC2PvtDNS")) && StringUtils.isNotEmpty(instance.getPublicDnsName())) {
            hostSystem.setHost(instance.getPublicDnsName());
        } else if (!"true".equals(AppConfig.getProperty("useEC2PvtDNS")) && StringUtils.isNotEmpty(instance.getPublicIpAddress())) {
            hostSystem.setHost(instance.getPublicIpAddress());
        } else if (StringUtils.isNotEmpty(instance.getPrivateDnsName())) {
            hostSystem.setHost(instance.getPrivateDnsName());
        } else {
            hostSystem.setHost(instance.getPrivateIpAddress());
        }

        hostSystem.setApplicationKey(PrivateKeyDB.getEC2KeyByNmRegion(instance.getKeyName(), ec2Region));
        hostSystem.setEc2Region(ec2Region);
        hostSystem.setStatusCd(instance.getState().getName().toUpperCase());
        hostSystem.setUser(AppConfig.getProperty("defaultEC2User"));
        for (Tag tag : instance.getTags()) {
            if ("Name".equals(tag.getKey())) {
                hostSystem.setDisplayNm(tag.getValue());
            }
        }
        return hostSystem;
	}
    
    /**
     * Delete Systems by ApplicationKey ID
     * 
     * @param appKeyID ApplicationKey ID
     */
    public static void deleteSystemByApplicationKeyID(Long appKeyID) {
    	Connection con = null;
    	
    	try {
			con=DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("SELECT sys.ID FROM SYSTEM sys " +
									"JOIN APPLICATION_KEY_SYSTEM appkey_sys on appkey_sys.system_id = sys.id " +
									"WHERE appkey_sys.application_key_id = ?");
			stmt.setLong(1, appKeyID);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				deleteSystem(rs.getLong("ID"));
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
	}
}
