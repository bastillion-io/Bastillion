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


    $('#dummy').focus();

    $("#set_password_dialog").dialog({
        autoOpen: false,
        height: 225,
        minWidth: 550,
        modal: true
    });
    $("#set_passphrase_dialog").dialog({
        autoOpen: false,
        height: 200,
        minWidth: 550,
        modal: true
    });
    $("#error_dialog").dialog({
        autoOpen: false,
        height: 225,
        minWidth: 550,
        modal: true
    });
    $("#upload_push_dialog").dialog({
        autoOpen: false,
        height: 375,
        minWidth: 725,
        modal: true,
        open: function (event, ui) {
            $(".ui-dialog-titlebar-close").show();
        },
        close: function () {
            $("#upload_push_frame").attr("src", "");
        }
    });

    $(".termwrapper").sortable({
        helper: 'clone'
    });

    //submit add or edit form
    $(".submit_btn").button().click(function () {
        <s:if test="pendingSystemStatus!=null">
        $(this).parents('form:first').submit();
        </s:if>
        $("#error_dialog").dialog("close");
    });
    //close all forms
    $(".cancel_btn").button().click(function () {
        $("#set_password_dialog").dialog("close");
        window.location = 'getNextPendingSystemForTerms.action?pendingSystemStatus.id=<s:property value="pendingSystemStatus.id"/>&script.id=<s:if test="script!=null"><s:property value="script.id"/></s:if>';

    });


    //if terminal window toggle active for commands
    $(".run_cmd").click(function () {

        //check for cmd-click / ctr-click
        if (!keys[17] && !keys[91] && !keys[93] && !keys[224]) {
            $(".run_cmd").removeClass('run_cmd_active');
        }

        if ($(this).hasClass('run_cmd_active')) {
            $(this).removeClass('run_cmd_active');
        } else {
            $(this).addClass('run_cmd_active')
        }

    });

    $('#select_all').click(function () {
        $(".run_cmd").addClass('run_cmd_active');
    });


    $('#upload_push').click(function () {


        //get id list from selected terminals
        var ids = [];
        $(".run_cmd_active").each(function (index) {
            var id = $(this).attr("id").replace("run_cmd_", "");
            ids.push(id);
        });
        var idListStr = '?action=upload';
        ids.forEach(function (entry) {
            idListStr = idListStr + '&idList=' + entry;
        });

        $("#upload_push_frame").attr("src", "setUpload.action" + idListStr);
        $("#upload_push_dialog").dialog("open");


    });


    <s:if test="currentSystemStatus!=null && currentSystemStatus.statusCd=='GENERICFAIL'">
    $("#error_dialog").dialog("open");
    </s:if>
    <s:elseif test="pendingSystemStatus!=null">
    <s:if test="pendingSystemStatus.statusCd=='AUTHFAIL'">
    $("#set_password_dialog").dialog("open");
    </s:if>
    <s:elseif test="pendingSystemStatus.statusCd=='KEYAUTHFAIL'">
    $("#set_passphrase_dialog").dialog("open");
    </s:elseif>
    <s:else>
    <s:if test="currentSystemStatus==null ||currentSystemStatus.statusCd!='GENERICFAIL'">
    $("#composite_terms_frm").submit();
    </s:if>
    </s:else>
    </s:elseif>






    <s:if test="pendingSystemStatus==null">

    var keys = {};

    $(document).keypress(function (e) {
        var keyCode = (e.keyCode) ? e.keyCode : e.charCode;

        var idList = [];
        $(".run_cmd_active").each(function (index) {
            var id = $(this).attr("id").replace("run_cmd_", "");
            idList.push(id);
        });

        if (String.fromCharCode(keyCode) && String.fromCharCode(keyCode) != ''
                && !keys[91] && !keys[93] && !keys[224] && !keys[27]
                && !keys[37] && !keys[38] && !keys[39] && !keys[40]
                && !keys[13] && !keys[8] && !keys[9] && !keys[17]) {
            var cmdStr = String.fromCharCode(keyCode);
            connection.send(JSON.stringify({id: idList, command: cmdStr}));
        }

    });
    //function for command keys (ie ESC, CTRL, etc..)
    $(document).keydown(function (e) {
        var keyCode = (e.keyCode) ? e.keyCode : e.charCode;
        keys[keyCode] = true;
        //27 - ESC
        //37 - LEFT
        //38 - UP
        //39 - RIGHT
        //40 - DOWN
        //13 - ENTER
        //8 - BS
        //9 - TAB
        //17 - CTRL
        if (keys[27] || keys[37] || keys[38] || keys[39] || keys[40] || keys[13] || keys[8] || keys[9] || keys[17]) {
            var idList = [];
            $(".run_cmd_active").each(function (index) {
                var id = $(this).attr("id").replace("run_cmd_", "");
                idList.push(id);
            });

            connection.send(JSON.stringify({id: idList, keyCode: keyCode}));
        }

    });

    $(document).keyup(function (e) {
        var keyCode = (e.keyCode) ? e.keyCode : e.charCode;
        delete keys[keyCode];
        $('#dummy').focus();
    });

    $(document).click(function (e) {
        $('#dummy').focus();
    });

    //get cmd text from paste
    $("#dummy").bind('paste', function (e) {
        $('#dummy').val('');
        setTimeout(function () {
            var idList = [];
            $(".run_cmd_active").each(function (index) {
                var id = $(this).attr("id").replace("run_cmd_", "");
                idList.push(id);
            });
            var cmdStr = $('#dummy').val();
            connection.send(JSON.stringify({id: idList, command: cmdStr}));
        }, 100);
    });


    var termMap = {};

    $(".output").each(function (index) {
        var id = $(this).attr("id").replace("output_", "");
        termMap[id] = new Terminal(80, 24);
        termMap[id].open($(this));
    });


    var loc = window.location, ws_uri;
    if (loc.protocol === "https:") {
        ws_uri = "wss:";
    } else {
        ws_uri = "ws:";
    }
    ws_uri += "//" + loc.host + '/terms.ws?t=' + new Date().getTime();

    var connection = new WebSocket(ws_uri);


    // Log errors
    connection.onerror = function (error) {
        console.log('WebSocket Error ' + error);
    };

    // Log messages from the server
    connection.onmessage = function (e) {
        var json = jQuery.parseJSON(e.data);
        $.each(json, function (key, val) {
            if (val.output != '') {
                termMap[val.hostSystemId].write(val.output);
            }
        });

    };

    setInterval(function () {
        try {
            connection.send('');
        }catch(ex){}
    }, <s:property value="terminalRefreshRate"/>);

    </s:if>

});
</script>

<title>KeyBox - Composite Terms</title>

</head>
<body>
<s:if test="(systemList!= null && !systemList.isEmpty()) || pendingSystemStatus!=null">

    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
        <div class="container" >

            <div class="navbar-header">
                <div class="navbar-brand" >
                    <div class="nav-img"><img src="<%= request.getContextPath() %>/img/keybox_50x38.png"/></div>
                    KeyBox</div>
            </div>
            <div class="collapse navbar-collapse">
                <s:if test="pendingSystemStatus==null">



                    <ul class="nav navbar-nav">
                        <li><a id="select_all" href="#">Select All</a></li>
                        <li><a id="upload_push" href="#">Upload &amp; Push</a></li>
                        <li><a href="exitTerms.action">Exit Terminals</a></li>
                    </ul>
                    <div class="note">(Use CMD-Click or CTRL-Click to select multiple individual terminals)</div>
                    <div class="clear"></div>
                </s:if>
            </div>
            <!--/.nav-collapse -->
        </div>
    </div>
    <div style="float:right;"><textarea name="dummy" id="dummy" size="1"
                                        style="border:none;color:#FFFFFF;width:1px;height:1px"></textarea></div>
    <div style="float:right;"><input type="text" name="dummy2" id="dummy2" size="1"
                                     style="border:none;color:#FFFFFF;width:1px;height:1px"/>
    </div>
    <div class="container"  style="width:100%;padding: 0px; margin: 0px;border:none;">


        <div class="termwrapper" >
            <s:iterator value="systemList">
                <div id="run_cmd_<s:property value="id"/>" class="run_cmd_active run_cmd">

                    <h4><s:property value="displayLabel"/></h4>

                    <div id="term" class="term">
                        <div id="output_<s:property value="id"/>" class="output"></div>
                    </div>

                </div>
            </s:iterator>
        </div>


        <div id="set_password_dialog" title="Enter Password">
            <p class="error"><s:property value="pendingSystemStatus.errorMsg"/></p>

            <p>Enter password for <s:property value="pendingSystemStatus.displayLabel"/>

            </p>
            <s:form id="password_frm" action="createTerms">
                <s:hidden name="pendingSystemStatus.id"/>
                <s:password name="password" label="Password" size="15" value="" autocomplete="off"/>
                <s:if test="script!=null">
                    <s:hidden name="script.id"/>
                </s:if>
                <tr>
                    <td>&nbsp;</td>
                    <td align="left">
                        <div class="btn btn-default submit_btn">Submit</div>
                        <div class="btn btn-default cancel_btn">Cancel</div>
                    </td>
                </tr>
            </s:form>
        </div>

        <div id="set_passphrase_dialog" title="Enter Passphrase">
            <p class="error"><s:property value="pendingSystemStatus.errorMsg"/></p>

            <p>Enter passphrase for <s:property value="pendingSystemStatus.displayLabel"/></p>
            <s:form id="passphrase_frm" action="createTerms">
                <s:hidden name="pendingSystemStatus.id"/>
                <s:password name="passphrase" label="Passphrase" size="15" value="" autocomplete="off"/>
                <s:if test="script!=null">
                    <s:hidden name="script.id"/>
                </s:if>
                <tr>
                    <td colspan="2">
                        <div class="btn btn-default submit_btn">Submit</div>
                        <div class="btn btn-default cancel_btn">Cancel</div>
                    </td>
                </tr>
            </s:form>
        </div>


        <div id="error_dialog" title="Error">
            <p class="error">Error: <s:property value="currentSystemStatus.errorMsg"/></p>

            <p>System: <s:property value="currentSystemStatus.displayLabel"/>

            </p>

            <s:form id="error_frm" action="createTerms">
                <s:hidden name="pendingSystemStatus.id"/>
                <s:if test="script!=null">
                    <s:hidden name="script.id"/>
                </s:if>
                <tr>
                    <td colspan="2">
                        <div class="btn btn-default submit_btn">OK</div>
                    </td>
                </tr>
            </s:form>
        </div>

        <div id="upload_push_dialog" title="Upload &amp; Push">
            <iframe id="upload_push_frame" width="700px" height="300px" style="border: none;">

            </iframe>


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

    <div class="container">
        <h3>Composite SSH Terms</h3>
        <p class="error">No sessions could be created</p>
    </div>
</s:else>



</body>
</html>
