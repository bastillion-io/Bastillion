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
            //Select the first textbox in modal
            $('.modal').on('shown.bs.modal', function () {
                $('input:text:visible:first').focus();
            });
            
            //call delete action
            $(".del_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteProfile.action?profile.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('.modal').find('form').submit();
            });

            $(".sort,.sortAsc,.sortDesc").click(function () {
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

            $('.scrollableTable').tableScroll({height: 500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
        });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function () {
                <s:if test="profile.id>0">
                $("#edit_dialog_<s:property value="profile.id"/>").modal();
                </s:if>
                <s:else>
                $("#add_dialog").modal();
                </s:else>
            });
        </script>
    </s:if>

    <title>KeyBox - Manage System Profiles</title>
</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        <s:form action="viewProfiles">
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
            <s:if test="script!=null">
                <s:hidden name="script.id"/>
            </s:if>
        </s:form>


        <h3>Manage System Profiles</h3>

        <p>Add / Delete profiles or select a profile below to assign systems to that profile.</p>


        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

            <table class="table-striped scrollableTable">
                <thead>



                <tr>
                    <th id="<s:property value="@com.keybox.manage.db.ProfileDB@SORT_BY_PROFILE_NM"/>" class="sort">
                        Profile Name
                    </th>
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
                            <div style="width:240px">
                                <a href="viewProfileSystems.action?profile.id=<s:property value="id"/>">
                                <div class="spacer spacer-left">
                                    <button id="assign_btn_<s:property value="id"/>" class="btn btn-default edit_btn">Assign Systems</button></a>
                                </div>
                                <div class="spacer spacer-middle">
                                    <button class="btn btn-default" data-toggle="modal" data-target="#edit_dialog_<s:property value="id"/>">Edit</button>
                                </div>
                                <div class="spacer spacer-right">
                                    <button id="del_btn_<s:property value="id"/>" class="btn btn-default del_btn">Delete</button>
                                </div>
                                <div style="clear:both"/>
                            </div>
                        </td>
                    </tr>


                </s:iterator>
                </tbody>
            </table>
        </s:if>

        <button class="btn btn-default add_btn" data-toggle="modal" data-target="#add_dialog">Add Profile</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Add Profile</h4>
                    </div>
                    <div class="modal-body">
                        <s:form action="saveProfile" class="save_profile_form_add">
                            <s:textfield name="profile.nm" label="Name" size="15" placeholder="Mandatory field"/>
                            <s:textarea name="profile.desc" label="Profile Description" rows="5" cols="25"/>
                            <s:hidden name="sortedSet.orderByDirection"/>
                            <s:hidden name="sortedSet.orderByField"/>
                         </s:form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-default submit_btn">Submit</button>
                    </div>
                </div>
            </div>
        </div>

        <s:iterator var="profile" value="sortedSet.itemList" status="stat">
            <div id="edit_dialog_<s:property value="id"/>" class="modal fade">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <h4 class="modal-title">Edit Profile</h4>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <s:form action="saveProfile" id="save_profile_form_edit_%{id}">
                                    <s:textfield name="profile.nm" value="%{nm}" label="Name" size="15"/>
                                    <s:textarea name="profile.desc" value="%{desc}" label="Profile Description" rows="5"
                                                cols="25"/>
                                    <s:hidden name="profile.id" value="%{id}"/>
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
