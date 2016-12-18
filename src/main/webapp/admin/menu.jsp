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


    <title>KeyBox - Main Menu</title>

    <script type="text/javascript">
        $(document).ready(function() {


            $("table").css("background-color", "#ffffff");
            $("table tr:even").css("background-color", "#e0e0e0");

        });
    </script>





</head>


<body >

    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        <div class="template">


        <table class="table-striped">
            <thead>
            <tr>
                <th colspan="2">Main Menu</th>
            </tr>
            </thead>

            <tbody>

            <s:if test="%{#session.userType==\"M\"}">
            <tr>

                <td>
                    <a  href="../manage/viewSystems.action?_csrf=<s:property value="#session['_csrf']"/>">Systems</a>

                </td>


                <td>
                    Manage systems so that access may be granted for establishing SSH sessions.
                </td>

            </tr>
            <tr>
               <td>
                    <a href="../manage/viewProfiles.action?_csrf=<s:property value="#session['_csrf']"/>">Profiles</a>
               </td>
               <td>
                    Create profiles and assign systems to grant user access and distribute keys.
               </td>
            </tr>
            <tr>
                <td>
                    <a href="../manage/viewUsers.action?_csrf=<s:property value="#session['_csrf']"/>">Users</a>

                </td>
                <td>
                    Manage user accounts and profiles. Profiles grants a user access to the defined systems.
                </td>
            </tr>
            </s:if>


             <tr>
                <td>
                    <a href="../admin/viewSystems.action?_csrf=<s:property value="#session['_csrf']"/>">Composite SSH Terms</a>
                </td>
                <td>
                Execute multiple-simultaneous web-terminals on selected systems.
                </td>
             </tr>
             <tr>
                <td>
                    <a href="../admin/viewScripts.action?_csrf=<s:property value="#session['_csrf']"/>">Composite Scripts</a>
                </td>
                <td>
                    Create scripts to be executed on selected systems simultaneously through a web-terminal.
                </td>
             </tr>
            <s:if test="%{@com.keybox.manage.util.SSHUtil@keyManagementEnabled}">
            <tr>
                <s:if test="%{#session.userType==\"M\"}">
                <td>
                    <a href="../manage/viewKeys.action?_csrf=<s:property value="#session['_csrf']"/>">Manage SSH Keys</a>
                </td>
                 <td>
                     Disable any administrative key forcing rotation. Set additional public SSH keys for current user. 
                 </td>
                </s:if>
                <s:else>
                    <td>
                        <a href="../admin/viewKeys.action?_csrf=<s:property value="#session['_csrf']"/>">Manage SSH Keys</a>
                    </td>
                    <td>
                        Set additional public SSH keys for current user.
                    </td>
                </s:else>
            </tr>
            </s:if>
            <s:if test="%{@com.keybox.manage.util.SessionOutputUtil@enableInternalAudit && #session.userType==\"M\"}">
             <tr>
                <td>
                    <a href="../manage/viewSessions.action?_csrf=<s:property value="#session['_csrf']"/>">Audit Sessions</a>
                </td>
                <td>
                    Audit administrator's sessions and terminal history.
                </td>
            </tr>
            </s:if>
             <tr>
                <td>
                  <a href="../admin/userSettings.action?_csrf=<s:property value="#session['_csrf']"/>">Settings</a>
                </td>
                <td>Administrative settings</td>
            </tr>
            </tbody>
        </table>
    </div>
        </div>
</body>
</html>