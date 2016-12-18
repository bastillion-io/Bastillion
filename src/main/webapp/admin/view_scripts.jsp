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
        $(document).ready(function() {


            //call delete action
            $(".del_btn").button().click(function() {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteScript.action?script.id='+ id +'&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>&_csrf=<s:property value="#session['_csrf']"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).parents('.modal').find('form').submit();
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

        });
    </script>

    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function() {
                <s:if test="script.id>0">
                $("#edit_dialog_<s:property value="script.id"/>").modal();
                </s:if>
                <s:else>
                $("#add_dialog").modal();
                </s:else>


            });
        </script>
    </s:if>

    <title>KeyBox - Manage Scripts</title>

</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        <s:form action="viewScripts">
            <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
            <s:hidden name="sortedSet.orderByDirection" />
            <s:hidden name="sortedSet.orderByField"/>
        </s:form>
            <h3>Manage Scripts</h3>

            <p>Add / Delete scripts or select a script below to execute</p>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="scrollWrapper">
                <table class="table-striped scrollableTable">
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
                                <a href="viewSystems.action?script.id=<s:property value="id"/>&_csrf=<s:property value="#session['_csrf']"/>"><s:property value="displayNm"/></a>
                        </td>
                            <td>
                                <div style="width:240px">

                                    <a href="viewSystems.action?script.id=<s:property value="id"/>&_csrf=<s:property value="#session['_csrf']"/>"><button id="exec_btn_<s:property value="id"/>" class="btn btn-default edit_btn spacer spacer-left">Execute Script</button></a>
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

        <button class="btn btn-default add_btn spacer spacer-bottom" data-toggle="modal" data-target="#add_dialog">Add Script</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Add Script</h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:form action="saveScript" class="save_script_form_add">
                                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                <s:textfield name="script.displayNm" label="Script Name" size="15"/>
                                <s:textarea name="script.script" label="Script" rows="15" cols="35" wrap="off"/>
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

            <s:iterator var="script" value="sortedSet.itemList" status="stat">
                <div id="edit_dialog_<s:property value="id"/>" class="modal fade">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                                <h4 class="modal-title">Edit Script</h4>
                            </div>
                            <div class="modal-body">
                                <div class="row">
                                    <s:form action="saveScript" id="save_script_form_edit_%{id}">
                                        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                        <s:textfield name="script.displayNm" value="%{displayNm}"  label="Script Name" size="15"/>
                                        <s:textarea name="script.script" value="%{script}" label="Script" rows="15" cols="35" wrap="off"/>
                                        <s:hidden name="script.id" value="%{id}"/>
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


    </div>

</body>
</html>
