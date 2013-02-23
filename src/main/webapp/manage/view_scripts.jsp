<%
/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
 */
%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>

    <jsp:include page="../_res/inc/header.jsp"/>

    <script type="text/javascript">
        $(document).ready(function() {

            $("#add_dialog").dialog({
                autoOpen: false,
                height: 400,
                width: 600,
                modal: true
            });

            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 400,
                width: 600,
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
                window.location = 'deleteScript.action?script.id='+ id +'&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
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
            $("#gen_auth_keys_scriptSelectAll").click(function() {

                if ($(this).is(':checked')) {
                    $(".scriptSelect").attr('checked', true);
                } else {
                    $(".scriptSelect").attr('checked', false);
                }
            });
            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewScripts_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewScripts_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewScripts_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewScripts_sortedSet_orderByField').attr('value', id);
                $("#viewScripts").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>


            $('.scrollableTable').tableScroll({height:500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
        });
    </script>

    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function() {
                <s:if test="script.id>0">
                $("#edit_dialog_<s:property value="script.id"/>").dialog("open");
                </s:if>
                <s:else>
                $("#add_dialog").dialog("open");
                </s:else>


            });
        </script>
    </s:if>

    <title>KeyBox - Manage Scripts</title>

</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">
        <s:set id="selectForm"><s:property value="#parameters['selectForm']"/></s:set>
        <s:form action="viewScripts">
            <s:hidden name="sortedSet.orderByDirection" />
            <s:hidden name="sortedSet.orderByField"/>
        </s:form>
            <h3>Manage Scripts</h3>

            <p>Add / Delete scripts or select a script below to execute</p>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <table class="vborder scrollableTable">
                    <thead>
                    <tr>

                        <th id="<s:property value="@com.keybox.manage.db.ScriptDB@SORT_BY_DISPLAY_NM"/>" class="sort">Script Name</th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <s:iterator var="script" value="sortedSet.itemList" status="stat">
                    <tr>
                        <td>
                                <a title="Execute Script" href="viewSystems.action?selectForm=true&script.id=<s:property value="id"/>""><s:property value="displayNm"/></a>
                        </td>
                            <td>
                                <div id="edit_btn_<s:property value="id"/>" class="edit_btn" style="float:left">
                                    Edit
                                </div>
                                <div id="del_btn_<s:property value="id"/>" class="del_btn" style="float:left">
                                    Delete
                                </div>
                                <div style="clear:both"></div>
                            </td>
                    </tr>
                    </s:iterator>
                    </tbody>
                </table>
        </s:if>



            <div id="add_btn">Add Script</div>
            <div id="add_dialog" title="Add Script">
                <s:form action="saveScript" class="save_script_form_add">
                    <s:textfield name="script.displayNm" label="Script Name" size="15"/>
                    <s:textarea name="script.script" label="Script" rows="15" cols="35" wrap="off"/>
                    <s:hidden name="sortedSet.orderByDirection"/>
                    <s:hidden name="sortedSet.orderByField"/>
                </s:form>
                <div class="submit_btn">Submit</div>
                <div class="cancel_btn">Cancel</div>
            </div>


            <s:iterator var="script" value="sortedSet.itemList" status="stat">
                <div id="edit_dialog_<s:property value="id"/>" title="Edit Script" class="edit_dialog">
                    <s:form action="saveScript" id="save_script_form_edit_%{id}">
                       <s:textfield name="script.displayNm" value="%{displayNm}"  label="Script Name" size="15"/>
                       <s:textarea name="script.script" value="%{script}" label="Script" rows="15" cols="35" wrap="off"/>
                       <s:hidden name="script.id" value="%{id}"/>
                       <s:hidden name="sortedSet.orderByDirection"/>
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
