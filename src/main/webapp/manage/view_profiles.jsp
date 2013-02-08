<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>

    <jsp:include page="../_res/inc/header.jsp"/>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#add_dialog").dialog({
                autoOpen: false,
                height: 275,
                width: 375,
                modal: true
            });
            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 275,
                width: 375,
                modal: true
            });
            //open add dialog
            $("#add_btn").button().click(function() {
                $("#add_dialog").dialog("open");
            });
            //open edit dialog
            $(".edit_btn").button().click(function() {
                //get dialog id to open
                var id = $(this).attr('id').replace("edit_btn_", "");
                $("#edit_dialog_" + id).dialog("open");

            });

            //call delete action
            $(".del_btn").button().click(function() {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteProfile.action?profile.id=' + id +'&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).prev().submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function() {
                $("#add_dialog").dialog("close");
                $(".edit_dialog").dialog("close");
            });
            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewProfiles_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewProfiles_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewProfiles_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewProfiles_sortedSet_orderByField').attr('value', id);
                $("#viewProfiles").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

            $('.scrollableTable').tableScroll({height:600});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
        });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function() {
                <s:if test="profile.id>0">
                $("#edit_dialog_<s:property value="profile.id"/>").dialog("open");
                </s:if>
                <s:else>
                $("#add_dialog").dialog("open");
                </s:else>
            });
        </script>
    </s:if>

    <title>KeyBox - Manage System Profiles</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">
        <s:form action="viewProfiles">
            <s:hidden name="sortedSet.orderByDirection" />
            <s:hidden name="sortedSet.orderByField"/>
        </s:form>

        <h3>Manage System Profiles</h3>

        <p>Add / Delete profiles or select a profile below to assign systems to that profile.</p>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <table class="vborder scrollableTable">
                <thead>


                <tr>
                    <th id="<s:property value="@com.keybox.manage.db.ProfileDB@SORT_BY_PROFILE_NM"/>" class="sort">Profile Name</th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>



                <s:iterator var="profile" value="sortedSet.itemList" status="stat">
                    <tr>


                        <td>
                            <a href="viewProfileSystems.action?profile.id=<s:property value="id"/>"
                               title="Manage Systems in Profile">
                                <s:property value="nm"/>
                            </a>
                        </td>

                        <td>
                            <div id="edit_btn_<s:property value="id"/>" class="edit_btn" style="float:left">Edit
                            </div>
                            <div id="del_btn_<s:property value="id"/>" class="del_btn" style="float:left">Delete
                            </div>
                        </td>
                    </tr>

                </tbody>

                </s:iterator>
            </table>
        </s:if>


        <div id="add_btn">Add Profile</div>
        <div id="add_dialog" title="Add Profile">
            <s:form action="saveProfile" class="save_profile_form_add">
                <s:textfield name="profile.nm" label="Profile Name" size="15"/>
                <s:textarea name="profile.desc" label="Profile Description" rows="5" cols="25"/>
                <s:hidden name="sortedSet.orderByDirection" />
                <s:hidden name="sortedSet.orderByField"/>
            </s:form>
            <div class="submit_btn">Submit</div>
            <div class="cancel_btn">Cancel</div>
        </div>


        <s:iterator var="profile" value="sortedSet.itemList" status="stat">
            <div id="edit_dialog_<s:property value="id"/>" title="Edit Profile" class="edit_dialog">
                <s:form action="saveProfile" id="save_profile_form_edit_%{id}">
                    <s:textfield name="profile.nm" value="%{nm}" label="Profile Name" size="15"/>
                    <s:textarea name="profile.desc" value="%{desc}" label="Profile Description" rows="5"
                                cols="25"/>
                    <s:hidden name="profile.id" value="%{id}"/>
                    <s:hidden name="sortedSet.orderByDirection" />
                    <s:hidden name="sortedSet.orderByField"/>
                </s:form>
                <div class="submit_btn">Submit</div>
                <div class="cancel_btn">Cancel</div>
            </div>
        </s:iterator>

    </div>


</div>

</body>
</html>
