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

public class ClickjackingInterceptor extends AbstractInterceptor {

	/**
	 * Clickjacking, also known as a "UI redress attack", is when an attacker
	 * uses multiple transparent or opaque layers to trick a user into clicking
	 * on a button or link on another page when they were intending to click on
	 * the the top level page. Thus, the attacker is "hijacking" clicks meant
	 * for their page and routing them to another page, most likely owned by
	 * another application, domain, or both.
	 * https://www.owasp.org/index.php/Clickjacking
	 */

	private static final long serialVersionUID = 2438421386123540997L;
	private static final String HEADER = "X-Frame-Options";
	private static final String VALUE = "SAMEORIGIN";
	
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		ActionContext context = invocation.getInvocationContext();
		HttpServletResponse response = (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);
		String headerValue = VALUE;
		response.addHeader(HEADER, headerValue);
		return invocation.invoke();
	}
}
