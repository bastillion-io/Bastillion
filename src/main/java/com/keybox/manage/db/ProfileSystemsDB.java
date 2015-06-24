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
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.keybox.common.util.AppConfig;
import com.keybox.manage.model.AWSCred;
import com.keybox.manage.model.ApplicationKey;
import com.keybox.manage.model.HostSystem;
import com.keybox.manage.model.Profile;
import com.keybox.manage.util.AWSClientConfig;
import com.keybox.manage.util.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * DAO to manage profile information
 */
public class ProfileSystemsDB {

	/**
	 * adds a host system to profile
	 *
	 * @param profileId profile id
	 * @param systemId  host system id
	 */
	public static void addSystemToProfile(Long profileId, Long systemId) {

		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("insert into system_map (profile_id, system_id) values (?,?)");
			stmt.setLong(1, profileId);
			stmt.setLong(2, systemId);
			stmt.execute();
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
	}

	/**
	 * deletes all systems for a given profile
	 *
	 * @param profileId profile id
	 */
	public static void deleteAllSystemsFromProfile(Long profileId) {

		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("delete from system_map where profile_id=?");
			stmt.setLong(1, profileId);
			stmt.execute();
			DBUtils.closeStmt(stmt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
	}


	/**
	 * returns a list of systems for a given profile
	 *
	 * @param con       DB connection
	 * @param profileId profile id
	 * @return list of host systems
	 */
	public static List<HostSystem> getSystemsByProfile(Connection con, Long profileId) {

		List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
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
				hostSystem.setEnabled(rs.getBoolean("enabled"));
				hostSystemList.add(hostSystem);
			}
			DBUtils.closeRs(rs);
			DBUtils.closeStmt(stmt);
		} catch (Exception e) {
			e.printStackTrace();
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

		List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
		Connection con = null;
		try {
			con=DBUtils.getConn();
			hostSystemList=getSystemsByProfile(con,profileId);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
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

		List<Long> systemIdList = new ArrayList<Long>();
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
			e.printStackTrace();
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

		List<Long> systemIdList = new ArrayList<Long>();
		Connection con = null;
		try {
			con = DBUtils.getConn();
			systemIdList = getSystemIdsByProfile(con, profileId);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
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

		List<Long> systemIdList = new ArrayList<Long>();
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
			e.printStackTrace();
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

		List<Long> systemIdList = new ArrayList<Long>();
		Connection con = null;
		try {
			con = DBUtils.getConn();
			systemIdList = getSystemIdsByProfile(con, profileId, userId);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return systemIdList;
	}
	
	/**
	 * Update Profile and AWS Systems in DB
	 * <br><br>
	 * Call first updateAWSSystem() to Update EC2 systems<br>
	 * Clean SystemProfileEntries from EC2 systems<br>
	 * Rebuild SystemProfileEntries for EC2 systems
	 */
	public static void updateProfileAWSSysteme() {
		
		SystemDB.updateAWSSystems();
		deleteAWSSystemProfileEntries();
		
		try {
			List<Profile> profileList = ProfileDB.getAllProfiles();
			for (Profile profile : profileList) {
				if(profile.getTag().equals("")){
					continue;
				}
				
				List<String> ec2RegionList = PrivateKeyDB.getEC2Regions();
				
				//remove Formating Char
				String tags = profile.getTag().replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "");
				
				String[] tagArr1 = tags.split(",");
				
				Map<String, String> tagMap = new HashMap<>();
	            List<String> tagList = new ArrayList<>();
				
	            if (tagArr1.length > 0) {
	                for (String tag1 : tagArr1) {
	                    String[] tagArr2 = tag1.split("=");
	                    if (tagArr2.length > 1) {
	                        tagMap.put(tag1.split("=")[0], tag1.split("=")[1]);
	                    } else {
	                        tagList.add(tag1);
	                    }
	                }
	            }
				
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
                            
                            if (tagList.size() > 0) {
                                Filter tagFilter = new Filter("tag-key", tagList);
                                describeInstancesRequest.withFilters(tagFilter);
                            }

                            //set name value pair for tag filter
                            for (String tag : tagMap.keySet()) {
                                Filter tagValueFilter = new Filter("tag:" + tag, Arrays.asList(tagMap.get(tag)));
                                describeInstancesRequest.withFilters(tagValueFilter);
                            }
                            
                            DescribeInstancesResult describeInstancesResult = service.describeInstances(describeInstancesRequest);


                            for (Reservation res : describeInstancesResult.getReservations()) {
                                for (Instance instance : res.getInstances()) {
                                	HostSystem ec2System = SystemDB.getSystemByInstance(instance.getInstanceId());
                                    addSystemToProfile(profile.getId(), ec2System.getId());
                                }
                            }
                        }
                    }
                }	
			}
		}catch (AmazonServiceException ex)
        {
            ex.printStackTrace();
        }
	}
	
	/**
	 * Delete all Entries from system_map where system has not instance_id "---" 
	 */
	public static void deleteAWSSystemProfileEntries() {
		
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("DELETE FROM system_map sm WHERE sm.system_id IN (SELECT s.id FROM system s WHERE s.instance_id NOT LIKE '---')");
			stmt.execute();
			DBUtils.closeStmt(stmt);

		} catch (Exception e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
	}
}
