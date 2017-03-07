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
            $("#view_btn").button().click(function () {
                $("#viewSystems").submit();
            });
            $(".refresh_btn").button().click(function () {
                //get id to submit edit form
                var id = $(this).attr('id').replace("refresh_btn_", "");
                $("#save_sys_form_edit_" + id).submit();

            });
            //call delete action
            $(".del_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteSystem.action?hostSystem.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>&_csrf=<s:property value="#session['_csrf']"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('.modal').find('form').submit();
            });
            $(".sort,.sortAsc,.sortDesc").click(function () {
                var id = $(this).attr('id');

                if ($('#viewSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'desc');
                } else {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewSystems_sortedSet_orderByField').attr('value', id);
                $("#viewSystems").submit();

            });
            <s:if test="sortedSet.orderByField!=null && sortedSet.orderByField!=''">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>


            <s:if test="hostSystem.statusCd=='AUTHFAIL'">
            $("#set_password_dialog").modal();
            </s:if>
            <s:elseif test="hostSystem.statusCd=='KEYAUTHFAIL'">
            $("#set_passphrase_dialog").modal();
            </s:elseif>
            <s:elseif test="pendingSystem!=null">
            $("#error_dialog").modal();
            </s:elseif>

        });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function () {
                <s:if test="hostSystem.id>0">
                $("#edit_dialog_<s:property value="hostSystem.id"/>").modal();
                </s:if>
                <s:else>
                $("#add_dialog").modal();
                </s:else>
            });
        </script>
    </s:if>

    <title>KeyBox - Manage Systems / Distribute SSH Keys</title>
</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        <s:form action="viewSystems" theme="simple">
            <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>

            <h3>Manage Systems</h3>

            <p>Add / Delete systems below or distribute SSH keys</p>

            <s:if test="profileList!= null && !profileList.isEmpty()">
               <div>
                     <table>
                        <tr>
                            <td class="align_left">
                                <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SystemDB@FILTER_BY_PROFILE_ID}']" listKey="id" listValue="nm"
                                class="view_frm_select"
                                list="profileList"
                                headerKey=""
                                headerValue="-Select Profile-"/>
                            </td>
                            <td>
                                <div id="view_btn" class="btn btn-default">Filter</div>
                            </td>
                        </tr>
                     </table>
               </div>
            </s:if>
        </s:form>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

            <div class="scrollWrapper">
            <table class="table-striped scrollableTable">
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
                            <s:elseif test="statusCd=='HOSTFAIL'">
			                	<div class="error">DNS Lookup Failed</div>
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

                            <div style="width:160px">
                                    <button id="refresh_btn_<s:property value="id"/>" class="btn btn-default refresh_btn spacer spacer-left"><img src="../img/refresh.png" alt="Refresh" style="float:left;width:20px;height:20px;"/></button>
                                    <button class="btn btn-default spacer spacer-middle" data-toggle="modal" data-target="#edit_dialog_<s:property value="id"/>">Edit</button>
                                    <button id="del_btn_<s:property value="id"/>" class="btn btn-default del_btn spacer spacer-right">Delete</button>
                                <div style="clear:both"></div>
                            </div>
                        </td>
                    </tr>

                </s:iterator>
                </tbody>

            </table>
            </div>

        </s:if>

        <button class="btn btn-default add_btn spacer spacer-bottom" data-toggle="modal" data-target="#add_dialog">Add System</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Add System</h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:form action="saveSystem" class="save_sys_form_add">
                                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                <s:textfield name="hostSystem.displayNm" label="Display Name" size="10"/>
                                <s:textfield name="hostSystem.user" label="System User" size="10"/>
                                <s:textfield name="hostSystem.host" label="Host" size="18"/>
                                <s:textfield name="hostSystem.port" label="Port" size="2"/>
                                <s:textfield name="hostSystem.authorizedKeys" label="Authorized Keys" size="30"/>
                                <s:hidden name="sortedSet.orderByDirection"/>
                                <s:hidden name="sortedSet.orderByField"/>
                            </s:form>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-default submit_btn">Submit</button>
                    </div>
                </div>
            </div>
        </div>

        <s:iterator var="system" value="sortedSet.itemList" status="stat">
            <div id="edit_dialog_<s:property value="id"/>" class="modal fade">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <h4 class="modal-title">Edit System</h4>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <s:form action="saveSystem" id="save_sys_form_edit_%{id}">
                                    <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                    <s:textfield name="hostSystem.displayNm" value="%{displayNm}" label="Display Name" size="10"/>
                                    <s:textfield name="hostSystem.user" value="%{user}" label="System User" size="10"/>
                                    <s:textfield name="hostSystem.host" value="%{host}" label="Host" size="18"/>
                                    <s:textfield name="hostSystem.port" value="%{port}" label="Port" size="2"/>
                                    <s:textfield name="hostSystem.authorizedKeys" value="%{authorizedKeys}"
                                                 label="Authorized Keys" size="30"/>
                                    <s:hidden name="hostSystem.id" value="%{id}"/>
                                    <s:hidden name="sortedSet.orderByDirection"/>
                                    <s:hidden name="sortedSet.orderByField"/>
                                </s:form>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                            <button type="button" class="btn btn-default submit_btn">Submit</button>
                        </div>
                    </div>
                </div>
            </div>
       </s:iterator>



        <div id="set_password_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Enter password for <s:property value="hostSystem.displayLabel"/></h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <div class="error">Error: <s:property value="hostSystem.errorMsg"/></div>
                            <s:form id="password_frm" action="saveSystem">
                                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
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
                            </s:form>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-default submit_btn">Submit</button>
                    </div>
                </div>
            </div>
        </div>



        <div id="set_passphrase_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Enter passphrase for <s:property value="hostSystem.displayLabel"/></h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <div class="error">Error: <s:property value="hostSystem.errorMsg"/></div>
                            <s:form id="passphrase_frm" action="saveSystem">
                                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
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
                            </s:form>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-default submit_btn">Submit</button>
                    </div>
                </div>
            </div>
        </div>

        <div id="error_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">System: <s:property value="hostSystem.displayLabel"/></h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <div class="error">Error: <s:property value="hostSystem.errorMsg"/></div>
                            <s:form id="error_frm">
                                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                <s:hidden name="hostSystem.id"/>
                                <s:hidden name="pendingSystem.id"/>
                            </s:form>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default submit_btn">OK</button>
                    </div>
                </div>
            </div>
        </div>

    </div>


</body>
</html>
