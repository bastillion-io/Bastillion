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
    <script type="text/javascript">
        $(document).ready(function () {
            $("#add_dialog").dialog({
                autoOpen: false,
                height: 300,
                width: 425,
                modal: true
            });
            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 300,
                width: 425,
                modal: true
            });
            $("#script_dia").dialog({
                autoOpen: false,
                height: 350,
                width: 350,
                modal: true,
                open: function (event, ui) {
                    $(".ui-dialog-titlebar-close").show();
                }
            });
            //open add dialog
            $("#add_btn").button().click(function () {
                $("#add_dialog").dialog("open");
            });
            //open edit dialog
            $(".edit_btn").button().click(function () {
                //get dialog id to open
                var id = $(this).attr('id').replace("edit_btn_", "");
                $("#edit_dialog_" + id).dialog("open");

            });


            //open edit dialog
            $(".refresh_btn").button().click(function () {
                //get id to submit edit form
                var id = $(this).attr('id').replace("refresh_btn_", "");
                $("#save_sys_form_edit_" + id).submit();

            });


            //call delete action
            $(".del_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteSystem.action?hostSystem.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('form:first').submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function () {
                <s:if test="pendingSystem!=null">
                window.location = 'getNextPendingSystem.action?pendingSystem.id=<s:property value="pendingSystem.id"/>';
                </s:if>
                <s:else>
                $(".dialog").dialog("close");
                $(".edit_dialog").dialog("close");
                </s:else>
            });
            $(".sort,.sortAsc,.sortDesc").click(function () {
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


            $('.scrollableTable').tableScroll({height: 500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");


            $("#error_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 500,
                modal: true
            });
            $("#set_password_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 500,
                modal: true
            });
            $("#set_passphrase_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 500,
                modal: true
            });


            <s:if test="pendingSystem!=null">
            //change all form actions to distrubte keys
            $('form').attr('action', 'genAuthKeyForSystem.action');
            //disable all buttons
            $('.refresh_btn').button("disable");
            $('.edit_btn').button("disable");
            $('.del_btn').button("disable");
            $('.add_btn').button("disable");
            //set scroll
            var container = $('.tablescroll_wrapper'), scrollTo = $('#status_<s:property value="pendingSystem.id"/>');
            container.scrollTop(scrollTo.offset().top - container.offset().top + container.scrollTop() - 55);
            </s:if>

            <s:if test="hostSystem.statusCd=='GENERICFAIL'">
            $("#error_dialog").dialog("open");
            </s:if>
            <s:elseif test="hostSystem.statusCd=='AUTHFAIL'">
            $("#set_password_dialog").dialog("open");
            </s:elseif>
            <s:elseif test="hostSystem.statusCd=='KEYAUTHFAIL'">
            $("#set_passphrase_dialog").dialog("open");
            </s:elseif>
            <s:elseif test="pendingSystem!=null">
            $("#gen_key_frm").submit();
            </s:elseif>

        });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function () {
                <s:if test="hostSystem.id>0">
                $("#edit_dialog_<s:property value="hostSystem.id"/>").dialog("open");
                </s:if>
                <s:else>
                $("#add_dialog").dialog("open");
                </s:else>
            });
        </script>
    </s:if>

    <title>KeyBox - Manage Systems / Distribute SSH Keys</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <s:form id="gen_key_frm" action="genAuthKeyForSystem">
            <s:hidden name="pendingSystem.id"/>
        </s:form>

        <s:form action="viewSystems">
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
        </s:form>

        <h3>Manage Systems / Distribute SSH Keys</h3>

        <p>Add / Delete systems below or distribute SSH keys</p>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

            <table class="vborder scrollableTable">
                <thead>
                <tr>
                    <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Display
                        Name
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_STATUS"/>" class="sort">Status
                    </th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>
                <s:iterator var="system" value="sortedSet.itemList" status="stat">
                    <tr>
                        <td>
                           <div id="status_<s:property value="id"/>"><s:property value="displayNm"/></div>
                        </td>
                        <td><s:property value="user"/></td>
                        <td><s:property value="host"/>:<s:property value="port"/></td>
                        <td>
                            <s:if test="statusCd=='INITIAL'">
                                <div class="warning">Not Started</div>
                            </s:if>
                            <s:elseif test="statusCd=='AUTHFAIL'">
                                <div class="warning">Authentication Failed</div>
                            </s:elseif>
                            <s:elseif test="statusCd=='KEYAUTHFAIL'">
                                <div class="warning">Passphrase Authentication Failed</div>
                            </s:elseif>
                            <s:elseif test="statusCd=='GENERICFAIL'">
                                <div class="error">Failed</div>
                            </s:elseif>
                            <s:elseif test="statusCd=='SUCCESS'">
                                <div class="success">Success</div>
                            </s:elseif>
                        </td>
                        <td>

                            <div style="width:175px">

                                <img src="../../img/refresh.png" alt="Refresh" style="float:left" class="refresh_btn"
                                     id="refresh_btn_<s:property value="id"/>"/>

                                <div id="edit_btn_<s:property value="id"/>" class="edit_btn" style="float:left">Edit
                                </div>
                                <div id="del_btn_<s:property value="id"/>" class="del_btn" style="float:left">Delete
                                </div>
                                <div style="clear:both"></div>
                            </div>
                        </td>
                    </tr>

                </s:iterator>
                </tbody>
            </table>

        </s:if>
        <div id="add_btn" class="add_btn">Add System</div>
        <div id="add_dialog" class="dialog" title="Add System">
            <s:form action="saveSystem" class="save_sys_form_add">
                <s:textfield name="hostSystem.displayNm" label="Display Name" size="10"/>
                <s:textfield name="hostSystem.user" label="System User" size="10"/>
                <s:textfield name="hostSystem.host" label="Host" size="18"/>
                <s:textfield name="hostSystem.port" label="Port" size="2"/>
                <s:textfield name="hostSystem.authorizedKeys" label="Authorized Keys" size="30"/>
                <s:hidden name="sortedSet.orderByDirection"/>
                <s:hidden name="sortedSet.orderByField"/>
                <tr>
                    <td>&nbsp;</td>
                    <td align="left">
                        <div class="submit_btn">Submit</div>
                        <div class="cancel_btn">Cancel</div>
                    </td>
                </tr>
            </s:form>

        </div>

        <s:iterator var="system" value="sortedSet.itemList" status="stat">

            <div id="edit_dialog_<s:property value="id"/>" title="Edit System" class="edit_dialog">
                <s:form action="saveSystem" id="save_sys_form_edit_%{id}">
                    <s:textfield name="hostSystem.displayNm" value="%{displayNm}" label="Display Name" size="10"/>
                    <s:textfield name="hostSystem.user" value="%{user}" label="System User" size="10"/>
                    <s:textfield name="hostSystem.host" value="%{host}" label="Host" size="18"/>
                    <s:textfield name="hostSystem.port" value="%{port}" label="Port" size="2"/>
                    <s:textfield name="hostSystem.authorizedKeys" value="%{authorizedKeys}"
                                 label="Authorized Keys" size="30"/>
                    <s:hidden name="hostSystem.id" value="%{id}"/>
                    <s:hidden name="sortedSet.orderByDirection"/>
                    <s:hidden name="sortedSet.orderByField"/>
                    <tr>
                        <td>&nbsp;</td>
                        <td align="left">
                            <div class="submit_btn">Submit</div>
                            <div class="cancel_btn">Cancel</div>
                        </td>
                    </tr>
                </s:form>
            </div>
        </s:iterator>

        <div id="set_password_dialog" class="dialog" title="Enter Password">
            <p class="error"><s:property value="hostSystem.errorMsg"/></p>

            <p>Enter password for <s:property value="hostSystem.displayLabel"/>

            </p>
            <s:form id="password_frm" action="saveSystem">
                <s:hidden name="hostSystem.id"/>
                <s:hidden name="hostSystem.displayNm"/>
                <s:hidden name="hostSystem.user"/>
                <s:hidden name="hostSystem.host"/>
                <s:hidden name="hostSystem.port"/>
                <s:hidden name="hostSystem.authorizedKeys"/>
                <s:hidden name="sortedSet.orderByDirection"/>
                <s:hidden name="sortedSet.orderByField"/>
                <s:password name="password" label="Password" size="15" value="" autocomplete="off"/>
                <s:hidden name="pendingSystem.id"/>
                <tr>
                    <td>&nbsp;</td>
                    <td align="left">
                        <div class="submit_btn">Submit</div>
                        <div class="cancel_btn">Cancel</div>
                    </td>
                </tr>
            </s:form>
        </div>

        <div id="set_passphrase_dialog" class="dialog" title="Enter Passphrase">
            <p class="error"><s:property value="hostSystem.errorMsg"/></p>
            <s:form id="passphrase_frm" action="saveSystem">
                <s:hidden name="hostSystem.id"/>
                <s:hidden name="hostSystem.displayNm"/>
                <s:hidden name="hostSystem.user"/>
                <s:hidden name="hostSystem.host"/>
                <s:hidden name="hostSystem.port"/>
                <s:hidden name="hostSystem.authorizedKeys"/>
                <s:hidden name="sortedSet.orderByDirection"/>
                <s:hidden name="sortedSet.orderByField"/>
                <s:password name="passphrase" label="Passphrase" size="15" value="" autocomplete="off"/>
                <s:hidden name="pendingSystem.id"/>
                <tr>
                    <td>&nbsp;</td>
                    <td align="left">
                        <div class="submit_btn">Submit</div>
                        <div class="cancel_btn">Cancel</div>
                    </td>
                </tr>
            </s:form>
        </div>

        <div id="error_dialog" class="dialog" title="Error">
            <p class="error">Error: <s:property value="hostSystem.errorMsg"/></p>

            <p>System: <s:property value="hostSystem.displayLabel"/>

            </p>

            <s:form id="error_frm">
                <s:hidden name="hostSystem.id"/>
                <s:hidden name="pendingSystem.id"/>
                <tr>
                    <td>&nbsp;</td>
                    <td align="left">
                        <div class="cancel_btn">OK</div>
                    </td>
                </tr>
            </s:form>
        </div>


    </div>
</div>

</body>
</html>
