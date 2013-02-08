<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>

    <jsp:include page="../_res/inc/header.jsp"/>

    <script type="text/javascript">
        $(document).ready(function() {

            $("#add_dialog").dialog({
                autoOpen: false,
                height: 450,
                width: 750,
                modal: true
            });

            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 450,
                width: 750,
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
                window.location = 'deleteUser.action?user.id='+ id +'&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).prev().submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function() {
                $("#add_dialog").dialog("close");
                $(".edit_dialog").dialog("close");
            });  //regenerate auth keys btn
            $(".gen_auth_keys_btn").button().click(function() {
                $("#gen_auth_keys").submit();
            });
            //select all check boxs
            $("#gen_auth_keys_userSelectAll").click(function() {

                if ($(this).is(':checked')) {
                    $(".userSelect").attr('checked', true);
                } else {
                    $(".userSelect").attr('checked', false);
                }
            });
            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewUsers_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewUsers_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewUsers_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewUsers_sortedSet_orderByField').attr('value', id);
                $("#viewUsers").submit();

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
                <s:if test="user.id>0">
                $("#edit_dialog_<s:property value="user.id"/>").dialog("open");
                </s:if>
                <s:else>
                $("#add_dialog").dialog("open");
                </s:else>


            });
        </script>
    </s:if>

    <title>KeyBox - Manage Users</title>

</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">
        <s:set id="genAuthKeys"><s:property value="#parameters['genAuthKeys']"/></s:set>
        <s:form action="viewUsers">
            <s:hidden name="sortedSet.orderByDirection" />
            <s:hidden name="sortedSet.orderByField"/>
            <s:hidden name="genAuthKeys"/>
        </s:form>
        <s:if test="#genAuthKeys=='true'">
            <h3>Generate Authorized Keys for Users</h3>

            <p>Select the users below to generate and set the authorized key file</p>

        </s:if>
        <s:else>
            <h3>Manage Users</h3>

            <p>Add / Delete users or select a user below to assign profile</p>
        </s:else>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <s:form action="selectUsersForAuthKeys" id="gen_auth_keys">
                <table class="vborder scrollableTable">
                    <thead>

                    <tr>

                        <s:if test="#genAuthKeys=='true'">
                            <th><s:checkbox name="userSelectAll" cssClass="systemSelect" fieldValue="true"
                                            theme="simple"
                                    /></th>
                        </s:if>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@SORT_BY_LAST_NM"/>" class="sort">Last
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@SORT_BY_FIRST_NM"/>" class="sort">First
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@SORT_BY_EMAIL"/>" class="sort">Email
                            Address
                        </th>
                        <s:if test="#genAuthKeys=='true'"></s:if>
                        <s:else>
                            <th>&nbsp;</th>
                        </s:else>
                    </tr>
                    </thead>
                    <tbody>


                    <s:iterator var="user" value="sortedSet.itemList" status="stat">
                    <tr>
                        <s:if test="#genAuthKeys=='true'">
                            <td>
                                <s:checkbox name="userSelectId" cssClass="userSelect" fieldValue="%{id}"
                                            value="checked" theme="simple"/>
                            </td>
                        </s:if>
                        <td>
                            <a href="viewUserProfiles.action?user.id=<s:property value="id"/>"
                               title="Manage Profiles for User">
                                <s:property value="lastNm"/>
                            </a>
                        </td>
                        <td><s:property value="firstNm"/></td>
                        <td><s:property value="email"/></td>
                        <s:if test="#genAuthKeys=='true'"></s:if>
                        <s:else>

                            <td>
                                <div id="edit_btn_<s:property value="id"/>" class="edit_btn" style="float:left">
                                    Edit
                                </div>
                                <div id="del_btn_<s:property value="id"/>" class="del_btn" style="float:left">
                                    Delete
                                </div>
                            </td>
                        </s:else>
                    </tr>
                    </tbody>

                    </s:iterator>
                </table>
            </s:form>
        </s:if>

        <s:if test="#genAuthKeys=='true'">

            <div id="gen_auth_keys" class="gen_auth_keys_btn">Generate Authorized Keys</div>
        </s:if>


        <s:else>

            <div id="add_btn">Add User</div>
            <div id="add_dialog" title="Add User">
                <s:form action="saveUser" class="save_user_form_add">
                    <s:textfield name="user.firstNm" value="" label="First Name" size="15"/>
                    <s:textfield name="user.lastNm" value="" label="Last Name" size="15"/>
                    <s:textfield name="user.email" value="" label="Email Address" size="25"/>
                    <s:textarea name="user.publicKey" value="" label="Public Key" rows="10" cols="75"/>
                    <s:hidden name="user.id" value=""/>
                    <s:hidden name="sortedSet.orderByDirection"/>
                    <s:hidden name="sortedSet.orderByField"/>
                    <s:hidden name="genAuthKeys"/>
                </s:form>
                <div class="submit_btn">Submit</div>
                <div class="cancel_btn">Cancel</div>
            </div>


            <s:iterator var="user" value="sortedSet.itemList" status="stat">
                <div id="edit_dialog_<s:property value="id"/>" title="Edit User" class="edit_dialog">
                    <s:form action="saveUser" id="save_user_form_edit_%{id}">
                        <s:textfield name="user.firstNm" value="%{firstNm}" label="First Name" size="15"/>
                        <s:textfield name="user.lastNm" value="%{lastNm}" label="Last Name" size="15"/>
                        <s:textfield name="user.email" value="%{email}" label="Email Address" size="25"/>
                        <s:textarea name="user.publicKey" value="%{publicKey}" label="Public Key" rows="10" cols="75"/>
                        <s:hidden name="user.id" value="%{id}"/>
                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <s:hidden name="genAuthKeys"/>
                    </s:form>
                    <div class="submit_btn">Submit</div>
                    <div class="cancel_btn">Cancel</div>
                </div>
            </s:iterator>

        </s:else>

    </div>
</div>
</body>
</html>
