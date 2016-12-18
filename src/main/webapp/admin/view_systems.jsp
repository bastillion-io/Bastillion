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
            $("#view_btn").button().click(function () {
                $("#viewSystems").submit();
            });

            $(".select_frm_btn").button().click(function() {
                $("#select_frm").submit();
            });
            //select all check boxes
            $("#select_frm_systemSelectAll").click(function() {
                if ($(this).is(':checked')) {
                    $(".systemSelect").prop('checked', true);
                } else {
                    $(".systemSelect").prop('checked', false);
                }
            });

            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewSystems_sortedSet_orderByField').attr('value', id);
                $("#viewSystems").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

    });
    </script>


    <title>KeyBox - Manage Systems</title>
</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">

            <s:if test="script!=null">
                <h3>Execute Script on Systems</h3>
                <p>Run <b> <a data-toggle="modal" data-target="#script_dialog"><s:property value="script.displayNm"/></a></b> on the selected systems below</p>

                <div id="script_dialog" class="modal fade">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                                <h4 class="modal-title">View Script: <s:property value="script.displayNm"/></h4>
                            </div>
                            <div class="modal-body">
                                <div class="row">
                                    <pre><s:property value="script.script"/></pre>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Close</button>

                            </div>
                        </div>
                    </div>
                </div>
            </s:if>
            <s:else>
                <h3>Composite SSH Terminals</h3>
                <p>Select the systems below to generate composite SSH sessions in multiple terminals</p>
            </s:else>

        <s:form id="viewSystems" action="viewSystems" theme="simple">
        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
        <s:hidden name="sortedSet.orderByDirection"/>
        <s:hidden name="sortedSet.orderByField"/>
        <s:if test="script!=null">
            <s:hidden name="script.id"/>
        </s:if>
        <s:if test="profileList!= null && !profileList.isEmpty()">
           <div>
                     <table>
                        <tr>
                            <td class="align_left">
                                <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SystemDB@FILTER_BY_PROFILE_ID}']" listKey="id" listValue="nm"
                                class="view_frm_select"
                                list="profileList"
                                headerKey=""
                                headerValue="-Select Profile-"/>
                            </td>
                            <td>
                                <div id="view_btn" class="btn btn-default">Filter</div>
                            </td>
                        </tr>
                     </table>
           </div>
        </s:if>
        </s:form>
        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

  	        <s:form action="selectSystemsForCompositeTerms" id="select_frm" theme="simple">
                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
  	             <s:if test="script!=null">
                        <s:hidden name="script.id"/>
                 </s:if>
                <div class="scrollWrapper">
                <table class="table-striped scrollableTable">
                    <thead>

                    <tr>
                        <th><s:checkbox name="systemSelectAll" cssClass="systemSelect"
                                            theme="simple"/></th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Display
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host
                        </th>
                    </tr>
                    </thead>
                    <tbody>

                    <s:iterator var="system" value="sortedSet.itemList" status="stat">
                        <tr>

                            <td>
                                <s:checkboxlist name="systemSelectId" list="#{id:''}" cssClass="systemSelect"
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
        </s:if>
        <s:if test="script!=null && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="btn btn-default select_frm_btn spacer spacer-bottom">Execute Script</div>
        </s:if>
        <s:elseif test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="btn btn-default select_frm_btn spacer spacer-bottom">Create SSH Terminals</div>
        </s:elseif>
        <s:else>
            <div class="actionMessage">
                <p class="error">Systems not available
                    <s:if test="%{#session.userType==\"M\"}">
                    (<a href="../manage/viewSystems.action?_csrf=<s:property value="#session['_csrf']"/>">Manage Systems</a>)
                    </s:if>.
                 </p>
                </div>
        </s:else>
    </div>


</body>
</html>
