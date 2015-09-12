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


<div class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">

        <div class="navbar-header">
            <div class="navbar-brand">
                <div class="nav-img"><img src="<%= request.getContextPath() %>/img/keybox_40x40.png" alt="keybox"/></div>
                KeyBox
            </div>
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                     <span class="sr-only">Toggle navigation</span>
                     <span class="icon-bar"></span>
                     <span class="icon-bar"></span>
                     <span class="icon-bar"></span>
            </button>
        </div>
        <div class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="../admin/menu.action">Home</a></li>


                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Manage <b class="caret"></b></a>
                        <ul class="dropdown-menu">
                            <s:if test="%{#session.userType==\"M\"}">


                            <li><a href="../manage/viewSystems.action">Systems</a></li>


                            <li><a href="../manage/viewProfiles.action">Profiles</a></li>


                            <li><a href="../manage/viewUsers.action">Users</a></li>

                            </s:if>
                            <s:if test="%{@com.keybox.manage.util.SSHUtil@keyManagementEnabled}">
                                <s:if test="%{#session.userType==\"M\"}">
                                    <li><a href="../manage/viewKeys.action">Manage SSH Keys</a></li>
                                </s:if>
                                <s:else>
                                    <li><a href="../admin/viewKeys.action">Manage SSH Keys</a></li>
                                </s:else>
                            </s:if>
                        </ul>
                    </li>


                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Secure Shell <b class="caret"></b></a>
                    <ul class="dropdown-menu">

                        <li><a href="../admin/viewSystems.action">Composite SSH Terms</a></li>
                        <li><a href="../admin/viewScripts.action">Composite Scripts</a></li>


                    </ul>
                </li>


                <s:if test="%{@com.keybox.manage.util.SessionOutputUtil@enableInternalAudit && #session.userType==\"M\"}">

                    <li><a href="../manage/viewSessions.action">Audit Sessions</a></li>

                </s:if>

                <li><a href="../admin/userSettings.action">Settings</a></li>
                
                <li><a href="../logout.action">Logout</a></li>
            </ul>
        </div>
        <!--/.nav-collapse -->
    </div>
</div>

