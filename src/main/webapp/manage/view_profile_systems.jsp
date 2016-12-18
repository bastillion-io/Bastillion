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

            //open add dialog
            $("#assign_sys").button().click(function() {
                $('#assignSystemsToProfile').submit();
            });

            //select all check boxes
            $("#assignSystemsToProfile_systemSelectAll").click(function() {

                if ($(this).is(':checked')) {
                    $(".systemSelect").prop('checked', true);
                } else {
                    $(".systemSelect").prop('checked', false);
                }
            });

            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewProfileSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewProfileSystems_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewProfileSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewProfileSystems_sortedSet_orderByField').attr('value', id);
                $("#viewProfileSystems").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

            <s:if test="profile.hostSystemList!= null && !profile.hostSystemList.isEmpty()">
            <s:iterator var="system" value="profile.hostSystemList" status="stat">
            $(':checkbox[value=<s:property value="id"/>]').prop('checked', true);
            </s:iterator>
            </s:if>


        });
    </script>

    <title>KeyBox - Assign Systems to Profile</title>

</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">

       <s:form action="viewProfileSystems">
           <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
            <s:hidden name="profile.id"/>
        </s:form>

        <h3>Assign Systems to Profile</h3>

        <p>Select the systems below to be assigned to the current profile.</p>

        <h4><s:property value="profile.nm"/></h4>
        <p class="small"><s:property value="profile.desc"/></p>


        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <s:form action="assignSystemsToProfile" theme="simple">
                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                <s:hidden name="profile.id"/>

           <div class="scrollWrapper">
                <table class="table-striped scrollableTable">
                    <thead>

                    <tr>
                        <th><s:checkbox name="systemSelectAll" cssClass="systemSelect" theme="simple"/></th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Display Name</th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User</th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host</th>
                    </tr>
                    </thead>
                    <tbody>




                    <s:iterator var="system" value="sortedSet.itemList" status="stat">
                        <tr>
                            <td>
                                <s:checkboxlist id="systemSelectId_%{id}" list="#{id:''}" name="systemSelectId" cssClass="systemSelect"
                                            theme="simple"/>
                            </td>
                            <td>
                                <s:property value="displayNm"/>

                            </td>
                            <td><s:property value="user"/></td>
                            <td><s:property value="host"/>:<s:property value="port"/></td>
                        </tr>

                    </s:iterator>
                    </tbody>
                </table>
               </div>
            </s:form>
            <div id="assign_sys" class="btn btn-default assign_sys_btn spacer spacer-bottom">Assign</div>
        </s:if>
        <s:else>
            <div class="error">There are no systems defined (<a href="viewSystems.action?_csrf=<s:property value="#session['_csrf']"/>">Manage Systems</a>).</div>
        </s:else>

    </div>

</body>
</html>
