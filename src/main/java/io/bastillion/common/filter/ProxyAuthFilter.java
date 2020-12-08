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
package io.bastillion.common.filter;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bastillion.common.util.AppConfig;
import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.control.LoginKtrl;
import io.bastillion.manage.db.AuthDB;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.User;
import loophole.mvc.filter.CSRFFilter;

/**
 * @author geoffroya (https://github.com/geoffroya)
 * 
 *         Filter determines if user is connected Only applies when
 *         authenticationMode=proxy
 * 
 *         If REMOTE_USER http header is present, the user is supposed to be
 *         authenticated from the reverse proxy, in front of Bastillion.
 * 
 *         This Filter looks for the user in user database, based on username.
 */
public class ProxyAuthFilter implements Filter {

	private static Logger log = LoggerFactory.getLogger(ProxyAuthFilter.class);
	private static Logger loginAuditLogger = LoggerFactory.getLogger("io.bastillion.manage.control.LoginAudit");

	private static final String HTTP_HEADER_REMOTE_USER = "REMOTE_USER";

	private static final SecureRandom random = new SecureRandom();

	public void init(FilterConfig config) throws ServletException {

	}

	public void destroy() {
	}

	/**
	 * doFilter determines if user logged or not in the front Proxy
	 *
	 * @param req   task request
	 * @param resp  task response
	 * @param chain filter chain
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws ServletException, IOException {

		HttpServletRequest servletRequest = (HttpServletRequest) req;
		HttpServletResponse servletResponse = (HttpServletResponse) resp;

		if (StringUtils.equals(AppConfig.getProperty("authenticationMode"), Auth.AUTHENTICATION_MODE_PROXY)) {

			String remoteUser = servletRequest.getHeader(HTTP_HEADER_REMOTE_USER);

			if (null != remoteUser) {
				log.debug("Found header {} with value {}", HTTP_HEADER_REMOTE_USER, remoteUser);

				// Check if user is logged

				if (!isConnected(servletRequest.getSession())) {

					// Try to log with username
					Auth auth = new Auth();
					auth.setUsername(remoteUser);
					auth.setAuthType(Auth.AUTH_PROXY);

					String authToken = AuthDB.login(auth);
					String clientIP = AuthUtil.getClientIPAddress(servletRequest);
					if (authToken != null) {
						User user = AuthDB.getUserByAuthToken(authToken);

						// check to see if account has expired
						if (user.isExpired()) {
							String msg = auth.getUsername() + " (" + clientIP + ") - "
									+ LoginKtrl.AUTH_ERROR_EXPIRED_ACCOUNT;
							loginAuditLogger.info(msg);
							servletResponse.sendError(HttpStatus.UNAUTHORIZED_401, msg);
							return;
						}

						AuthUtil.setAuthToken(servletRequest.getSession(), authToken);
						AuthUtil.setUserId(servletRequest.getSession(), user.getId());
						AuthUtil.setAuthType(servletRequest.getSession(), user.getAuthType());
						AuthUtil.setTimeout(servletRequest.getSession());
						AuthUtil.setUsername(servletRequest.getSession(), user.getUsername());

						AuthDB.updateLastLogin(user);

						loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - Authentication Success");

						// Create CRSF token
						String _csrf = (new BigInteger(165, random)).toString(36).toUpperCase();
						servletRequest.getSession().setAttribute(CSRFFilter._CSRF, _csrf);

						servletResponse
								.sendRedirect(servletRequest.getContextPath() + "/admin/menu.html?_csrf=" + _csrf);
						return;
					} else {
						String msg = auth.getUsername() + " (" + clientIP + ") - " + LoginKtrl.AUTH_ERROR;
						loginAuditLogger.info(msg);
						servletResponse.sendError(HttpStatus.UNAUTHORIZED_401, msg);
						return;
					}
				}

			}
		}
		chain.doFilter(req, resp);

	}

	private boolean isConnected(HttpSession session) {
		// read auth token
		String authToken = AuthUtil.getAuthToken(session);

		if (authToken != null && !authToken.trim().equals("")) {
			// check if valid admin auth token

			String userType = AuthDB.isAuthorized(AuthUtil.getUserId(session), authToken);
			if (userType != null) {

				AuthUtil.setUserType(session, userType);

				// check to see if user has timed out
				String timeStr = AuthUtil.getTimeout(session);
				try {
					if (timeStr != null && !timeStr.trim().equals("")) {
						SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss");
						Date sessionTimeout = sdf.parse(timeStr);
						Date currentTime = new Date();

						// if current time > timeout then redirect to login page
						if (sessionTimeout == null || currentTime.after(sessionTimeout)) {
							return false;
						} else {
							AuthUtil.setTimeout(session);
							return true;
						}
					} else {
						return false;
					}

				} catch (Exception ex) {
					log.error(ex.toString(), ex);
					return false;
				}
			}

		}
		return false;
	}

}
