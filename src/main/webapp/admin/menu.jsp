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


    <style type="text/css">
        .vborder td {
            white-space: normal;
        }
    </style>


</head>


<body style="background: #FFFFFF">
<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <table class="vborder">
            <thead>
            <tr>
                <th colspan="2">Main Menu</th>
            </tr>
            </thead>

            <tbody>

            <s:if test="%{#session.userType==\"M\"}">
            <tr>

                <td>
                    <a href="../manage/viewSystems.action">Systems</a>

                </td>


                <td>
                    Manage systems so that access may be granted and SSH sessions established
                </td>

            </tr>
            <tr>
               <td>
                    <a href="../manage/viewProfiles.action">Profiles</a>
               </td>
               <td>
                    Create profiles and assign systems so that users will be granted access when the authorized key file is distributed to systems.
               </td>
            </tr>
            <tr>
                <td>
                    <a href="../manage/viewUsers.action">Users</a>

                </td>
                <td>
                    Manage user accounts and public keys. Assign profiles so that users will be granted access when the authorized key
                    file is distributed to systems.
                </td>
            </tr>
            <tr>
                <td>
                    <a href="../manage/viewKeys.action">Public SSH Keys</a>
                </td>
                <td>
                    Set additional public SSH keys for systems
                </td>
            </tr>

                <tr>
                    <td>
                        <a href="../manage/distributeKeysByProfile.action">Distribute SSH Keys</a>
                    </td>
                    <td>
                        Distribute public SSH keys for systems
                    </td>
                </tr>

            </s:if>


             <tr>
                <td>
                    <a href="../admin/viewSystems.action">Composite SSH Terms</a>
                </td>
                <td>
                Execute multiple-simultaneous web-terminals on selected systems.
                </td>
             </tr>
             <tr>
                <td>
                    <a href="../admin/viewScripts.action">Composite Scripts</a>
                </td>
                <td>
                    Create scripts to be executed on selected systems simultaneously through a web-terminal
                </td>
             </tr>
             <s:if test="%{#session.userType==\"M\"}">
             <tr>
                <td>
                    <a href="../manage/viewSessions.action">Audit Sessions</a>
                </td>
                <td>
                    Audit administrator's sessions and terminal history
                </td>
            </tr>
            </s:if>
            <tr>
                <td>
                  <a href="../admin/setPassword.action">Change Password</a>
                </td>
                <td>
                    Change administrative login to application
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>