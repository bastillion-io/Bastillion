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
                height: 350,
                width: 800,
                modal: true
            });

            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 350,
                width: 800,
                modal: true
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
            //call delete action
            $(".del_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deletePublicKey.action?publicKey.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('form:first').submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function () {
                $("#add_dialog").dialog("close");
                $(".edit_dialog").dialog("close");
            });  //regenerate auth keys btn
            $(".gen_auth_keys_btn").button().click(function () {
                $("#gen_auth_keys").submit();
            });

            $(".sort,.sortAsc,.sortDesc").click(function () {
                var id = $(this).attr('id')

                if ($('#viewKeys_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewKeys_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewKeys_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewKeys_sortedSet_orderByField').attr('value', id);
                $("#viewKeys").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>


            $('.scrollableTable').tableScroll({height: 500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
        });
    </script>

    <s:if test="fieldErrors.size > 0 || actionErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function () {
                <s:if test="publicKey.id>0">
                $("#edit_dialog_<s:property value="publicKey.id"/>").dialog("open");
                </s:if>
                <s:else>
                $("#add_dialog").dialog("open");
                </s:else>
            });
        </script>
    </s:if>


    <title>KeyBox - Manage Keys</title>

</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">
        <s:form action="viewKeys">
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
        </s:form>

        <h3>Manage Keys</h3>

        <p>Add / Delete keys and assign to profile</p>


        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <table class="vborder scrollableTable">
                <thead>

                <tr>

                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_KEY_NM"/>" class="sort">Key
                        Name
                    </th>

                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_PROFILE"/>" class="sort">
                        Profile
                    </th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>
                <s:iterator var="publicKey" value="sortedSet.itemList" status="stat">
                    <tr>
                        <td><s:property value="keyNm"/></td>
                        <td>
                            <s:if test="%{#publicKey.profile==null}">
                                All Systems
                            </s:if>
                            <s:else>
                                <s:property value="profile.nm"/>
                            </s:else>
                        </td>
                        <td>
                            <div style="width:150px">
                                <div id="edit_btn_<s:property value="id"/>" class="edit_btn" style="float:left">
                                    Edit
                                </div>
                                <div id="del_btn_<s:property value="id"/>" class="del_btn" style="float:left">
                                    Delete
                                </div>
                                &nbsp;&nbsp;&nbsp;
                                <div style="clear:both"></div>
                            </div>
                        </td>
                    </tr>
                </s:iterator>
                </tbody>
            </table>
        </s:if>


        <div id="add_btn">Add Public Key</div>
        <div id="add_dialog" title="Add Public Key">
            <s:form action="savePublicKey" class="save_public_key_form_add" autocomplete="off">
                <s:textfield name="publicKey.keyNm" label="Key Name" size="15"/>
                <s:select name="publicKey.profile.id" list="profileList" headerKey="" headerValue="All Systems"
                          listKey="id" listValue="%{nm}" label="Profile"/>
                <s:textarea name="publicKey.publicKey" rows="10" cols="75"/>
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


        <s:iterator var="publicKey" value="sortedSet.itemList" status="stat">
            <div id="edit_dialog_<s:property value="id"/>" title="Edit Public Key" class="edit_dialog">
                <s:form action="savePublicKey" id="save_public_key_form_edit_%{id}" autocomplete="off">
                    <s:hidden name="publicKey.id" value="%{id}"/>
                    <s:textfield name="publicKey.keyNm" value="%{keyNm}" label="Key Name" size="15"/>
                    <s:select name="publicKey.profile.id" list="profileList" headerKey="" headerValue="All Systems"
                              listKey="id" listValue="%{nm}" label="Profile" value="%{profile.id}"/>

                    <s:textarea name="publicKey.publicKey" value="%{publicKey}" label="Public Key" rows="10" cols="75"/>
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


    </div>
</div>
</body>
</html>
