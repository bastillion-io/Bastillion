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

            $("#error_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 500,
                modal: true
            });
            $("#set_password_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 500,
                modal: true
            });
             $("#set_passphrase_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 500,
                modal: true
            });
            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).prev().submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function() {
                $("#set_password_dialog").dialog("close");
                window.location = 'getNextPendingSystem.action?pendingSystemStatus.id=<s:property value="pendingSystemStatus.id"/>';

            });
            $('.scrollableTable').tableScroll({height:450});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");



             <s:if test="currentSystemStatus!=null && currentSystemStatus.statusCd=='GENERICFAIL'">
                $("#error_dialog").dialog("open");
            </s:if>
            <s:elseif test="pendingSystemStatus!=null">
                //set scroll
                var container = $('.tablescroll_wrapper'), scrollTo = $('#status_<s:property value="pendingSystemStatus.hostSystem.id"/>');
                container.scrollTop(scrollTo.offset().top - container.offset().top + container.scrollTop()-225);
                <s:if test="pendingSystemStatus.statusCd=='AUTHFAIL'">
                    $("#set_password_dialog").dialog("open");
                </s:if>
                <s:elseif test="pendingSystemStatus.statusCd=='KEYAUTHFAIL'">
                    $("#set_passphrase_dialog").dialog("open");
                </s:elseif>
                <s:else>
                    $("#gen_key_frm").submit();
                </s:else>
            </s:elseif>



        });
    </script>


    <title>KeyBox - Key Generation Status</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <h3>System - Key Generation Status</h3>

        <p>The status of the authorized keys placement is displayed below</p>


        <s:if test="systemStatusList!= null && !systemStatusList.isEmpty()">
            <table class="vborder scrollableTable">
                <thead>

                <tr>


                    <th>Display Name</th>
                    <th>User</th>
                    <th>Host</th>
                    <th>Authorized Keys</th>
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
                        <td><s:property value="hostSystem.authorizedKeys"/></td>
                        <td>
                            <s:if test="statusCd=='INITIAL'">
                                <div class="warning">Not Started</div>
                            </s:if>
                            <s:elseif test="statusCd=='AUTHFAIL'">
                                <div class="warning">Authentication Failed</div>
                            </s:elseif>
                            <s:elseif test="statusCd=='KEYAUTHFAIL'">
                                <div class="warning">Passphrase Authentication Failed</div>
                            </s:elseif>
                            <s:elseif test="statusCd=='GENERICFAIL'">
                                <div class="error">Failed</div>
                            </s:elseif>
                            <s:elseif test="statusCd=='SUCCESS'">
                                <div class="success">Success</div>
                            </s:elseif>
                        </td>

                    </tr>

                </s:iterator>
                </tbody>
            </table>
        </s:if>
        <s:else>
            <p class="error">No authorized keys have been associated to system or user.</p>
        </s:else>


        <div id="set_password_dialog" title="Enter Password">
            <p class="error"><s:property value="pendingSystemStatus.errorMsg"/></p>

            <p>Enter password for <s:property value="pendingSystemStatus.hostSystem.displayLabel"/>

            </p>
            <s:form id="password_frm" action="genAuthKeyForSystem">
                <s:hidden name="pendingSystemStatus.id"/>
                <s:password name="password" label="Password" size="15" value="" autocomplete="off"/>
            </s:form>
            <div class="submit_btn">Submit</div>
            <div class="cancel_btn">Cancel</div>
        </div>

        <div id="set_passphrase_dialog" title="Enter Passphrase">
            <p class="error"><s:property value="pendingSystemStatus.errorMsg"/></p>
            <s:form id="passphrase_frm" action="genAuthKeyForSystem">
                <s:hidden name="pendingSystemStatus.id"/>
                <s:password name="passphrase" label="Passphrase" size="15" value="" autocomplete="off"/>
            </s:form>
            <div class="submit_btn">Submit</div>
            <div class="cancel_btn">Cancel</div>
        </div>

        <div id="error_dialog" title="Error">
            <p class="error">Error: <s:property value="currentSystemStatus.errorMsg"/></p>

            <p>System: <s:property value="currentSystemStatus.hostSystem.displayLabel"/>

            </p>

            <s:form id="error_frm" action="genAuthKeyForSystem">
                <s:hidden name="pendingSystemStatus.id"/>
            </s:form>
            <div class="submit_btn">OK</div>
        </div>

        <s:form id="gen_key_frm" action="genAuthKeyForSystem">
            <s:hidden name="pendingSystemStatus.id"/>
        </s:form>


    </div>
</div>

</body>
</html>
