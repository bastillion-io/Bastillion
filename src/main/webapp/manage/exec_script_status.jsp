<%
/**
 * Copyright (c) 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 * Licensed under the MIT license: http://www.opensource.org/licenses/mit-license.php
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
                height: 175,
                width: 350,
                modal: true
            });
            $(".output_dialog").dialog({
                autoOpen: false,
                height: 550,
                width: 600,
                modal: true
            });
            $("#set_password_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 350,
                modal: true
            });
            //open output dialog
            $(".output_btn").button().click(function() {
                //get dialog id to open
                var id = $(this).attr('id').replace("output_btn_", "");
                $("#output_dialog_" + id).dialog("open");

            });
            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).prev().submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function() {
                $("#set_password_dialog").dialog("close");
                window.location = 'getNextPendingSystemForExecScript.action?pendingSystemStatus.id=<s:property value="pendingSystemStatus.id"/>&script.id=<s:property value="script.id"/>';

            });
            $('.scrollableTable').tableScroll({height:450});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");


            <s:if test="pendingSystemStatus!=null">
            //set scroll
            var container = $('.tablescroll_wrapper'), scrollTo = $('#status_<s:property value="pendingSystemStatus.hostSystem.id"/>');
            container.scrollTop(scrollTo.offset().top - container.offset().top + container.scrollTop()-225);


            <s:if test="pendingSystemStatus.statusCd==\"A\"">
            $("#set_password_dialog").dialog("open");
            </s:if>
            <s:else>
                <s:if test="currentSystemStatus==null||currentSystemStatus.statusCd==\"P\" ||currentSystemStatus.statusCd!=\"F\"">
                setInterval(function(){$("#exec_script_frm").submit();},2000);

                </s:if>
            </s:else>
            </s:if>
            <s:if test="currentSystemStatus!=null">
            <s:if test="currentSystemStatus.statusCd==\"F\"">
            $("#error_dialog").dialog("open");
            </s:if>
            </s:if>


        });
    </script>


    <title>KeyBox - Execute Script Status</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <h3>System - Execute Script Status</h3>

        <p>The status of the script execution is displayed below</p>


        <s:if test="systemStatusList!= null && !systemStatusList.isEmpty()">
            <table class="vborder scrollableTable">
                <thead>

                <tr>


                    <th>Display Name</th>
                    <th>User</th>
                    <th>Host</th>
                    <th>Status</th>
                    <th>Output</th>

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
                        <td>
                            <s:if test="statusCd==\"S\"||statusCd==\"T\"">
                            <div id="output_btn_<s:property value="id"/>" class="output_btn" style="float:left">
                            View Output
                            </div></td>
                            </s:if>

                    </tr>

                </s:iterator>
                </tbody>
            </table>
        </s:if>
        <s:else>
            <p class="error">No systems have been selected</p>
        </s:else>

                <s:iterator value="systemStatusList" status="stat">
                   <div id="output_dialog_<s:property value="id"/>" title="Output" class="output_dialog">
                   <pre><s:property value="output"/></pre>
                    </div>
                </s:iterator>




        <div id="set_password_dialog" title="Set Password">
            <p class="error"><s:property value="pendingSystemStatus.errorMsg"/></p>

            <p>Set password for <s:property value="pendingSystemStatus.hostSystem.displayLabel"/>

            </p>
            <s:form id="password_frm" action="genExecScriptForSystem">
                <s:hidden name="pendingSystemStatus.id"/>
                <s:password name="password" label="Password" size="15" value="" autocomplete="off"/>
                <s:hidden name="script.id"/>
            </s:form>
            <div class="submit_btn">Submit</div>
            <div class="cancel_btn">Cancel</div>
        </div>


        <div id="error_dialog" title="Error">
            <p class="error">Error: <s:property value="currentSystemStatus.errorMsg"/></p>

            <p>System: <s:property value="currentSystemStatus.hostSystem.displayLabel"/>

            </p>

            <s:form id="error_frm" action="genExecScriptForSystem">
                <s:hidden name="pendingSystemStatus.id"/>
                <s:hidden name="script.id"/>
            </s:form>
            <div class="submit_btn">OK</div>
        </div>

        <s:form id="exec_script_frm" action="genExecScriptForSystem">
            <s:hidden name="pendingSystemStatus.id"/>
            <s:hidden name="script.id"/>
        </s:form>


    </div>
</div>

</body>
</html>
