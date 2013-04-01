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

            $(".run_cmd_dialog").dialog({
                autoOpen: false,
                height: 150,
                width: 325,
                modal: false
            });
            $("#set_password_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 400,
                modal: true
            });
            $("#error_dialog").dialog({
                autoOpen: false,
                height: 200,
                width: 400,
                modal: true
            });

             $(".enter_btn").button().click(function() {
                  $(this).prev().submit();
             });

            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).prev().submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function() {
                $("#set_password_dialog").dialog("close");
                window.location = 'getNextPendingSystemForTerms.action?pendingSystemStatus.id=<s:property value="pendingSystemStatus.id"/>&script.id=<s:if test="script!=null"><s:property value="script.id"/></s:if>';

            });

            //function for command keys (ie ESC, CTRL, etc..)
            var keys = {};
            $(".runCmd_command").keydown(function(e) {
                keys[e.which] = true;
                var c = String.fromCharCode(e.which);
                if (keys[27] || keys[37] || keys[38] || keys[39] || keys[40] || keys[17]) {
                    var id = $(this).attr("id").replace("runCmd_command_", "");
                    if (id == 'all') {
                        $.ajax({ url: 'runCmd.action?keyCode=' + e.which});
                    } else {
                        $.ajax({ url: 'runCmd.action?keyCode=' + e.which + '&id=' + id});
                    }
                    $('#runCmd_command_' + id).val('');
                }

            });
            $(document).keyup(function (e) {
                delete keys[e.which];
            });

            //run command
            $('.runCmd').submit(function() {
                var id = $(this).attr("id").replace("runCmd_", "");
                if (id == 'all') {
                    $.ajax({ url: 'runCmd.action?command=' + $('#runCmd_command_' + id).val()});
                }
                else {
                    $.ajax({ url: 'runCmd.action?command=' + $('#runCmd_command_' + id).val() + '&id=' + id});
                }
                $('#runCmd_command_' + id).val('');
                return false;

            });

            //if terminal window clicked show terminal prompt
            $(".scroll").click(function() {
                var id = $(this).attr("id").replace("scroll_", "");
                $("#run_cmd_dialog_" + id).dialog("open");
            });

            <s:if test="pendingSystemStatus!=null">
                <s:if test="pendingSystemStatus.statusCd==\"A\"">
                    $("#set_password_dialog").dialog("open");
                </s:if>
                <s:else>
                    <s:if test="currentSystemStatus==null||currentSystemStatus.statusCd==\"P\" ||currentSystemStatus.statusCd!=\"F\"">
                        setInterval(function() {
                            $("#composite_terms_frm").submit();
                        }, 2000);
                    </s:if>
                </s:else>
            </s:if>


            <s:if test="currentSystemStatus!=null">
                <s:if test="currentSystemStatus.statusCd==\"F\"">
                $("#error_dialog").dialog("open");
                </s:if>
            </s:if>

            <s:if test="pendingSystemStatus==null && (currentSystemStatus==null||currentSystemStatus.statusCd==\"S\")">
                  setInterval(function() {
                        $.getJSON('getOutputJSON.action', function(data) {
                        var append = false;
                        $.each(data, function(key, val) {
                            if(val.output!='') {
                                $('#output_' + val.sessionId).append(val.output);
                                $('#scroll_'+ val.sessionId).scrollTop($("#output_" +  val.sessionId).height());
                            }
                        });
                        //scroll to bottom
                    });
                    }, 750);
            </s:if>

        });
    </script>


    <title>KeyBox - Terms</title>

</head>
<body>

<div class="page">

  <s:if test="schSessionMap!= null && !schSessionMap.isEmpty()">
    <div class="content">
        <ul class="top_nav">
            <li class="top_nav_item">
                <s:form id="runCmd_all" action="runCmd" cssClass="runCmd" theme="simple">
                    #&nbsp;<s:textfield id="runCmd_command_all" name="command" theme="simple" cssClass="runCmd_command" size="35"/>
                    <div id="enter_btn" class="enter_btn" >Enter</div>
                </s:form>
            </li>
            <li class="top_nav_item"><a href="/manage/exitTerms.action">Exit Terminals</a></li>
        </ul>
        <div class="clear"></div>
        <br/>


        <div class="scrollwrapper">
            <s:iterator value="schSessionMap">
                <div>

                    <h4><s:property value="value.hostSystem.displayLabel"/></h4>

                    <div id="scroll_<s:property value="key"/>" class="scroll">
                        <pre id="output_<s:property value="key"/>" class="output"></pre>
                    </div>

                    <div id="run_cmd_dialog_<s:property value="key"/>" title="Run Command" class="run_cmd_dialog">
                        <h4><s:property value="value.hostSystem.displayLabel"/></h4>
                        <s:form id="runCmd_%{key}" action="runCmd" cssClass="runCmd" theme="simple">
                            #&nbsp;<s:textfield id="runCmd_command_%{key}" name="command" theme="simple"
                                                cssClass="runCmd_command" size="35"/>
                            <div id="enter_btn" class="enter_btn" >Enter</div>
                        </s:form>
                    </div>

                </div>
            </s:iterator>
        </div>


        <div id="set_password_dialog" title="Set Password">
            <p class="error"><s:property value="pendingSystemStatus.errorMsg"/></p>

            <p>Set password for <s:property value="pendingSystemStatus.hostSystem.displayLabel"/>

            </p>
            <s:form id="password_frm" action="createTerms">
                <s:hidden name="pendingSystemStatus.id"/>
                <s:password name="password" label="Password" size="15" value="" autocomplete="off"/>
                <s:if test="script!=null">
                    <s:hidden name="script.id"/>
                </s:if>
            </s:form>
            <div class="submit_btn">Submit</div>
            <div class="cancel_btn">Cancel</div>
        </div>


        <div id="error_dialog" title="Error">
            <p class="error">Error: <s:property value="currentSystemStatus.errorMsg"/></p>

            <p>System: <s:property value="currentSystemStatus.hostSystem.displayLabel"/>

            </p>

            <s:form id="error_frm" action="createTerms">
                <s:hidden name="pendingSystemStatus.id"/>
                   <s:if test="script!=null">
                    <s:hidden name="script.id"/>
                </s:if>
            </s:form>
            <div class="submit_btn">OK</div>
        </div>


        <s:form id="composite_terms_frm" action="createTerms">
            <s:hidden name="pendingSystemStatus.id"/>
            <s:if test="script!=null">
                <s:hidden name="script.id"/>
            </s:if>
        </s:form>

    </div>
     </s:if>
        <s:else>
         <jsp:include page="../_res/inc/navigation.jsp"/>

            <div class="content">
            <p class="error">No systems have been selected.</p>
            </div>
        </s:else>

</div>
</body>
</html>
