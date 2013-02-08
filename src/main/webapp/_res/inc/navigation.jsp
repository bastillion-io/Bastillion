<div class="nav">

    <div style="float: left;margin-top: 5px;margin-left: -10px"><img src="<%= request.getContextPath() %>/img/keybox_50x38.png"/></div>

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
            <a href="viewSystems.action?genAuthKeys=true">Generate Keys for Systems</a>
        </div>

    <div class="nav_item">
            <a href="viewUsers.action?genAuthKeys=true">Generate Keys for Users</a>
        </div>

     <div class="nav_item">
            <a href="setPassword.action">Change Admin Password</a>
        </div>

   <div class="nav_item">
            <a href="../logout.action">Logout</a>
        </div>

</div>
