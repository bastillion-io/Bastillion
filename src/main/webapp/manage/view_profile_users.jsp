<%
/**
 * Copyright 2015 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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



            //open add dialog
            $("#assign_users").button().click(function() {
                $('#assignUsersToProfile').submit();
            });

            //select all check boxes
            $("#assignUsersToProfile_userSelectAll").click(function() {

                if ($(this).is(':checked')) {
                    $(".userSelect").prop('checked', true);
                } else {
                    $(".userSelect").prop('checked', false);
                }
            });

               $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewProfileUsers_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewProfileUsers_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewProfileUsers_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewProfileUsers_sortedSet_orderByField').attr('value', id);
                $("#viewProfileUsers").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

            <s:if test="userList!= null && !userList.isEmpty()">
            <s:iterator var="user" value="userList" status="stat">
            $(':checkbox[value=<s:property value="id"/>]').prop('checked', true);
            </s:iterator>
            </s:if>


        });
    </script>

    <title>KeyBox - Assign Users to Profile</title>

</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">

       <s:form action="viewProfileUsers">
           <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
            <s:hidden name="profile.id"/>
        </s:form>

        <h3>Assign Users to Profile</h3>

        <p>Select the users below to be assigned to the current profile.</p>

        <h4><s:property value="profile.nm"/></h4>
        <p class="small"><s:property value="profile.desc"/></p>


        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <s:form action="assignUsersToProfile" theme="simple">
                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                <s:hidden name="profile.id"/>

           <div class="scrollWrapper">
                <table class="table-striped scrollableTable">
                    <thead>

                    <tr>
                        <th><s:checkbox name="userSelectAll" cssClass="userSelect" theme="simple"/></th>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@USERNAME"/>" class="sort">Username
                        </th>
                        <s:if test="%{@com.keybox.manage.util.ExternalAuthUtil@externalAuthEnabled}">
                            <th id="<s:property value="@com.keybox.manage.db.UserDB@AUTH_TYPE"/>" class="sort">Auth Type
                            </th>
                        </s:if>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@LAST_NM"/>" class="sort">Last
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@FIRST_NM"/>" class="sort">First
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.UserDB@EMAIL"/>" class="sort">Email
                            Address
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <s:iterator var="user" value="sortedSet.itemList" status="stat">
                        <tr>
                            <td>
                                <s:checkboxlist id="userSelectId_%{id}" list="#{id:''}" name="userSelectId" cssClass="userSelect"
                                                theme="simple"/>
                            </td>
                            <td>
                                <s:if test="userType==\"M\"">
                                    <s:property value="username"/>
                                </s:if>
                                <s:else>
                                    <s:property value="username"/>
                                </s:else>
                            </td>
                            <s:if test="%{@com.keybox.manage.util.ExternalAuthUtil@externalAuthEnabled}">
                                <td>
                                    <s:if test="authType==\"BASIC\"">
                                        Basic
                                    </s:if>
                                    <s:else>
                                        External
                                    </s:else>
                                </td>
                            </s:if>
                            <td><s:property value="lastNm"/></td>
                            <td><s:property value="firstNm"/></td>
                            <td><s:property value="email"/></td>
                        </tr>
                    </s:iterator>
                    </tbody>
                </table>
               </div>
            </s:form>
            <div id="assign_users" class="btn btn-default assign_user_btn spacer spacer-bottom">Assign</div>
        </s:if>
        <s:else>
            <div class="error">There are no users defined (<a href="viewUsers.action?_csrf=<s:property value="#session['_csrf']"/>">Manage Users</a>).</div>
        </s:else>
    </div>
</body>
</html>
