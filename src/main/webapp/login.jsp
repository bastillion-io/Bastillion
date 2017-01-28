<%
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
%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>

    <jsp:include page="_res/inc/header.jsp"/>

    <script type="text/javascript">
        //break if loaded in frame
        if(top != self) top.location.replace(location);

        $(document).ready(function() {

            $("#login_btn").button().click(function() {
                $('#loginSubmit').submit();
            });
        });
		
    </script>
    <title>KeyBox - Login </title>
</head>
<body>

    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
        <div class="container" >

            <div class="navbar-header">
                <div class="navbar-brand" >
                    <div class="nav-img"><img src="<%= request.getContextPath() %>/img/keybox_40x40.png" alt="keybox"/></div>
                 KeyBox</div>
            </div>
            <!--/.nav-collapse -->
        </div>
    </div>

    <div class="container">
        <p>
        <s:actionerror/>
        <s:form action="loginSubmit"  autocomplete="off">
            <s:if test="%{#session['_csrf']}">
                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
            </s:if>
            <s:else>
                <s:hidden name="_csrf"/>
            </s:else>
            <s:textfield name="auth.username" label="Username"/>
            <s:password name="auth.password" label="Password" value="" />
            <s:if test="otpEnabled">
                <s:textfield name="auth.otpToken" label="OTP Access Code"  autocomplete="off" value=""/>
            </s:if>
            <tr> <td>&nbsp;</td>
                <td align="right">  <div id="login_btn" class="btn btn-default login" >Login</div></td>
            </tr>
        </s:form>
        </p>



    </div>

</body>
</html>
