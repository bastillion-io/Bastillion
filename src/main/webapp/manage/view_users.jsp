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
        //Call when you manually toggle the list (Enable/Disable OTP)
        function toggleOtpReset() {
            var element = document.querySelectorAll("[id$=resetSharedSecret]");
            
            for (var i = 0; i < element.length; i++) {
                if (element[i].disabled.length > 0 ||
                    element[i].disabled === true) {
                    element[i].disabled="";
                } else {
                    element[i].disabled="disabled";
                }
            }
        }
        
        $(document).ready(function() {
            //Select the first textbox in modal
            $('.modal').on('shown.bs.modal', function () {
                $('input:text:visible:first').focus();
            });
            
            //call delete action
            $(".del_btn").button().click(function() {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteUser.action?user.id='+ id +'&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).parents('.modal').find('form').submit();
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


            $('.scrollableTable').tableScroll({height:500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
        });
    </script>

    <s:if test="fieldErrors.size > 0 || actionErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function() {
                <s:if test="user.id>0">
                $("#edit_dialog_<s:property value="user.id"/>").modal();
                </s:if>
                <s:else>
                $("#add_dialog").modal();
                </s:else>
            });
        </script>
    </s:if>

    <title>KeyBox - Manage Users</title>

</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        <s:form action="viewUsers">
            <s:hidden name="sortedSet.orderByDirection" />
            <s:hidden name="sortedSet.orderByField"/>
        </s:form>

        <h3>Manage Users</h3>

        <p>Add / Delete users or select a user below to assign profile</p>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <table class="table-striped  scrollableTable" style="min-width: 85%">

                    <thead>
                    <tr>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@SORT_BY_USERNAME"/>" class="sort">Username
                        </th>
                         <th id="<s:property value="@com.keybox.manage.db.UserDB@SORT_BY_USER_TYPE"/>" class="sort">User Type
                                                </th>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@SORT_BY_LAST_NM"/>" class="sort">Last
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@SORT_BY_FIRST_NM"/>" class="sort">First
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@SORT_BY_EMAIL"/>" class="sort">Email
                            Address
                        </th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <s:iterator var="user" value="sortedSet.itemList" status="stat">
                    <tr>
                        <td>
                         <s:if test="userType==\"M\"">
                            <s:property value="username"/>
                         </s:if>
                         <s:else>
                            <a href="viewUserProfiles.action?user.id=<s:property value="id"/>"
                            title="Manage Profiles for User">
                                <s:property value="username"/>
                            </a>
                         </s:else>
                        </td>
                        <td>
                         <s:if test="userType==\"A\"">
                            Administrative Only
                         </s:if>
                         <s:else>
                            Full Access
                         </s:else>
                        </td>
                        <td><s:property value="lastNm"/></td>
                        <td><s:property value="firstNm"/></td>
                        <td><s:property value="email"/></td>
                            <td>
                                <div style="width:235px">


                                <button class="btn btn-default" data-toggle="modal" data-target="#edit_dialog_<s:property value="id"/>" style="float:left">Edit</button>

                                <button id="del_btn_<s:property value="id"/>" class="btn btn-default del_btn" style="float:left" >Delete</button>

                                <s:if test="userType==\"A\"">
                                    <a href="viewUserProfiles.action?user.id=<s:property value="id"/>">
                                        <button id="profile_btn_<s:property value="id"/>" class="btn btn-default edit_btn" style="float:left">Assign Profiles</button>
                                    </a>
                                </s:if>
                                <div style="clear:both"></div>
                                </div>
                            </td>
                    </tr>
                    </s:iterator>
                    </tbody>
                </table>
        </s:if>




        <button class="btn btn-default add_btn" data-toggle="modal" data-target="#add_dialog">Add User</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Add User</h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:actionerror/>
                            <s:form action="saveUser" class="save_user_form_add" autocomplete="off">
                                <s:textfield name="user.username" label="Username" size="15" placeholder="Mandatory field"/>
                                <s:select name="user.userType" list="#{'A':'Administrative Only','M':'Full Access'}" label="UserType"/>
                                <s:textfield name="user.firstNm" label="First Name" size="15" placeholder="Mandatory field"/>
                                <s:textfield name="user.lastNm" label="Last Name" size="15" placeholder="Mandatory field"/>
                                <s:textfield name="user.email" label="Email Address" size="25"/>
                                <s:password name="user.password" value="" label="Password" size="15" placeholder="Mandatory field"/>
                                <s:password name="user.passwordConfirm" value="" label="Confirm Password" size="15" placeholder="Mandatory field"/>
                                <s:select name="user.useOtp" list="#{true:'true',false:'false'}" label="Show OTP page"/>
                                <s:hidden name="resetSharedSecret"/>
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


            <s:iterator var="user" value="sortedSet.itemList" status="stat">
                <div id="edit_dialog_<s:property value="id"/>" class="modal fade">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                                <h4 class="modal-title">Edit System</h4>
                            </div>
                            <div class="modal-body">
                                <div class="row">
                                    <s:actionerror/>
                                    <s:form action="saveUser" id="save_user_form_edit_%{id}" autocomplete="off">
                                        <s:textfield name="user.username" value="%{username}" label="Username" size="15" placeholder="Mandatory field"/>
                                        <s:select name="user.userType" value="%{userType}" list="#{'A':'Administrative Only','M':'Full Access'}" label="UserType"/>
                                        <s:textfield name="user.firstNm" value="%{firstNm}" label="First Name" size="15" placeholder="Mandatory field"/>
                                        <s:textfield name="user.lastNm" value="%{lastNm}" label="Last Name" size="15" placeholder="Mandatory field"/>
                                        <s:textfield name="user.email" value="%{email}" label="Email Address" size="25"/>
                                        <s:password name="user.password" value="" label="Password" size="15" placeholder="Password hidden"/>
                                        <s:password name="user.passwordConfirm" value="" label="Confirm Password" size="15" placeholder="Password hidden"/>
                                        <s:select name="user.useOtp" value="%{useOtp}" list="#{'true':'true','false':'false'}" label="Show OTP page" onchange="toggleOtpReset()"/>
                                        <s:if test="useOtp==true">
                                            <s:checkbox name="resetSharedSecret" label="Reset OTP Code"/>
                                        </s:if>
                                        <s:else>
                                            <s:checkbox name="resetSharedSecret" label="Reset OTP Code" disabled="true"/>
                                        </s:else>
                                        <s:hidden name="user.id" value="%{id}"/>
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
