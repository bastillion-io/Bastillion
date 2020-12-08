/**
 *    Copyright (C) 2020 GIP RENATER
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
package io.bastillion.manage.util;

import java.sql.Connection;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.User;

/**
 * @author geoffroya (https://github.co/geoffroya)
 * 
 * External authentication utility for reverse-proxy based auth.
 */
public class ProxyAuthUtil {

	private static Logger log = LoggerFactory.getLogger(ProxyAuthUtil.class);

	public static final boolean proxyAuthEnabled = StringUtils.equals(AppConfig.getProperty("authenticationMode"),
			Auth.AUTHENTICATION_MODE_PROXY);

	private ProxyAuthUtil() {

	}

	/**
	 * proxy auth login method
	 *
	 * @return auth token if success
	 * @auth authentication credentials
	 */
	public static String login(final Auth auth) {
		
		String authToken = null;

		if (proxyAuthEnabled && auth != null && StringUtils.isNotEmpty(auth.getUsername())) {
			Connection con = null;
			try {
				con = DBUtils.getConn();

				User user = AuthDB.getUserByUID(con, auth.getUsername());

				if (user != null) {
					auth.setId(user.getId());
					authToken = UUID.randomUUID().toString();
					auth.setAuthToken(authToken);
					auth.setAuthType(Auth.AUTH_PROXY);
					AuthDB.updateLogin(con, auth);
				}

			} catch (Exception e) {
				authToken = null;
				log.error(e.toString(), e);
			} finally {
				DBUtils.closeConn(con);
			}
		}
		return authToken;
	}

}