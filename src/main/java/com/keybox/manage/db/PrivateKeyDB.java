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

import com.keybox.manage.model.ApplicationKey;
import com.keybox.manage.model.SortedSet;
import com.keybox.manage.model.User;
import com.keybox.manage.util.DBUtils;
import com.keybox.manage.util.EncryptionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO that returns public / private key for the system generated private key
 */
public class PrivateKeyDB {


    public static final String FILTER_BY_ENABLED = "enabled";


    /**
     * Inserts new ApplicationKey
     * 
     * @param applicarionKey ApplicationKey object 
     */
    public static void insertApplicationKey(ApplicationKey applicarionKey) {
    	Connection con = null;
    	long fingerprintID = 0;
    	try {
    		fingerprintID = FingerprintDB.insertFingerprint(applicarionKey.getFingerprint());
    		
    		con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("insert into application_key (keyname, public_key, private_key, passphrase, initialKey, user_id, type, fingerprint_id, enabled, ec2_region) values(?,?,?,?,?,?,?,?,?,?)");
            stmt.setString(1, applicarionKey.getKeyname());
            stmt.setString(2, applicarionKey.getPublicKey());
            stmt.setString(3, EncryptionUtil.encrypt(applicarionKey.getPrivateKey()));
            stmt.setString(4, EncryptionUtil.encrypt(applicarionKey.getPassphrase()));
            stmt.setBoolean(5, applicarionKey.isInitialkey());
            if(applicarionKey.getUserId() == null){
            	stmt.setNull(6, Types.INTEGER);		
            }else {
            	stmt.setLong(6, applicarionKey.getUserId());
            }
            stmt.setString(7, applicarionKey.getType());
            stmt.setLong(8, fingerprintID);
            stmt.setBoolean(9, applicarionKey.isEnabled());
            stmt.setString(10, applicarionKey.getEc2Region());
            stmt.execute();
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
			FingerprintDB.deleteFingerprint(fingerprintID);
        }
        DBUtils.closeConn(con);	
	}


    /**
     * return application keys base on sort oder defined
     * 
     * @param sortedSet object that defines sort oder
     * @return sort script list
     */
	public static SortedSet getApplicationKeySet(SortedSet sortedSet) {
		
		ArrayList<ApplicationKey> applicationKeysList = new ArrayList<ApplicationKey>();
		
		String orderBy = "";
		if(sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")){
			orderBy = " oder by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
		}
		String sql = "select appKey.id from application_key appKey where initialKey = true AND ec2_region LIKE 'NO_EC2_REGION'";
		
		// sql+= StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_ENABLED)) ? " and enabled=? " : "";
		sql=sql+orderBy;
		
		Connection con = null;
		try {
		
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement(sql);
			
//			int i=1;
//            //set filters in prepared statement
//            if(StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_ENABLED))){
//                stmt.setBoolean(i++, Boolean.valueOf(sortedSet.getFilterMap().get(FILTER_BY_ENABLED)));
//            }
			
            ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				applicationKeysList.add(getApplicationKeyByID(rs.getLong("id")));
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
        }
        DBUtils.closeConn(con);
		
        sortedSet.setItemList(applicationKeysList);
        return sortedSet;
	}

	
    /**
     * return application keys base on sort oder defined
     * 
     * @param sortedSet object that defines sort oder
     * @return sort script list
     */
	public static SortedSet getEC2KeySet(SortedSet sortedSet) {
		
		ArrayList<ApplicationKey> applicationKeysList = new ArrayList<ApplicationKey>();
		
		String orderBy = "";
		if(sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")){
			orderBy = " oder by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
		}
		String sql = "select appKey.id from application_key appKey where initialKey = true AND ec2_region NOT LIKE 'NO_EC2_REGION'";
		
		// sql+= StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_ENABLED)) ? " and enabled=? " : "";
		sql=sql+orderBy;
		
		Connection con = null;
		try {
		
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement(sql);
			
//			int i=1;
//            //set filters in prepared statement
//            if(StringUtils.isNotEmpty(sortedSet.getFilterMap().get(FILTER_BY_ENABLED))){
//                stmt.setBoolean(i++, Boolean.valueOf(sortedSet.getFilterMap().get(FILTER_BY_ENABLED)));
//            }
			
            ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				applicationKeysList.add(getApplicationKeyByID(rs.getLong("id")));
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
        }
        DBUtils.closeConn(con);
		
        sortedSet.setItemList(applicationKeysList);
        return sortedSet;
	}

	/**
	 * returns ApplicationKey base on applicationKeyID
	 * 
	 * @param applicationKeyID ApplicationKeyID
	 * @return ApplicationKey Object
	 */
	public static ApplicationKey getApplicationKeyByID(long applicationKeyID) {
		ApplicationKey applicationKey = new ApplicationKey();
		
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("Select * From application_key where id = ?");
			stmt.setLong(1, applicationKeyID);
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				applicationKey.setId(rs.getLong("id"));
				applicationKey.setKeyname(rs.getString("keyname"));
				applicationKey.setPublicKey(rs.getString("public_key"));
				applicationKey.setPrivateKey(EncryptionUtil.decrypt(rs.getString("private_key")));
				applicationKey.setPassphrase(EncryptionUtil.decrypt(rs.getString("passphrase")));
				applicationKey.setInitialkey(rs.getBoolean("initialKey"));
				Long userID = rs.getLong("user_id");
				if(userID == null || userID == 0){
					applicationKey.setUserId(null);
					applicationKey.setUsername("SYSTEM");
				}else{
					applicationKey.setUserId(userID);
					User user = UserDB.getUser(userID);
					applicationKey.setUsername(user.getUsername());
				}
				applicationKey.setType(rs.getString("type"));
				applicationKey.setFingerprint(FingerprintDB.getFingerprint(rs.getLong("fingerprint_id")));
				applicationKey.setEnabled(rs.getBoolean("enabled"));
				applicationKey.setCreateDt(rs.getTimestamp("create_dt"));
				applicationKey.setEc2Region(rs.getString("ec2_region"));
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
        }
        DBUtils.closeConn(con);		
		
		return applicationKey;
	}

	/**
	 * enable Application Key
	 * 
	 * @param id Application Key ID
	 */
	public static void enableApplicationKey(Long id) {
		disableEnabeleApplicationKey(id, true);
		
	}

	/**
	 * disable Application Key
	 * 
	 * @param id Application Key ID
	 */
	public static void disableApplicationKey(Long id) {
		disableEnabeleApplicationKey(id, false);
		
	}
	
	/**
	 * dis-/enable Application Key
	 * 
	 * @param id Application Key ID
	 * @param enable Application Key enabled
	 */
	private static void disableEnabeleApplicationKey(Long id, boolean enable) {
		Connection con = null;
		try{
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("update application_key set enabled=? where id=?");
            stmt.setBoolean(1, enable);
			stmt.setLong(2, id);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
	}


	/**
	 * returns active ApplicationKey for System
	 * 
	 * @param systemId SystemID
	 * @return <strong>active ApplicationKey</strong><br>
	 * or<br>
	 * <strong>null</strong>, if there is no ApplicationKey for the system is available
	 */
	public static ApplicationKey getApplicationKeyBySystemID(Long systemId) {
		ApplicationKey appKey = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select aks.* from application_key_system aks join application_key ak on aks.application_key_id = ak.id where aks.system_id = ? and aks.active = true");
            stmt.setLong(1, systemId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                appKey = getApplicationKeyByID(rs.getLong("application_key_id"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return appKey;
	}

	/**
	 * return enabled InitialApplication keys
	 * 
	 * @return ArrayList with InitialApplicationKey
	 */
	public static ArrayList<ApplicationKey> getInitialApplicationKey() {
		ArrayList<ApplicationKey> applicationKeysList = new ArrayList<ApplicationKey>();
		Connection con = null;
		try{
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("select id from application_key where initialKey = true and enabled = true AND ec2_region LIKE 'NO_EC2_REGION'");
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				applicationKeysList.add(getApplicationKeyByID(rs.getLong("id")));
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
        }
        DBUtils.closeConn(con);
		return applicationKeysList;
	}
	
	
	/**
	 * returns KeyBox InitialApplication key
	 * 
	 * @return KeyBox InitialApplication key
	 */
	public static ApplicationKey getKeyBoxsInitialApplicationKey() {
		ApplicationKey appKey = null;
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from application_key where keyname like 'KeyBoxInitialKey'");
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                appKey = getApplicationKeyByID(rs.getLong("id"));
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return appKey;
	}	
	
	
	/**
	 * Sets the ApplicationKey - System link
	 * 
	 * @param appKeyID ApplicationKeyID
	 * @param systemID SystemID
	 */
	public static void setActiveApplicationKeyforSystemID(Long appKeyID, Long systemID) {
		Connection con = null;
		try{
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("INSERT INTO APPLICATION_KEY_SYSTEM (SYSTEM_ID, APPLICATION_KEY_ID, ACTIVE) VALUES (?, ?,true)");
			stmt.setLong(1, systemID);
			stmt.setLong(2, appKeyID);
			stmt.execute();
            DBUtils.closeStmt(stmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
	}
	
	/**
	 * Update ApplicationKey - System link
	 * 
	 * @param appKeyID ApplicationKeyID
	 * @param systemID SystemID
	 */
	public static void updateActiveApplicationKeyforSystemID(Long appKeyID, Long systemID) {
		ApplicationKey testAppKey = getApplicationKeyBySystemID(systemID);
		if(testAppKey.getId()!=appKeyID){
			Connection con = null;
			try{
				con = DBUtils.getConn();
				PreparedStatement stmt = con.prepareStatement("UPDATE APPLICATION_KEY_SYSTEM SET active = ? where SYSTEM_ID = ?");
				stmt.setBoolean(1, false);
				stmt.setLong(2, systemID);
				stmt.execute();
	            DBUtils.closeStmt(stmt);
	            setActiveApplicationKeyforSystemID(appKeyID, systemID);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        DBUtils.closeConn(con);
		}
	}
	
	/**
	 * returns ApplicationKey base on Fingerprint 
	 * 
	 * @param fingerprint Fingerprint
	 * @return ApplicationKey Object
	 */
	public static ApplicationKey getApplicationKeyByFingerprint(String fingerprint) {
		ApplicationKey applicationKey = null;
		Connection con = null;
		try{
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("select appKey.id from application_key appKey JOIN fingerprint fp on appKey.fingerprint_id = fp.id  where fp.fingerprint = ?");
			stmt.setString(1, fingerprint);		
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				applicationKey = getApplicationKeyByID(rs.getLong("id"));
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return applicationKey;
	}

	/**
	 * Delete ApplicationKey with Fingerprint
	 * If ApplicationKey in use, the default value is set to the KeyBoxsInitialApplicationKey
	 * 
	 * @param appKeyID ApplicationKeyID
	 */
	public static void deleteApplicationKey(Long appKeyID) {
		Connection con = null;
		
		ApplicationKey defaultAppKey = getKeyBoxsInitialApplicationKey();
		changeApplicationKeyForSystems(appKeyID, defaultAppKey.getId());
		
		ApplicationKey appKey = getApplicationKeyByID(appKeyID);
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("DELETE FROM application_key WHERE id=?");
			stmt.setLong(1, appKeyID);
			stmt.execute();
			DBUtils.closeStmt(stmt);
			FingerprintDB.deleteFingerprint(appKey.getFingerprint().getId());
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		DBUtils.closeConn(con);
	}
	
	/**
	 * Change ApplicationKey for a lot of Systems
	 * 
	 * @param oldAppKeyID old ApplicationKeyID
	 * @param newAppKeyID new ApplicationKeyID
	 */
	private static void changeApplicationKeyForSystems(long oldAppKeyID, long newAppKeyID){
		Connection con = null;
		try{
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("SELECT SYSTEM_ID FROM APPLICATION_KEY_SYSTEM WHERE ACTIVE = true AND APPLICATION_KEY_ID = ?");
			
			stmt.setLong(1,	oldAppKeyID);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				updateActiveApplicationKeyforSystemID(newAppKeyID, rs.getLong("SYSTEM_ID"));
			}
			
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
        }
        DBUtils.closeConn(con);
	}


	/**
	 * return SystemName-List that use Applicationkey
	 *  
	 * @param appKeyID ApplicationkeyID
	 * @return SystemName-List
	 */
	public static ArrayList<String> getSystemNamesByApplicationId(Long appKeyID) {
		ArrayList<String> systemNames = new ArrayList<String>();
		Connection con = null;
		try{
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("SELECT syst.DISPLAY_NM FROM SYSTEM syst JOIN APPLICATION_KEY_SYSTEM aks ON syst.ID = aks.SYSTEM_ID WHERE aks.ACTIVE = true AND aks.APPLICATION_KEY_ID = ?");
			stmt.setLong(1,	appKeyID);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				systemNames.add(rs.getString("DISPLAY_NM"));
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
        }
        DBUtils.closeConn(con);
		return systemNames;
	}

	/**
	 * Test if Keyname exist in the Region
	 * 
	 * @param keyname Keyname
	 * @param ec2Region EC2Region
	 * @return
	 */
	public static boolean keyNameExistsInRegion(String keyname, String ec2Region) {
		boolean isexisted = false;
		Connection con = null;
		try{
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("select id from application_key where keyname = ? and ec2_region = ?");
			stmt.setString(1, keyname);
			stmt.setString(2, ec2Region);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				isexisted = true;
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
        }
        DBUtils.closeConn(con);
		return isexisted;
	}

	/**
     * returns the EC2 region
     *
     * @return region set
     */
	public static List<String> getEC2Regions() {
		List<String> ec2RegionList = new ArrayList<String>();

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select distinct ec2_region from APPLICATION_KEY");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	if(!rs.getString("ec2_region").equals("NO_EC2_REGION")){
            		ec2RegionList.add(rs.getString("ec2_region"));
            	}
            }
            DBUtils.closeStmt(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return ec2RegionList;
	}

	/**
     * returns private keys information for region
     *
     * @param ec2Region ec2 region
     * @return key information
     */
	public static List<ApplicationKey> getEC2KeyByRegion(String ec2Region) {
		List<ApplicationKey> ec2KeyList = new ArrayList<ApplicationKey>();

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from APPLICATION_KEY where ec2_region like ?");
            stmt.setString(1, ec2Region);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	ec2KeyList.add(getApplicationKeyByID(rs.getLong("id")));
            }
            DBUtils.closeStmt(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return ec2KeyList;
	}
	
	/**
     * returns private keys information for region and user
     * 
     * @param keyName 
     * @param ec2Region ec2 region
     * @return key information
     */
	public static ApplicationKey getEC2KeyByNmRegion(String keyName, String ec2Region) {
		ApplicationKey ec2Key=null;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from APPLICATION_KEY where KEYNAME like ? and ec2_region like ?");
            stmt.setString(1, keyName);
            stmt.setString(2, ec2Region);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ec2Key = getApplicationKeyByID(rs.getLong("id")); 
            }
            DBUtils.closeStmt(stmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return ec2Key;
	}


	/**
	 * Delete EC2 System Key with Fingerprint
	 * 
	 * @param ec2KeyID ApplicationKeyID
	 */
	public static void deleteEC2Key(Long ec2KeyID) {
		Connection con = null;
		
		SystemDB.deleteSystemByApplicationKeyID(ec2KeyID);
		
		ApplicationKey ec2Key = getApplicationKeyByID(ec2KeyID);
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("DELETE FROM application_key WHERE id=?");
			stmt.setLong(1, ec2KeyID);
			stmt.execute();
			DBUtils.closeStmt(stmt);
			FingerprintDB.deleteFingerprint(ec2Key.getFingerprint().getId());
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		DBUtils.closeConn(con);
	}
	
}
