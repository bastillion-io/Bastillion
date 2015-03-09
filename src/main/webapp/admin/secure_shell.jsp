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
    <script src="<%= request.getContextPath() %>/_res/js/jquery-ui.js"></script>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/_res/css/jquery-ui/jquery-ui.css"/>

    <script type="text/javascript">
        $(document).ready(function () {

            //get instance id list from selected terminals
            function getActiveTermsInstanceIds() {
                var ids = [];
                $(".run_cmd_active").each(function (index) {
                    var id = $(this).attr("id").replace("run_cmd_", "");
                    ids.push(id);
                });
                return ids;
            }


            $('#upload_push_dialog').on('hidden.bs.modal', function () {
                $("#upload_push_frame").attr("src", "");
            });

            $(".termwrapper").sortable({
                connectWith: ".run_cmd",
                handle: ".term-header",
                zIndex: 10000,
                helper: 'clone'
            });

            //submit
            $(".submit_btn").button().click(function () {
                <s:if test="pendingSystemStatus!=null">
                $(this).parents('.modal').find('form').submit();
                </s:if>
                $(this).parents('.modal').modal('hide');
            });
            
            //close all forms
            $(".cancel_btn").button().click(function () {
                window.location = 'getNextPendingSystemForTerms.action?pendingSystemStatus.id=<s:property value="pendingSystemStatus.id"/>&script.id=<s:if test="script!=null"><s:property value="script.id"/></s:if>';
            });

            //disconnect terminals and remove from view
            $('#disconnect').click(function(){
                var ids = getActiveTermsInstanceIds();
                for(var i=0;i<ids.length;i++) {
                    var id=ids[i];
                    $.ajax({url: '../admin/disconnectTerm.action?id=' + id, cache: false});
                    $('#run_cmd_'+id).remove();
                    termMap[id].destroy();
                    termMap[id]==null;
                    delete termMap[id];
                }
                
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

            //select all
            $('#select_all').click(function () {
                $(".run_cmd").addClass('run_cmd_active');
            });


            //upload frame dialog
            $('#upload_push').click(function () {


                var ids=[];
                $(".run_cmd_active").each(function (index) {
                    var id = $(this).find(".host").attr("data-hostId");
                    if (ids.indexOf(id)==-1) {
                        ids.push(id);
                    }
                });
                
                var idListStr = '?action=upload';
                ids.forEach(function (entry) {
                    idListStr = idListStr + '&idList=' + entry;
                });

                $("#upload_push_frame").attr("src", "setUpload.action" + idListStr);
                $("#upload_push_dialog").modal();


            });


            <s:if test="currentSystemStatus!=null && currentSystemStatus.statusCd=='GENERICFAIL'">
            $("#error_dialog").modal();
            </s:if>
            <s:if test="currentSystemStatus!=null && currentSystemStatus.statusCd=='HOSTFAIL'">
            $("#error_dialog").modal();
            </s:if>
            <s:elseif test="pendingSystemStatus!=null">
            <s:if test="pendingSystemStatus.statusCd=='AUTHFAIL'">
            $("#set_password_dialog").modal();
            </s:if>
            <s:elseif test="pendingSystemStatus.statusCd=='KEYAUTHFAIL'">
            $("#set_passphrase_dialog").modal();
            </s:elseif>
            <s:else>
            <s:if test="currentSystemStatus==null ||currentSystemStatus.statusCd!='GENERICFAIL'">
            $("#composite_terms_frm").submit();
            </s:if>
            </s:else>
            </s:elseif>






            <s:if test="pendingSystemStatus==null">

            $('#dummy').focus();
            var keys = {};


            var termFocus = true;
            $("#match").focus(function () {
                termFocus = false;
            });
            $("#match").blur(function () {
                termFocus = true;
            });

            $(".output").click().mouseover().mousedown(function () {
                termFocus = false;
            });


            $(document).keypress(function (e) {
                if (termFocus) {
                    var keyCode = (e.keyCode) ? e.keyCode : e.charCode;

                    if (String.fromCharCode(keyCode) && String.fromCharCode(keyCode) != ''
                            && !keys[91] && !keys[93] && !keys[224] && !keys[27]
                            && !keys[37] && !keys[38] && !keys[39] && !keys[40]
                            && !keys[13] && !keys[8] && !keys[9] && !keys[17] && !keys[46]) {
                        var cmdStr = String.fromCharCode(keyCode);
                        connection.send(JSON.stringify({id: getActiveTermsInstanceIds(), command: cmdStr}));
                    }

                }
            });
            //function for command keys (ie ESC, CTRL, etc..)
            $(document).keydown(function (e) {
                if (termFocus) {
                    var keyCode = (e.keyCode) ? e.keyCode : e.charCode;
                    keys[keyCode] = true;
                    //prevent default for unix ctrl commands
                    if (keys[17] && (keys[83] || keys[81] || keys[67] || keys[220] || keys[90] || keys[72] || keys[87] || keys[85] || keys[82] || keys[68])) {
                        e.preventDefault();
                    }

                    //27 - ESC
                    //37 - LEFT
                    //38 - UP
                    //39 - RIGHT
                    //40 - DOWN
                    //13 - ENTER
                    //8 - BS
                    //9 - TAB
                    //17 - CTRL
                    //46 - DEL
                    if (keys[27] || keys[37] || keys[38] || keys[39] || keys[40] || keys[13] || keys[8] || keys[9] || keys[17] || keys[46]) {
                        connection.send(JSON.stringify({id: getActiveTermsInstanceIds(), keyCode: keyCode}));
                    }
                }

            });

            $(document).keyup(function (e) {
                var keyCode = (e.keyCode) ? e.keyCode : e.charCode;
                delete keys[keyCode];
                if (termFocus) {
                    $('#dummy').focus();
                }
            });

            $(document).click(function (e) {
                if (termFocus) {
                    $('#dummy').focus();
                }
                //always change focus unless in match sort
                if (e.target.id != 'match') {
                    termFocus = true;
                }
            });


            //get cmd text from paste
            $("#dummy").bind('paste', function (e) {
                $('#dummy').focus();
                $('#dummy').val('');
                setTimeout(function () {
                    var cmdStr = $('#dummy').val();
                    connection.send(JSON.stringify({id: getActiveTermsInstanceIds(), command: cmdStr}));
                }, 100);
            });


            var termMap = {};

            var y_offset = $('.run_cmd:first').innerHeight() - $('.run_cmd').find(".output:first").innerHeight();

            $(".run_cmd").resizable({
                ghost: true,
                stop: function (event, ui) {
                    resize($(this));
                }
            });

            $("#reset_size").click(function () {
                var ids = getActiveTermsInstanceIds();
                for(var i=0;i<ids.length;i++) {
                    $("#run_cmd_"+ids[i]).width("48%");
                    $("#run_cmd_"+ids[i]).height(346 + y_offset);
                    resize($("#run_cmd_"+ids[i]));
                }
                
            });


            function resize(element) {
                var id = element.attr("id").replace("run_cmd_", "");

                if (termMap[id]) {
                    var width = $('#run_cmd_'+id).find(".output:first").innerWidth();
                    var height = $('#run_cmd_'+id).innerHeight() - y_offset;

                    termMap[id].resize(Math.floor(width / 7.2981), Math.floor(height / 14.4166));

                    $.ajax({
                        url: '../admin/setPtyType.action?id=' + id + '&ptyWidth=' + width + '&ptyHeight=' + height,
                        cache: false
                    });
                }
            }

            
            
               


            var loc = window.location, ws_uri;
            if (loc.protocol === "https:") {
                ws_uri = "wss:";
            } else {
                ws_uri = "ws:";
            }
            ws_uri += "//" + loc.host + loc.pathname + '/../terms.ws?t=' + new Date().getTime();

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
                        if(!termMap[val.instanceId]) {
                            createTermMap(val.instanceId, val.output);
                        }else {
                            termMap[val.instanceId].write(val.output);
                        }
                    }
                        
                });

                
            };
            
            function  createTermMap(id, output){

                if($("#run_cmd_" + id).find('.output').length == 0) {
                    setTimeout(createTermMap(id,output), 1000)
                }else {
                    termMap[id] = new Terminal({
                        cols: Math.floor($('.output:first').innerWidth() / 7.2981), rows: 24,
                        screenKeys: false,
                        useStyle: true,
                        cursorBlink: true,
                        convertEol: true
                    });
                    termMap[id].open($("#run_cmd_" + id).find('.output'));
                    
                    resize($("#run_cmd_" + id));
                    
                    termMap[id].write(output);

                }
                
            }
          

            $('#match_btn').unbind().click(function () {
                $('#match_frm').submit();
            });

            $('#match_frm').submit(function () {
                runRegExMatch();
                return false;
            });


            var matchFunction = null;

            function runRegExMatch() {

                if ($('#match_btn').hasClass('btn-success')) {
                    $('#match_btn').addClass('btn-danger');
                    $('#match_btn').removeClass('btn-success');
                    $('#match_btn').text("Stop");

                    matchFunction = setInterval(function () {

                        var termMap = [];
                        var existingTerms = [];
                        $(".run_cmd").each(function () {
                            var matchRegEx = null;
                            try {
                                matchRegEx = new RegExp($('#match').val(), 'g');
                            } catch (ex) {
                            }
                            if (matchRegEx != null) {
                                var attrId = $(this).attr("id");
                                if (attrId && attrId != '') {
                                    var id = attrId.replace("run_cmd_", "");

                                    var match = $('#output_' + id + ' > .terminal').text().match(matchRegEx);

                                    if (match != null) {
                                        termMap.push({id: id, no_matches: match.length});
                                    }
                                    existingTerms.push({id: id});
                                }
                            }
                        });


                        var sorted = termMap.slice(0).sort(function (a, b) {
                            return a.no_matches - b.no_matches;
                        });


                        for (var i = 0; i < sorted.length; ++i) {
                            var termId = sorted[i].id;
                            $('#run_cmd_' + termId).prependTo('.termwrapper');
                            if (sorted[sorted.length - i - 1].id != existingTerms[i].id) {
                                $('#run_cmd_' + termId).fadeTo(100, .5).fadeTo(100, 1);
                            }
                        }


                    }, 5000);
                } else {
                    $('#match_btn').addClass('btn-success');
                    $('#match_btn').removeClass('btn-danger');
                    $('#match_btn').text("Start");
                    clearInterval(matchFunction)
                }
            }

            //function clones terminals based on active
            var newInstanceId=-1;
            $('#dup_session').click(function () {
                var instanceIds = getActiveTermsInstanceIds();
                for (var i = 0; i < instanceIds.length; i++) {

                    var instanceId = instanceIds[i];
                    var hostId=$('#run_cmd_'+instanceId).find(".host").attr("data-hostId");

                    //call server to create instances - returned the new cloned instance id
                    $.getJSON('../admin/duplicateSession.action?systemSelectId=' + hostId, function (data) {
                        newInstanceId= parseInt(data);
                    });


                    if (newInstanceId!=null && newInstanceId >= 1) {

                        $("#run_cmd_" + instanceId).resizable('destroy');

                        var clone = $("#run_cmd_" + instanceId).clone(true, true);
                        clone.attr("id", "run_cmd_" + newInstanceId);
                        clone.find(".output").remove();

                        $("<div id='output_" + newInstanceId + "' class='output' />").appendTo(clone.find(".term"));

                        clone.insertAfter($("#run_cmd_" + instanceId));
                        

                        $("#run_cmd_" + newInstanceId+",#run_cmd_" + instanceId ).resizable({
                            ghost: true,
                            stop: function (event, ui) {
                                resize($(this));
                            }
                        });

                    


                    }
                }

            });
           //default to active
           $(".run_cmd").addClass('run_cmd_active');


            </s:if>


        });


    </script>

    <style>

        .align-right {
            padding: 10px 2px 10px 10px;
            float: right;
        }

        .term-container {
            width: 100%;
            padding: 25px 0px;
            margin: 0px;
        }

    </style>

    <title>KeyBox - Composite Terms</title>

</head>
<body>
<s:if test="(systemList!= null && !systemList.isEmpty()) || pendingSystemStatus!=null">

<div class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">

        <div class="navbar-header">
            <div class="navbar-brand">
                <div class="nav-img"><img src="<%= request.getContextPath() %>/img/keybox_50x38.png" alt="keybox"/>
                </div>
                KeyBox
            </div>
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        </div>
        <div class="collapse navbar-collapse">
            <s:if test="pendingSystemStatus==null">


                <ul class="nav navbar-nav">
                    <li><a id="select_all" href="#"
                           title="Use CMD-Click or CTRL-Click to select multiple individual terminals">Select
                        All</a></li>

                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Terminal Actions<b class="caret"></b></a>
                        <ul class="dropdown-menu">
                            <li><a id="upload_push" href="#">Upload &amp; Push</a></li>
                            <li><a id="dup_session" href="#">Duplicate Session</a></li>
                            <li><a id="reset_size" href="#">Reset Size</a></li>
                            <li><a id="disconnect" href="#">Disconnect</a></li>
                        </ul>
                    </li>
                    <li><a href="exitTerms.action">Exit Terminals</a></li>
                </ul>
                <div style="float:right;width:1px;">
                    <textarea name="dummy" id="dummy" size="1"
                              style="border:none;color:#FFFFFF;width:1px;height:1px"></textarea>
                    <input type="text" name="dummy2" id="dummy2" size="1"
                           style="border:none;color:#FFFFFF;width:1px;height:1px"/>
                </div>
                <div class="align-right">
                    <s:form id="match_frm" theme="simple">
                        <label>Sort By</label>&nbsp;&nbsp;<s:textfield id="match" name="match"
                                                                       placeholder="Bring terminals to top that match RegExp"
                                                                       size="40"
                                                                       theme="simple"/>
                        <div id="match_btn" class="btn btn-success">Start</div>
                    </s:form>
                </div>

                <div class="clear"></div>
            </s:if>
        </div>
        <!--/.nav-collapse -->
    </div>


</div>

<div class="term-container container">


    <div class="termwrapper">


        <s:iterator value="systemList">

            <div id="run_cmd_<s:property value="instanceId"/>" class="run_cmd_active run_cmd">
                <h6 class="term-header" style="white-space: nowrap"><s:property value="displayLabel"/></h6>
                <div class="term">
                    <div id="output_<s:property value="instanceId"/>" class="output"></div>
                </div>
                <div data-hostId="<s:property value="id"/>" class="host"></div>
            </div>
        </s:iterator>


    </div>
    </s:if>
    <s:else>
        <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        <h3>Composite SSH Terms</h3>

        <p class="error">No sessions could be created</p>
    </div>
    </s:else>

    <div id="set_password_dialog" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close cancel_btn" data-dismiss="modal" aria-hidden="true">x</button>
                    <h4 class="modal-title">Enter password for <s:property
                            value="pendingSystemStatus.displayLabel"/></h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="error">Error: <s:property value="pendingSystemStatus.errorMsg"/></div>
                        <s:form id="password_frm" action="createTerms">
                            <s:hidden name="pendingSystemStatus.id"/>
                            <s:password name="password" label="Password" size="15" value="" autocomplete="off"/>
                            <s:if test="script!=null">
                                <s:hidden name="script.id"/>
                            </s:if>
                        </s:form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-default submit_btn">Submit</button>
                </div>
            </div>
        </div>
    </div>


    <div id="set_passphrase_dialog" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close cancel_btn" data-dismiss="modal" aria-hidden="true">x</button>
                    <h4 class="modal-title">Enter passphrase for <s:property
                            value="pendingSystemStatus.displayLabel"/></h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="error">Error: <s:property value="pendingSystemStatus.errorMsg"/></div>
                        <s:form id="passphrase_frm" action="createTerms">
                            <s:hidden name="pendingSystemStatus.id"/>
                            <s:password name="passphrase" label="Passphrase" size="15" value="" autocomplete="off"/>
                            <s:if test="script!=null">
                                <s:hidden name="script.id"/>
                            </s:if>
                        </s:form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-default submit_btn">Submit</button>
                </div>
            </div>
        </div>
    </div>

    <div id="error_dialog" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                    <h4 class="modal-title">System: <s:property value="currentSystemStatus.displayLabel"/></h4>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="error">Error: <s:property value="currentSystemStatus.errorMsg"/></div>
                        <s:form id="error_frm" action="createTerms">
                            <s:hidden name="pendingSystemStatus.id"/>
                            <s:if test="script!=null">
                                <s:hidden name="script.id"/>
                            </s:if>
                        </s:form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default submit_btn">OK</button>
                </div>
            </div>
        </div>
    </div>

    <div id="upload_push_dialog" class="modal fade">
        <div class="modal-dialog" style="width:700px">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                    <h4 class="modal-title">Upload &amp; Push</h4>
                </div>
                <div class="modal-body">
                    <iframe id="upload_push_frame" width="675px" height="300px" style="border: none;">
                    </iframe>
                </div>
            </div>
        </div>
    </div>

    <s:form id="composite_terms_frm" action="createTerms">
        <s:hidden name="pendingSystemStatus.id"/>
    <s:if test="script!=null">
        <s:hidden name="script.id"/>
    </s:if>
    </s:form>

</body>
</html>
