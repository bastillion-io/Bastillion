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
                height: 350,
                width: 350,
                modal: true
            });
            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 350,
                width: 350,
                modal: true
            });
             $("#script_dia").dialog({
                autoOpen: false,
                height: 350,
                width: 350,
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
            $("#script_btn").click(function() {
                $("#script_dia").dialog("open");
             });
            //call delete action
            $(".del_btn").button().click(function() {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteSystem.action?hostSystem.id=' + id +'&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
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
            //regenerate auth keys btn
            $(".select_frm_btn").button().click(function() {
                //change form action if executing script
                <s:if test="script!=null">
                    $("#select_frm").attr("action","selectSystemsForExecScript.action");
                </s:if>
                $("#select_frm").submit();
            });
            //select all check boxs
            $("#select_frm_systemSelectAll").click(function() {

                if ($(this).is(':checked')) {
                    $(".systemSelect").attr('checked', true);
                } else {
                    $(".systemSelect").attr('checked', false);
                }
            });
            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewSystems_sortedSet_orderByField').attr('value', id);
                $("#viewSystems").submit();

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
                <s:if test="hostSystem.id>0">
                $("#edit_dialog_<s:property value="hostSystem.id"/>").dialog("open");
                </s:if>
                <s:else>
                $("#add_dialog").dialog("open");
                </s:else>
            });
        </script>
    </s:if>

    <title>KeyBox - Manage Systems</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">
        <s:set id="selectForm"><s:property value="#parameters['selectForm']"/></s:set>
        <s:form action="viewSystems">
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
            <s:hidden name="selectForm"/>
            <s:if test="script!=null">
              <s:hidden name="script.id"/>
            </s:if>
        </s:form>

        <s:if test="#selectForm=='true'">
            <s:if test="script!=null">
                <h3>Execute Script on Systems</h3>
                <jsp:include page="../_res/inc/nav_sub.jsp"/>
                <p>Run <b>
                <a id="script_btn" href="#"><s:property value="script.displayNm"/></a></b> on the selected systems below
                </p>
                <div id="script_dia" title="View Script">
                    <pre><s:property value="script.script"/></pre>
                </div>
            </s:if>
            <s:else>
                <h3>Distribute Authorized Key for Systems</h3>
                <jsp:include page="../_res/inc/nav_sub.jsp"/>
                <p>Select the systems below to generate and set the authorized key file</p>
            </s:else>



        </s:if>
        <s:else>
            <h3>Manage Systems</h3>

            <p>Add / Delete systems below</p>
        </s:else>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

  	        <s:form action="selectSystemsForAuthKeys" id="select_frm" theme="simple">
  	             <s:if test="script!=null">
                        <s:hidden name="script.id"/>
                 </s:if>
                <table class="vborder scrollableTable">
                    <thead>
                    <tr>
                        <s:if test="#selectForm=='true'">
                            <th><s:checkbox name="systemSelectAll" cssClass="systemSelect"
                                            theme="simple"/></th>
                        </s:if>

                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Display
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host
                        </th>
                        <s:if test="#selectForm=='true'"></s:if>
                        <s:else>
                            <th>&nbsp;</th>
                        </s:else>
                    </tr>
                    </thead>
                    <tbody>
                    <s:iterator var="system" value="sortedSet.itemList" status="stat">
                        <tr>
                            <s:if test="#selectForm=='true'">
                                <td>
                                    <s:checkboxlist name="systemSelectId" list="#{id:''}" cssClass="systemSelect"
                                                theme="simple"/>
                                </td>
                            </s:if>
                            <td>
                                <s:property value="displayNm"/>
                            </td>
                            <td><s:property value="user"/></td>
                            <td><s:property value="host"/>:<s:property value="port"/></td>
                            <s:if test="#selectForm=='true'"></s:if>
                            <s:else>
                                <td>
                                    <div id="edit_btn_<s:property value="id"/>" class="edit_btn" style="float:left" >Edit </div>
                                    <div id="del_btn_<s:property value="id"/>" class="del_btn" style="float:left">Delete</div>
                                    <div style="clear:both"></div>
                                </td>
                            </s:else>
                        </tr>

                    </s:iterator>
                    </tbody>
                </table>
	    </s:form>
        </s:if>
        <s:if test="#selectForm=='true'">
            <s:if test="script!=null && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <div class="select_frm_btn">Execute Script</div>
            </s:if>
            <s:elseif test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <div class="select_frm_btn">Distribute Authorized Keys</div>
            </s:elseif>
            <s:else>
              <div class="error">There are no systems defined.  New systems may be defined <a href="viewSystems.action">here</a>.</div>
            </s:else>
        </s:if>
        <s:else>
            <div id="add_btn">Add System</div>
            <div id="add_dialog" title="Add System">
                <s:form action="saveSystem" class="save_sys_form_add">
                    <s:textfield name="hostSystem.displayNm" label="Display Name" size="10"/>
                    <s:textfield name="hostSystem.user" label="System User" size="10"/>
                    <s:textfield name="hostSystem.host" label="Host" size="18"/>
                    <s:textfield name="hostSystem.port" label="Port" size="2"/>
                    <s:textfield name="hostSystem.authorizedKeys" label="Authorized Keys"
                                 size="18"/>
                    <s:hidden name="sortedSet.orderByDirection"/>
                    <s:hidden name="sortedSet.orderByField"/>
                    <s:hidden name="selectForm"/>
                </s:form>
                <div class="submit_btn">Submit</div>
                <div class="cancel_btn">Cancel</div>
            </div>

            <s:iterator var="system" value="sortedSet.itemList" status="stat">

                <div id="edit_dialog_<s:property value="id"/>" title="Edit System" class="edit_dialog">
                    <s:form action="saveSystem" id="save_sys_form_edit_%{id}">
                        <s:textfield name="hostSystem.displayNm" value="%{displayNm}" label="Display Name" size="10"/>
                        <s:textfield name="hostSystem.user" value="%{user}" label="System User" size="10"/>
                        <s:textfield name="hostSystem.host" value="%{host}" label="Host" size="18"/>
                        <s:textfield name="hostSystem.port" value="%{port}" label="Port" size="2"/>
                        <s:textfield name="hostSystem.authorizedKeys" value="%{authorizedKeys}"
                                     label="Authorized Keys" size="18"/>
                        <s:hidden name="hostSystem.id" value="%{id}"/>
                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <s:hidden name="selectForm"/>
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
