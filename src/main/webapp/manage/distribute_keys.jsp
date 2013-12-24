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
            $(".submit_btn").button().click(function () {
                $(this).prev().submit();
            });

            //select all check boxs
            $("#select_frm_systemSelectAll").click(function () {

                if ($(this).is(':checked')) {
                    $(".systemSelect").attr('checked', true);
                } else {
                    $(".systemSelect").attr('checked', false);
                }
            });

            $(".sort,.sortAsc,.sortDesc").click(function () {
                var id = $(this).attr('id');

                if ($('#distributeKeysBySystem_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#distributeKeysBySystem_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#distributeKeysBySystem_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#distributeKeysBySystem_sortedSet_orderByField').attr('value', id);
                $("#distributeKeysBySystem").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>


            $('.scrollableTable').tableScroll({height: 500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");


        });

    </script>


    <title>KeyBox - Distribute Keys</title>

</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <s:if test="sortedSet.itemList!= null">

            <h3>Distribute Keys By System</h3>

            <ul class="top_nav">
                <li class="top_nav_item"><a href="distributeKeysByProfile.action">Distribute Keys by Profile</a></li>
                <li class="top_nav_item"><a href="distributeKeysBySystem.action">Distribute Keys by System</a></li>
            </ul>
            <div class="clear"></div>




            <s:if test="sortedSet.itemList.isEmpty()">
                <p>
                <div class="error">There are no systems defined.</div>
                </p>
            </s:if>
            <s:else>

                <p>Select the systems below to distribute SSH public keys</p>


                <s:form action="distributeKeysBySystem">
                    <s:hidden name="sortedSet.orderByDirection"/>
                    <s:hidden name="sortedSet.orderByField"/>
                </s:form>

                <s:form action="selectSystemsForAuthKeys" id="select_frm" theme="simple">

                    <table class="vborder scrollableTable">
                        <thead>
                        <tr>

                            <th><s:checkbox name="systemSelectAll" cssClass="systemSelect"
                                            theme="simple"/></th>


                            <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">
                                Display
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
                <div class="submit_btn">Distribute Keys</div>
            </s:else>


        </s:if>
        <s:else>
            <h3>Distribute Keys By Profile</h3>
            <ul class="top_nav">
                <li class="top_nav_item"><a href="distributeKeysByProfile.action">Distribute Keys by Profile</a></li>
                <li class="top_nav_item"><a href="distributeKeysBySystem.action">Distribute Keys by System</a></li>
            </ul>
            <div class="clear"></div>

            <p>Select a profile below to distribute SSH public keys</p>


            <s:form action="selectProfileForAuthKeys">
                <s:select name="publicKey.profile.id" list="profileList" headerKey="" headerValue="All Systems"
                          listKey="id" listValue="%{nm}" label="Profile" value="%{profile.id}"/>


            </s:form>
            <div class="submit_btn">Distribute Keys</div>

        </s:else>


    </div>
</div>
</body>
</html>
