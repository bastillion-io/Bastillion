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
             $("#script_dia").dialog({
                autoOpen: false,
                height: 350,
                width: 350,
                modal: true,
                open: function(event, ui) {
                    $(".ui-dialog-titlebar-close").show();
                }
            });

            $("#view_btn").button().click(function () {
                $("#viewSystemsFilter").submit();
            });

            $("#script_btn").click(function() {
                $("#script_dia").dialog("open");
             });

            $(".select_frm_btn").button().click(function() {
                $("#select_frm").submit();
            });
            //select all check boxes
            $("#select_frm_systemSelectAll").click(function() {
                if ($(this).is(':checked')) {
                    $(".systemSelect").attr('checked', true);
                } else {
                    $(".systemSelect").attr('checked', false);
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


                $('.scrollableTable').tableScroll({height:500});
                $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
    });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function() {
                <s:if test="hostSystem.id>0">
                $("#edit_dialog_<s:property value="hostSystem.id"/>").dialog("open");
                </s:if>
                <s:else>
                $("#add_dialog").dialog("open");
                </s:else>
            });
        </script>
    </s:if>

    <title>KeyBox - Manage Systems</title>
</head>
<body>


    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        <s:form action="viewSystems" theme="simple">
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
            <s:hidden name="profileId"/>
            <s:if test="script!=null">
              <s:hidden name="script.id"/>
            </s:if>
        </s:form>

            <s:if test="script!=null">
                <h3>Execute Script on Systems</h3>
                Run <b>
                <a id="script_btn" href="#"><s:property value="script.displayNm"/></a></b> on the selected systems below

                <div id="script_dia" title="View Script">
                    <pre><s:property value="script.script"/></pre>
                </div>
            </s:if>
            <s:else>
                <h3>Composite SSH Terminals</h3>
                Select the systems below to generate composite SSH sessions in multiple terminals
            </s:else>

       <s:if test="profileList!= null && !profileList.isEmpty()">
        <table style="min-width:0px">
            <tr>
                <td class="align_left">
                    <s:form id="viewSystemsFilter" action="viewSystems" theme="simple">
                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <s:if test="script!=null">
                            <s:hidden name="script.id"/>
                        </s:if>
                        <table style="min-width:0px">
                        <tr>
                            <td style="padding-left:0px;">
                                <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SystemDB@FILTER_BY_PROFILE_ID}']" listKey="id" listValue="nm"
                                class="view_frm_select"
                                list="profileList"
                                headerKey=""
                                headerValue="-Select Profile-"/>
                            </td>
                            <td style="padding:5px 5px 0px 5px;">
                                <div id="view_btn" class="btn btn-default">Filter</div>
                            </td>
                        </tr>
                        </table>
                    </s:form>
                </td>
            </tr>
       </table>
       </s:if>
        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

  	        <s:form action="selectSystemsForCompositeTerms" id="select_frm" theme="simple">
  	             <s:if test="script!=null">
                        <s:hidden name="script.id"/>
                 </s:if>
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
	    </s:form>
        </s:if>
        <s:if test="script!=null && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="btn btn-default select_frm_btn">Execute Script</div>
        </s:if>
        <s:elseif test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="btn btn-default select_frm_btn">Create SSH Terminals</div>
        </s:elseif>
        <s:else>
            <div class="actionMessage">
                <p class="error">Systems not available
                    <s:if test="%{#session.userType==\"M\"}">
                    (<a href="../manage/viewSystems.action">Manage Systems</a>)
                    </s:if>.
                 </p>
                </div>
        </s:else>
    </div>


</body>
</html>
