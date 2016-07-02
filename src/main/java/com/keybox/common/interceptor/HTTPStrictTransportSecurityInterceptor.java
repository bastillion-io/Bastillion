/**
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
package com.keybox.common.interceptor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.struts2.StrutsStatics;

import javax.servlet.http.HttpServletResponse;

public class HTTPStrictTransportSecurityInterceptor extends AbstractInterceptor {

	/**
	 * HTTP Strict Transport Security (HSTS) is an opt-in security enhancement
	 * that is specified by a web application through the use of a special
	 * response header. Once a supported browser receives this header that
	 * browser will prevent any communications from being sent over HTTP to the
	 * specified domain and will instead send all communications over HTTPS. It
	 * also prevents HTTPS click through prompts on browsers.
	 * https://www.owasp.org/index.php/HTTP_Strict_Transport_Security
	 */

	private static final long serialVersionUID = 6937154325400922939L;
	private static final String HEADER = "Strict-Transport-Security";
	private static final String MAX_AGE = "max-age=";
	private static final int ONE_YEAR = 31536000;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionContext context = invocation.getInvocationContext();
		HttpServletResponse response = (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);
		String headerValue = MAX_AGE + ONE_YEAR;
		response.addHeader(HEADER, headerValue);
		return invocation.invoke();
	}

}
