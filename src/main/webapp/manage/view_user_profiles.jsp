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

            //call delete action
            $(".del_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteProfileFromUser.action?user.id=<s:property value="user.id"/>&profileId=' + id;
            });
            //submit add or edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('.modal').find('form').submit();
            });

        });
    </script>

    <title>KeyBox - Grant System Access</title>

</head>
<body>


<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">
    <h3>Grant System Access</h3>

    <p>Add / Delete profiles to be assigned to the current user.</p>

    <h5><s:property value="user.username"/>
    <s:if test="%{user.lastNm!=null && user.firstNm!=null}">
        &nbsp;:&nbsp;<s:property value="user.lastNm"/>,&nbsp;<s:property value="user.firstNm"/>
    </s:if> <s:if test="%{user.email!=null}">
        &nbsp;(<s:property value="user.email"/>)
    </s:if>

    </h5>


    <s:if test="user.profileList!= null && !user.profileList.isEmpty()">

    <div class="scrollWrapper">
        <table class="table-striped scrollableTable">
            <thead>

            <tr>
                <th>Name</th>
                <th>&nbsp;</th>
            </tr>
            </thead>
            <tbody>
            <s:iterator var="profile" value="user.profileList" status="stat">
                <tr>
                    <td>
                        <s:property value="nm"/>
                    </td>

                    <td>
                        <a href="viewProfileUsers.action?profile.id=<s:property value="id"/>">
                            <button id="user_assign_btn_<s:property value="id"/>" class="btn btn-default edit_btn spacer spacer-left">Assign Users</button></a>
                        <a href="viewProfileSystems.action?profile.id=<s:property value="id"/>">
                            <button id="assign_btn_<s:property value="id"/>" class="btn btn-default edit_btn spacer spacer-left">Assign Systems</button></a>
                        <button id="del_btn_<s:property value="id"/>" class="btn btn-default del_btn spacer spacer-right">Delete</button>

                    </td>
                </tr>
            </s:iterator>
            </tbody>

        </table>
        </div>
    </s:if>

    <s:if test="profileList!= null && !profileList.isEmpty()">


        <button class="btn btn-default add_btn spacer spacer-bottom" data-toggle="modal" data-target="#add_dialog">Add Profile</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Add Profile</h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:form action="addProfileToUser">
                                <s:select name="profileId" list="profileList" headerKey="" headerValue="- Select One -"
                                          label="Profile" listKey="id" listValue="%{nm}"/>
                                <s:hidden name="user.id"/>
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

    </s:if>
    <s:else>
        <div class="error">There are no profiles defined (<a href="viewProfiles.action">View Profiles</a>).</div>
    </s:else>


</div>

</body>
</html>
