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

            $(".submit_btn").button().click(function() {
                $(this).closest('form').submit();
            });
        });

    </script>
    <style>
        form table {
            width:350px;
        }
    </style>

    <title>KeyBox - Set Admin Password</title>
</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
    <s:if test="%{!@com.keybox.manage.util.ExternalAuthUtil@externalAuthEnabled || #session.authType==\"BASIC\"}">

        <h3>Set Admin Password</h3>
        <p>Change your administrative password below</p>

        <s:actionerror/>
        <s:form action="passwordSubmit" autocomplete="off">
            <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
            <s:password name="auth.prevPassword" label="Current Password" />
            <s:password name="auth.password" label="New Password" />
            <s:password name="auth.passwordConfirm" label="Confirm New Password" />
            <tr> <td>&nbsp;</td>
                <td align="right">  <div id="change_btn" class="btn btn-default submit_btn" >Change Password</div></td>
            </tr>
        </s:form>
    </s:if>



        <h3>Set Terminal Theme</h3>

        <p>Change the theme for your terminals below</p>
        <s:form action="themeSubmit">
            <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
            <s:select name="userSettings.theme"
                      list="#{'#2e3436,#cc0000,#4e9a06,#c4a000,#3465a4,#75507b,#06989a,#d3d7cf,#555753,#ef2929,#8ae234,#fce94f,#729fcf,#ad7fa8,#34e2e2,#eeeeec':'Tango',
                              '#000000,#cd0000,#00cd00,#cdcd00,#0000ee,#cd00cd,#00cdcd,#e5e5e5,#7f7f7f,#ff0000,#00ff00,#ffff00,#5c5cff,#ff00ff,#00ffff,#ffffff':'XTerm'}" 
                      label="Terminal Theme" headerKey="" headerValue="- Select Theme -"/>
            
            <s:select name="userSettings.plane"
                      list="#{'#FFFFDD,#000000':'Black on light yellow',
                              '#FFFFFF,#000000':'Black on white',
                              '#000000,#AAAAAA':'Gray on black',
                              '#000000,#00FF00':'Green on black',
                              '#000000,#FFFFFF':'White on black'
                              }" label="Foreground / Background" headerKey=""
                      headerValue="- Select FG / BG -"/>


            

            <tr> <td>&nbsp;</td>
                <td align="right">  <div id="theme_btn" class="btn btn-default submit_btn" >Update Theme</div></td>
            </tr>
        </s:form>

    </div>



</body>
</html>
