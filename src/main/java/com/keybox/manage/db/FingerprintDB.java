/**
 * Copyright 2015 Robert Vorköper - robert-vor@gmx.de
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.keybox.manage.model.Fingerprint;
import com.keybox.manage.util.DBUtils;


/**
 * DAO that returns Fingerprint for public key
 */
public class FingerprintDB {

	/**
	 * Checks if Fingerprint registered
	 * 
	 * @param fingerprint
	 * @return <strong>true</strong>, if Fingerprint exists <br>
	 * 			<strong>false</strong>, if Fingerprint not exists
	 */
	public static boolean isFingerprintExists(String fingerprint) {
		boolean isexisted = false;
		PreparedStatement stmt;
        Connection con = null;
        try{
        	con = DBUtils.getConn();
            stmt = con.prepareStatement("select * from fingerprint where fingerprint like ?");
            stmt.setString(1, fingerprint);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
    			isexisted = true;
            }
            DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        DBUtils.closeConn(con);
		
		return isexisted;
	}
	
	/**
	 * Insert new fingerprint
	 * 
	 * @param fingerprint Fingerprint
	 * @return <strong>fingerprintID</strong><br>or<br> <strong>0</strong>, if not successfully insert
	 */
	public static long insertFingerprint(Fingerprint fingerprint) {
		Connection con = null;
        long fingerprintID = 0;
        try{
        	con = DBUtils.getConn();
        	PreparedStatement stmt_kf;
        	
        	stmt_kf = con.prepareStatement("insert into fingerprint(fingerprint) values (?)", Statement.RETURN_GENERATED_KEYS);
            stmt_kf.setString(1, fingerprint.getFingerprint());
            stmt_kf.execute();
            
            ResultSet tableKeys = stmt_kf.getGeneratedKeys();
            tableKeys.next();
            fingerprintID = tableKeys.getLong(1);
            DBUtils.closeStmt(stmt_kf);
        } catch (SQLException ex) {
			ex.printStackTrace();
		}	
        DBUtils.closeConn(con);
        return fingerprintID;
	}
	
	/**
	 * deletes fingerprint for rollback
	 * 
	 * @param fingerprintID fingerprintID
	 * @return <strong>true</strong>, if Fingerprint delete<br>
	 * 			<strong>false</strong>, if Fingerprint in use or SQLException
	 */
	public static boolean deleteFingerprint(Long fingerprintID) {
		boolean successfully = true;
		Connection con = null;
		if(!inuseFingerprint(fingerprintID)){
			try {
				con = DBUtils.getConn();
				PreparedStatement stmt = con.prepareStatement("delete from fingerprint where id=?");
				stmt.setLong(1, fingerprintID);
				stmt.execute();
				DBUtils.closeStmt(stmt);
			} catch (SQLException e) {
				e.printStackTrace();
				successfully = false;
			}
		} else {
			successfully = false;
		}
		DBUtils.closeConn(con);
		return successfully;
	}
	
	/**
	 * Test is fingerprint in using
	 * 
	 * @param fingerprintID FingerprintID
	 * @return <strong>true</strong>, if Fingerprint in using<br>
	 * 			<strong>false</strong>, if Fingerprint in not using
	 */
	public static boolean inuseFingerprint(long fingerprintID) {
		boolean inUsing = false;
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt_pub = con.prepareStatement("select * from public_keys where fingerprint_id = ?");
			stmt_pub.setLong(1, fingerprintID);
            ResultSet rs_pub = stmt_pub.executeQuery();
            if (rs_pub.next()) {
            	inUsing = true;
            }
            DBUtils.closeRs(rs_pub);
            DBUtils.closeStmt(stmt_pub);
            
            PreparedStatement stmt_pvd = con.prepareStatement("select * from application_key where fingerprint_id = ?");
            stmt_pvd.setLong(1, fingerprintID);
            ResultSet rs_pvd = stmt_pvd.executeQuery();
            if (rs_pvd.next()) {
            	inUsing = true;
            }
            DBUtils.closeRs(rs_pvd);
            DBUtils.closeStmt(stmt_pvd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return inUsing;
	}

	/**
	 * returns Fingerprint base on fingerprintID
	 * 
	 * @param fingerprintID FingerprintID
	 * @return Fingerprint Object
	 */
	public static Fingerprint getFingerprint(Long fingerprintID) {
		Fingerprint fingerprint = null;
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("select * from fingerprint where id = ?");
			stmt.setLong(1, fingerprintID);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				fingerprint = new Fingerprint(rs.getString("fingerprint"));
				fingerprint.setId(rs.getLong("id"));
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return fingerprint;
	}
	
	/**
	 * returns Fingerprint base on fingerprint
	 * 
	 * @param fingerprintString Fingerprint String
	 * @return Fingerprint Object
	 */
	public static Fingerprint getFingerprint(String fingerprintString) {
		Fingerprint fingerprint = null;
		Connection con = null;
		try {
			con = DBUtils.getConn();
			PreparedStatement stmt = con.prepareStatement("select * from fingerprint where fingerprint = ?");
			stmt.setString(1, fingerprintString);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				fingerprint = new Fingerprint(rs.getString("fingerprint"));
				fingerprint.setId(rs.getLong("id"));
			}
			DBUtils.closeRs(rs);
            DBUtils.closeStmt(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DBUtils.closeConn(con);
		return fingerprint;
	}
}
