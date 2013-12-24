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
<div class="nav">

    <div style="float: left;margin-top: 5px;margin-left: -10px"><img
            src="<%= request.getContextPath() %>/img/keybox_50x38.png"/></div>

    <h3>
        <a href="../admin/menu.action">KeyBox</a>
    </h3>
    <s:if test="%{#session.userType==\"M\"}">
        <div class="nav_item">
            <a href="../manage/viewSystems.action">Systems</a>
        </div>
        <div class="nav_item">
            <a href="../manage/viewProfiles.action">Profiles</a>
        </div>
        <div class="nav_item">
            <a href="../manage/viewUsers.action">Users</a>
        </div>
        <div class="nav_item">
            <a href="../manage/viewKeys.action">Public SSH Keys</a>
        </div>
        <div class="nav_item">
            <a href="../manage/distributeKeysByProfile.action">Distribute SSH Keys</a>
        </div>
    </s:if>
     <div class="nav_item">
        <a href="../admin/viewSystems.action">Composite SSH Terms</a>
     </div>
     <div class="nav_item">
        <a href="../admin/viewScripts.action">Composite Scripts</a>
     </div>
    <s:if test="%{#session.userType==\"M\"}">
    <div class="nav_item">
        <a href="../manage/viewSessions.action">Audit Sessions</a>
    </div>

    </s:if>
    <div class="nav_item">
        <a href="../admin/setPassword.action">Change Password</a>
    </div>
    <div class="nav_item">
        <a href="../logout.action">Logout</a>
    </div>

</div>
