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


    <title>KeyBox - Upload &amp; Push</title>

    <script type="text/javascript">
        $(document).ready(function() {
            $("#error_dialog").dialog({
                autoOpen: false,
                height: 175,
                width: 400,
                modal: true
            });

            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $('#push').submit();
            });

            $('.scrollableTable').tableScroll({height:150, width:435});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
            <s:if test="pendingSystemStatus!=null">
            //set scroll
            var container = $('.tablescroll_wrapper'), scrollTo = $('#status_<s:property value="pendingSystemStatus.hostSystem.id"/>');
            container.scrollTop(scrollTo.offset().top - container.offset().top + container.scrollTop() - 55);
            </s:if>
            <s:if test="currentSystemStatus!=null && currentSystemStatus.statusCd==\"F\"">
            $("#error_dialog").dialog("open");
            </s:if>
            <s:elseif test="pendingSystemStatus!=null">
                $('#push').submit();
            </s:elseif>


        });
    </script>


</head>
<body style="background: #FFFFFF">

<h4>
    Pushing File: <s:property value="uploadFileName"/>
</h4>


<s:if test="systemStatusList!= null && !systemStatusList.isEmpty()">
    <table class="vborder scrollableTable">
        <thead>

        <tr>


            <th>Display Name</th>
            <th>User</th>
            <th>Host</th>

            <th>Status</th>

        </tr>
        </thead>
        <tbody>

        <s:iterator value="systemStatusList" status="stat">
            <tr>

                <td>
                    <div id="status_<s:property value="hostSystem.id"/>"><s:property
                            value="hostSystem.displayNm"/></div>
                </td>
                <td><s:property value="hostSystem.user"/></td>
                <td><s:property value="hostSystem.host"/>:<s:property value="hostSystem.port"/></td>

                <td>
                    <s:if test="statusCd==\"I\"">
                        <div class="warning"> Not Started</div>
                    </s:if>
                    <s:elseif test="statusCd==\"P\"">
                        <div class="warning">In Progress</div>
                    </s:elseif>
                    <s:elseif test="statusCd==\"A\"">
                        <div class="warning">Authentication Failed</div>
                    </s:elseif>
                    <s:elseif test="statusCd==\"F\"">
                        <div class="error">Failed</div>
                    </s:elseif>
                    <s:elseif test="statusCd==\"T\"">
                        <div class="error">Timeout</div>
                    </s:elseif>
                    <s:elseif test="statusCd==\"S\"">
                        <div class="success">Success</div>
                    </s:elseif>
                </td>

            </tr>

        </s:iterator>
        </tbody>
    </table>
</s:if>
<s:else>
    <p class="error">No systems associated with upload</p>
</s:else>

<s:form action="push" method="post">
    <s:hidden name="pushDir"/>
    <s:hidden name="uploadFileName"/>
</s:form>

<div id="error_dialog" title="Error">
    <p class="error">Error: <s:property value="currentSystemStatus.errorMsg"/></p>

    <p>System: <s:property value="currentSystemStatus.hostSystem.displayLabel"/>

    </p>


    <div class="submit_btn">OK</div>
</div>

</body>
</html>