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



             $(".terminals_btn").button().click(function() {
                var id = $(this).attr('id').replace("terminals_btn_", "");
                window.location = 'getTermsForSession.action?sessionId='+ id;
             });

             //submit add or edit form
             $(".submit_btn").button().click(function() {
                $(this).parents('form:first').submit();
             });

            $(".sort,.sortAsc,.sortDesc").click(function() {
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


                $('.scrollableTable').tableScroll({height:500});
                $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
    });
    </script>


    <title>KeyBox - Audit Sessions</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">


            <s:form action="viewSessions" theme="simple">
                <s:hidden name="sortedSet.orderByDirection"/>
                <s:hidden name="sortedSet.orderByField"/>
            </s:form>

            <h3>Audit Sessions</h3>

            <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

                <p>Select a session to audit below</p>


                <table class="vborder scrollableTable">
                    <thead>
                    <tr>


                        <th id="<s:property value="@com.keybox.manage.db.SessionAuditDB@SORT_BY_USERNAME"/>" class="sort">Username</th>

                        <th id="<s:property value="@com.keybox.manage.db.SessionAuditDB@SORT_BY_LAST_NM"/>" class="sort">Last Name</th>
                        <th id="<s:property value="@com.keybox.manage.db.SessionAuditDB@SORT_BY_FIRST_NM"/>" class="sort">First Name</th>
                        <th id="<s:property value="@com.keybox.manage.db.SessionAuditDB@SORT_BY_SESSION_TM"/>" class="sort">Session Time</th>
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
                            <div id="terminals_btn_<s:property value='id'/>" class="terminals_btn">Audit</div>
                            </td>


                        </tr>

                    </s:iterator>
                    </tbody>
                </table>
                </s:if>
                <s:else>
                  <p class="error">No session audits available</p>
                </s:else>

    </div>
</div>

</body>
</html>
