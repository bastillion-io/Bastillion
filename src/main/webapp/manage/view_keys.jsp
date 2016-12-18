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

            $("#view_btn").button().click(function () {
                $("#viewKeys").submit();
            });

            //call delete action
            $(".disable_btn").button().click(function () {
                var id = $(this).attr('id').replace("disable_btn_", "");
                window.location = 'disablePublicKey.action?publicKey.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>&_csrf=<s:property value="#session['_csrf']"/>'
                +'&sortedSet.filterMap[\'user_id\']=<s:property value="sortedSet.filterMap['user_id']"/>'
                +'&sortedSet.filterMap[\'profile_id\']=<s:property value="sortedSet.filterMap['profile_id']"/>'
                +'&sortedSet.filterMap[\'enabled\']=<s:property value="sortedSet.filterMap['enabled']"/>';


            });
            
            $(".enable_btn").button().click(function () {
                var id = $(this).attr('id').replace("enable_btn_", "");
                window.location = 'enablePublicKey.action?publicKey.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>&_csrf=<s:property value="#session['_csrf']"/>'
                +'&sortedSet.filterMap[\'user_id\']=<s:property value="sortedSet.filterMap['user_id']"/>'
                +'&sortedSet.filterMap[\'profile_id\']=<s:property value="sortedSet.filterMap['profile_id']"/>'
                +'&sortedSet.filterMap[\'enabled\']=<s:property value="sortedSet.filterMap['enabled']"/>';

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


           
        });
    </script>


    <title>KeyBox - View / Disable SSH Keys</title>

</head>
<body>


<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">

    <h3>View / Disable SSH Keys</h3>

    <p>Disabling will remove the public key from all host systems. Additional SSH keys with the same fingerprint can no longer be set forcing users to rotate keys.</p>
    <table>
        <tr>
            <td class="align_left">
                <s:form id="viewKeys" action="viewKeys" theme="simple">
                    <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                    <s:hidden name="sortedSet.orderByDirection"/>
                    <s:hidden name="sortedSet.orderByField"/>

                    <table>
                        <tr>
                            <td class="align_left"><a href="../admin/viewKeys.action?_csrf=<s:property value="#session['_csrf']"/>" class="btn btn-success">Add / Remove Keys</a></td>
                            <td>|</td>
                            <s:if test="userList!= null && !userList.isEmpty()">
                                <td >
                                    <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.PublicKeyDB@FILTER_BY_USER_ID}']" listKey="id" listValue="username"
                                              class="view_frm_select"
                                              list="userList"
                                              headerKey=""
                                              headerValue="-Select User-"/>
                                </td>
                            </s:if>
                            <s:if test="profileList!= null && !profileList.isEmpty()">
                                <td>
                                    <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.PublicKeyDB@FILTER_BY_PROFILE_ID}']" listKey="id" listValue="nm"
                                              class="view_frm_select"
                                              list="profileList"
                                              headerKey=""
                                              headerValue="-Select Profile-"/>
                                </td>
                            </s:if>
                            <td>
                            <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.PublicKeyDB@FILTER_BY_ENABLED}']"
                                      class="view_frm_select"
                                      list="#{true:'Enabled', false:'Disabled'}"/>
                            </td>
                            <td>
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

                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_KEY_NM"/>" class="sort">Key
                        Name
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_USERNAME"/>" class="sort">
                        Username
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_PROFILE"/>" class="sort">
                        Profile
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_TYPE"/>" class="sort">
                        Type
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_FINGERPRINT"/>" class="sort">
                        Fingerprint
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_CREATE_DT"/>" class="sort">
                        Created
                    </th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>
                <s:iterator var="publicKey" value="sortedSet.itemList" status="stat">
                    <tr>
                        <td><s:property value="keyNm"/></td>
                        <td><s:property value="username"/></td>
                        <td>
                            <s:if test="%{#publicKey.profile==null}">
                                All Systems
                            </s:if>
                            <s:else>
                                <s:property value="profile.nm"/>
                            </s:else>
                        </td>
                        <td>[ <s:property value="type"/> ]</td>
                        <td><s:property value="fingerprint"/></td>
                        <td><s:date name="createDt" nice="true"/></td>
                        <td>
                            <div>
                                <s:if test="%{enabled}">
                                    <button class="btn btn-default btn-danger spacer spacer-left disable_btn" data-toggle="modal"
                                            id="disable_btn_<s:property value="id"/>">Disable
                                    </button>
                                </s:if>
                                <s:else>
                                    <button class="btn btn-default btn-success spacer spacer-left enable_btn" data-toggle="modal"
                                            id="enable_btn_<s:property value="id"/>">Enable
                                    </button>
                                </s:else>
                                <div style="clear:both"></div>
                            </div>
                        </td>
                    </tr>
                </s:iterator>
                </tbody>
            </table>
            </div>
        </s:if>

</div>

</body>
</html>
