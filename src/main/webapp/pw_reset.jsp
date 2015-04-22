<%
/**
 * Copyright 2015 Robert Vorkoeper - robert-vor@gmx.de
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

            $("#pwrest_btn").button().click(function() {
                $('#pwResetSubmit').submit();
            });
        });
		
    </script>
    <title>KeyBox - PW Reset </title>
</head>
<body>

    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
        <div class="container" >

            <div class="navbar-header">
                <div class="navbar-brand" >
                    <div class="nav-img"><img src="<%= request.getContextPath() %>/img/keybox_50x38.png" alt="keybox"/></div>
                 KeyBox</div>
            </div>
            <!--/.nav-collapse -->
        </div>
    </div>

	
	
    <div class="container">
        <p>
        <s:actionmessage/>
        <s:actionerror/>
        <s:form action="pwResetSubmit"  autocomplete="off">
        	<s:textfield name="email" label="E-Mail"/>
			<tr> <td>&nbsp;</td>
        		<td align="right">  <div id="pwrest_btn" class="btn btn-default" >Reset password</div></td>
	    	</tr>
	    	<tr> <td>&nbsp;</td>
	    	<td align="right">  <a href="/login.action">Back to Login</a></td>
	    	</tr>
        </s:form>
        </p>



    </div>

</body>
</html>
