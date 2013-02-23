<%
/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
%>
<div class="nav">

<!--
    <div style="float: left;margin-top: 5px;margin-left: -10px"><img src="<%= request.getContextPath() %>/img/keybox_50x38.png"/></div>
-->

        <h3>

            KeyBox
        </h3>

        <div class="nav_item">
            <a href="viewSystems.action">Systems</a>
        </div>
        <div class="nav_item">
            <a href="viewUsers.action">Users</a>
        </div>
      <div class="nav_item">
            <a href="viewProfiles.action">System Profiles</a>
        </div>
    <div class="nav_item">
            <a href="viewSystems.action?selectForm=true">Distribute SSH Keys</a>
        </div>


     <div class="nav_item">
            <a href="viewScripts.action">Shell Scripts</a>
     </div>

     <div class="nav_item">
            <a href="setPassword.action">Change Password</a>
     </div>


   <div class="nav_item">
            <a href="../logout.action">Logout</a>
        </div>

</div>
