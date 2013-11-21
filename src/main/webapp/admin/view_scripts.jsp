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

            $("#add_dialog").dialog({
                autoOpen: false,
                height: 425,
                width: 600,
                modal: true
            });

            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 425,
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
               $(this).parents('form:first').submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function() {
                $("#add_dialog").dialog("close");
                $(".edit_dialog").dialog("close");
            });  //regenerate auth keys btn
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
                                <a title="Execute Script" href="viewSystems.action?script.id=<s:property value="id"/>""><s:property value="displayNm"/></a>
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
                    <tr> <td>&nbsp;</td>
                        <td align="left"><div class="submit_btn">Submit</div>
                        <div class="cancel_btn">Cancel</div></td>
                    </tr>
                </s:form>

            </div>


            <s:iterator var="script" value="sortedSet.itemList" status="stat">
                <div id="edit_dialog_<s:property value="id"/>" title="Edit Script" class="edit_dialog">
                    <s:form action="saveScript" id="save_script_form_edit_%{id}">
                       <s:textfield name="script.displayNm" value="%{displayNm}"  label="Script Name" size="15"/>
                       <s:textarea name="script.script" value="%{script}" label="Script" rows="15" cols="35" wrap="off"/>
                       <s:hidden name="script.id" value="%{id}"/>
                       <s:hidden name="sortedSet.orderByDirection"/>
                       <s:hidden name="sortedSet.orderByField"/>
                       <tr> <td>&nbsp;</td>
                        <td align="left"><div class="submit_btn">Submit</div>
                        <div class="cancel_btn">Cancel</div></td>
                        </tr>
                    </s:form>
                </div>
            </s:iterator>


    </div>
</div>
</body>
</html>
