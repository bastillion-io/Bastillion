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


            $(".terminals_btn").button().click(function () {
                var id = $(this).attr('id').replace("terminals_btn_", "");
                window.location = 'getTermsForSession.action?sessionId=' + id + '&_csrf=<s:property value="#session['_csrf']"/>';
            });

            $("#view_btn").button().click(function () {
                $("#viewSessions").submit();
            });

            //submit add or edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('form:first').submit();
            });

            $(".sort,.sortAsc,.sortDesc").click(function () {
                var id = $(this).attr('id')

                if ($('#viewSessions_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewSessions_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewSessions_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewSessions_sortedSet_orderByField').attr('value', id);
                $("#viewSessions").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>



        });
    </script>


    <title>KeyBox - Audit Sessions</title>
</head>
<body>


<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">


    <h3>Audit Sessions</h3>

           Audit sessions below
                <table>
                    <tr>
                        <td class="align_left">
                            <s:form id="viewSessions" action="viewSessions" theme="simple">
                                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                <s:hidden name="sortedSet.orderByDirection"/>
                                <s:hidden name="sortedSet.orderByField"/>
                                <table>
                                <tr>
                                    <s:if test="userList!= null && !userList.isEmpty()">
                                        <td class="align_left">
                                           <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SessionAuditDB@FILTER_BY_USER_ID}']" listKey="id" listValue="username"
                                           class="view_frm_select"
                                            list="userList"
                                            headerKey=""
                                            headerValue="-Select User-"/>
                                        </td>
                                     </s:if>
                                    <s:if test="systemList!= null && !systemList.isEmpty()">
                                            <td class="align_left">
                                            <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SessionAuditDB@FILTER_BY_SYSTEM_ID}']" listKey="id" listValue="displayLabel"
                                            class="view_frm_select"
                                            list="systemList"
                                            headerKey=""
                                            headerValue="-Select System-"/>
                                        </td>
                                    </s:if>
                                    <td style="padding:5px 5px 0px 5px;">
                                        <div id="view_btn" class="btn btn-default">Filter</div>
                                    </td>
                                </tr>
                                </table>
                            </s:form>
                        </td>
                    </tr>
               </table>
        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

    <div class="scrollWrapper">
            <table class="table-striped scrollableTable" >
                <thead>
                <tr>


                    <th id="<s:property value="@com.keybox.manage.db.SessionAuditDB@SORT_BY_USERNAME"/>" class="sort">
                        Username
                    </th>

                    <th id="<s:property value="@com.keybox.manage.db.SessionAuditDB@SORT_BY_LAST_NM"/>" class="sort">
                        Last Name
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.SessionAuditDB@SORT_BY_FIRST_NM"/>" class="sort">
                        First Name
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.SessionAuditDB@SORT_BY_SESSION_TM"/>" class="sort">
                        Session Time
                    </th>
                    <th>&nbsp;</th>

                </tr>
                </thead>
                <tbody>
                <s:iterator var="session" value="sortedSet.itemList" status="stat">
                    <tr>

                        <td><s:property value="user.username"/></td>
                        <td><s:property value="user.lastNm"/></td>
                        <td><s:property value="user.firstNm"/></td>
                        <td><s:date name="sessionTm"/></td>
                        <td>
                            <div id="terminals_btn_<s:property value='id'/>" class="btn btn-default terminals_btn">
                                Audit
                            </div>
                        </td>


                    </tr>

                </s:iterator>
                </tbody>
            </table>
        </div>
        </s:if>
        <s:else>
            <p class="error">No session audits available</p>
        </s:else>


</div>


</body>
</html>
