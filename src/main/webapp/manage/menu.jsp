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

            <tr>

                <td>
                    <a href="viewSystems.action">Manage Systems</a>

                </td>


                <td>
                    Manage systems so that access may be granted and SSH sessions established
                </td>

            </tr>
            <tr>
                <td>
                    <a href="viewUsers.action">Manage Users</a>

                </td>
                <td>
                    Manage user accounts and public keys. Assign profiles so that users will be granted access when the authorized key
                    file is distributed to systems.
                </td>
            </tr>
            <tr>
                <td>
                    <a href="viewProfiles.action">Manage Profiles</a>

                </td>
                <td>
                    Create profiles and assign systems so that users will be granted access when the authorized key file is distributed
                    to systems.
                </td>
            </tr>


            <tr>
                <td>
                    <a href="viewSystems.action?selectForm=true">Distribute SSH Keys</a>

                </td>
                <td>
                    Distribute the authorized key file to systems that have been defined. The authorized key file will
                    contain entries based on the systems assigned to the user's profile.
                </td>
            </tr>

            <tr>
                <td>
                    <a href="viewScripts.action">Composite Scripts</a>

                </td>
                <td>
                    Create scripts to be executed on selected systems simultaneously through a web-terminal
                </td>
            </tr>
            <tr>
                <td>
                    <a href="viewSystems.action?selectForm=true&terms=true">Composite SSH Terms</a>

                </td>
                <td>
                    Execute multiple-simultaneous web-terminals on selected systems.
                </td>
            </tr>

            <tr>
                <td>
                    <a href="setPassphrase.action">Change Passphrase</a>

                </td>
                <td>
                    Generate system key with a custom passphrase to access defined systems. This enables added security
                    and you will be prompted for a passphrase each time you access a system or systems. You will have to re-distribute the authorized key file after setting a passphrase.
                </td>
            </tr>
              <tr>
                <td>
                  <a href="setPassword.action">Change Password</a>
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