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
package io.bastillion.common.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.util.DBUtils;
import io.bastillion.manage.util.EncryptionUtil;
import io.bastillion.manage.util.RefreshAuthKeyUtil;
import io.bastillion.manage.util.SSHUtil;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Initial startup task. Creates an SQLite DB and generates the system
 * public/private key pair if none exists
 */
@WebServlet(name = "DBInitServlet", urlPatterns = { "/config" }, loadOnStartup = 1)
public class DBInitServlet extends javax.servlet.http.HttpServlet {

	private static Logger log = LoggerFactory.getLogger(DBInitServlet.class);

	/**
	 * task init method that created DB and generated public/private keys
	 *
	 * @param config task config
	 */
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		Connection connection = null;
		Statement statement = null;
		// check if reset ssh application key is set
		boolean resetSSHKey = "true".equals(AppConfig.getProperty("resetApplicationSSHKey"));

		// if DB password is empty generate a random
		if (StringUtils.isEmpty(AppConfig.getProperty("dbPassword"))) {
			String dbPassword = null;
			String dbPasswordConfirm = null;
			if (!"true".equals(System.getProperty("GEN_DB_PASS"))) {
				// prompt for password and confirmation
				while (dbPassword == null || !dbPassword.equals(dbPasswordConfirm)) {
					if (System.console() == null) {
						Scanner in = new Scanner(System.in);
						System.out.println("Please enter database password: ");
						dbPassword = in.nextLine();
						System.out.println("Please confirm database password: ");
						dbPasswordConfirm = in.nextLine();
					} else {
						dbPassword = new String(System.console().readPassword("Please enter database password: "));
						dbPasswordConfirm = new String(
								System.console().readPassword("Please confirm database password: "));
					}
					if (!dbPassword.equals(dbPasswordConfirm)) {
						System.out.println("Passwords do not match");
					}
				}
			}
			// set password
			if (StringUtils.isNotEmpty(dbPassword)) {
				AppConfig.encryptProperty("dbPassword", dbPassword);
				// if password not set generate a random
			} else {
				System.out.println("Generating random database password");
				AppConfig.encryptProperty("dbPassword", RandomStringUtils.random(32, true, true));
			}
			// else encrypt password if plain-text
		} else if (!AppConfig.isPropertyEncrypted("dbPassword")) {
			AppConfig.encryptProperty("dbPassword", AppConfig.getProperty("dbPassword"));
		}

		try {
			connection = DBUtils.getConn();
			statement = connection.createStatement();

			boolean databaseExists = false;

			ResultSet rs = null;
			try {
				rs = statement.executeQuery("select * from users");
				databaseExists = true;
			} finally {
				DBUtils.closeRs(rs);
			}

			// Automatically create or update database
			if (Boolean.valueOf(AppConfig.getProperty("dbCreate"))) {

				log.info("Creating database with Liquibase");

				// Create DB objects with liquibase
				Database database = DatabaseFactory.getInstance()
						.findCorrectDatabaseImplementation(new JdbcConnection(connection));

				try (Liquibase liquibase = new liquibase.Liquibase("config/liquibase/master.xml",
						new ClassLoaderResourceAccessor(), database)) {
					liquibase.update(new Contexts(), new LabelExpression());
				} catch (Exception e) {
					log.error("Error creating/updating database", e);
					System.exit(1);
				}

			}

			// Reset SSH Key and create admin if database didn't exist
			if (!databaseExists) {

				resetSSHKey = true;

				// if exists readfile to set default password
				String salt = EncryptionUtil.generateSalt();
				String defaultPassword = EncryptionUtil.hash("changeme" + salt);

				// set password if running in EC2
				File file = new File("/opt/bastillion/instance_id");
				if (file.exists()) {
					String str = FileUtils.readFileToString(file, "UTF-8");
					if (StringUtils.isNotEmpty(str)) {
						defaultPassword = EncryptionUtil.hash(str.trim() + salt);
					}
				}

				// insert default admin user
				if (connection == null || connection.isClosed()) {
					connection = DBUtils.getConn();
				}
				PreparedStatement pStmt = connection
						.prepareStatement("insert into users (username, password, user_type, salt) values(?,?,?,?)");
				pStmt.setString(1, "admin");
				pStmt.setString(2, defaultPassword);
				pStmt.setString(3, Auth.MANAGER);
				pStmt.setString(4, salt);
				pStmt.execute();
				DBUtils.closeStmt(pStmt);

			}

			// if reset ssh application key then generate new key
			if (resetSSHKey) {

				// delete old key entry
				PreparedStatement pStmt = connection.prepareStatement("delete from application_key");
				pStmt.execute();
				DBUtils.closeStmt(pStmt);

				// generate new key and insert passphrase
				System.out.println("Setting Bastillion SSH public/private key pair");

				// generate application pub/pvt key and get values
				String passphrase = SSHUtil.keyGen();
				String publicKey = SSHUtil.getPublicKey();
				String privateKey = SSHUtil.getPrivateKey();

				// insert new keys
				pStmt = connection.prepareStatement(
						"insert into application_key (public_key, private_key, passphrase) values(?,?,?)");
				pStmt.setString(1, publicKey);
				pStmt.setString(2, EncryptionUtil.encrypt(privateKey));
				pStmt.setString(3, EncryptionUtil.encrypt(passphrase));
				pStmt.execute();
				DBUtils.closeStmt(pStmt);

				System.out.println("Bastillion Generated Global Public Key:");
				System.out.println(publicKey);

				// set config to default
				AppConfig.updateProperty("publicKey", "");
				AppConfig.updateProperty("privateKey", "");
				AppConfig.updateProperty("defaultSSHPassphrase", "${randomPassphrase}");

				// set to false
				AppConfig.updateProperty("resetApplicationSSHKey", "false");

			}

			// delete ssh keys
			SSHUtil.deletePvtGenSSHKey();

		} catch (Exception ex) {
			log.error(ex.toString(), ex);
		} finally {
			DBUtils.closeStmt(statement);
			DBUtils.closeConn(connection);
		}

		RefreshAuthKeyUtil.startRefreshAllSystemsTimerTask();
	}

}
