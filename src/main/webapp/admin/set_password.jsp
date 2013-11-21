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

    <jsp:include page="../_res/inc/header.jsp"/>

       <script type="text/javascript">
        $(document).ready(function() {


            $("#change_pass_btn").button().click(function() {
                $('#passwordSubmit').submit();
            });
        });

    </script>

    <title>KeyBox - Set Admin Password</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <h3>Set Admin Password</h3>
        <p>Change your administrative password below</p>

        <s:actionerror/>
        <s:form action="passwordSubmit" autocomplete="off">
            <s:password name="auth.prevPassword" label="Current Password" />
            <s:password name="auth.password" label="New Password" />
            <s:password name="auth.passwordConfirm" label="Confirm New Password" />
            <tr> <td>&nbsp;</td>
                <td align="right">  <div id="change_pass_btn" class="login" >Change Password</div></td>
            </tr>
        </s:form>

    </div>


</div>

</body>
</html>
