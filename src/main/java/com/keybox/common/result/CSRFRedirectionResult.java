/**
 * Copyright 2016 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.common.result;

import com.keybox.common.util.AuthUtil;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.result.ServletRedirectResult;

/**
 * Override redirect and add CSRF token
 */
public class CSRFRedirectionResult extends ServletRedirectResult {

    @Override
    protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        String token = AuthUtil.getCSRFToken(ServletActionContext.getRequest().getSession());
        finalLocation = finalLocation.contains("?") ? finalLocation + "&" : finalLocation + "?";
        finalLocation = finalLocation + AuthUtil.CSRF_TOKEN_NM + "=" + token;
        setLocation(finalLocation);
        super.doExecute(this.getLocation(), invocation);
    }
}
