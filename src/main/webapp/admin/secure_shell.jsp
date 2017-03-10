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
                window.location = 'getNextPendingSystemForTerms.action?pendingSystemStatus.id=<s:property value="pendingSystemStatus.id"/>&script.id=<s:if test="script!=null"><s:property value="script.id"/></s:if>&_csrf=<s:property value="#session['_csrf']"/>';
            });

            //disconnect terminals and remove from view
            $('#disconnect').click(function(){
                var ids = getActiveTermsInstanceIds();
                for(var i=0;i<ids.length;i++) {
                    var id=ids[i];
                    $.ajax({url: '../admin/disconnectTerm.action?id=' + id + '&_csrf=<s:property value="#session['_csrf']"/>', cache: false});
                    $('#run_cmd_'+id).remove();
                    termMap[id].destroy();
                    delete termMap[id];
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
                
                var idListStr = '?action=upload&_csrf=<s:property value="#session['_csrf']"/>';
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

           

            $(document).keypress(function (e) {
                if (termFocus) {
                    var keyCode = (e.keyCode) ? e.keyCode : e.charCode;

                    if (String.fromCharCode(keyCode) && String.fromCharCode(keyCode) != ''
                            && (!e.ctrlKey || e.altKey) && !e.metaKey && !keys[27] && !keys[37]
                            && !keys[38] && !keys[39] && !keys[40] && !keys[13] && !keys[8] && !keys[9] 
                            && !keys[46] && !keys[45] && !keys[33] && !keys[34] && !keys[35] && !keys[36]) {
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
                    //45 - INSERT
                    //33 - PG UP
                    //34 - PG DOWN
                    //35 - END
                    //36 - HOME
                    if((e.ctrlKey && !e.altKey) || keyCode == 27 || keyCode == 37 || keyCode == 38 || keyCode == 39 || keyCode == 40 || keyCode == 13 || keyCode == 8 || keyCode == 9 || keyCode == 46 || keyCode == 45 || keyCode == 33 || keyCode == 34 || keyCode == 35 || keyCode == 36) {
                        connection.send(JSON.stringify({id: getActiveTermsInstanceIds(), keyCode: keyCode}));
                    }
                    
                    //prevent default for unix ctrl commands
                    if (e.ctrlKey && (keyCode == 83 || keyCode == 81 || keyCode == 84 || keyCode == 220 || keyCode == 90 || keyCode == 72 || keyCode == 87 || keyCode == 85 || keyCode == 82 || keyCode == 68)) {
                        e.preventDefault();
                        e.stopImmediatePropagation();
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
                if (termFocus && !$('body').hasClass('modal-open')) {
                    $('#dummy').focus();
                }
                //always change focus unless in match sort
                if (e.target.id != 'match') {
                    termFocus = true;
                }
            });


            //get cmd text from paste
            $(this).bind('paste', function (e) {
                $('#dummy').focus();
                $('#dummy').val('');
                setTimeout(function () {
                    var cmdStr = $('#dummy').val();
                    connection.send(JSON.stringify({id: getActiveTermsInstanceIds(), command: cmdStr}));
                }, 100);
            });


            var termMap = {};


            $("#reset_size").click(function () {
                var ids = getActiveTermsInstanceIds();
                for(var i=0;i<ids.length;i++) {
                    $("#run_cmd_"+ids[i]).width("48%");
                    $("#run_cmd_"+ids[i]).height(346 + y_offset);
                    resize($("#run_cmd_"+ids[i]));
                }
                
            });

            //resize element during drag event. Makes call to set pty width and height
            function resize(element) {
                var id = element.attr("id").replace("run_cmd_", "");

                if (termMap[id]) {
                    var width = $('#run_cmd_'+id).find(".output:first").innerWidth() - 8;
                    var height = $('#run_cmd_'+id).innerHeight() - y_offset;

                    termMap[id].resize(Math.floor(width / 7.2981), Math.floor(height / 14.4166));

                    $.ajax({
                        url: '../admin/setPtyType.action?id=' + id + '&userSettings.ptyWidth=' + width + '&userSettings.ptyHeight=' + height + '&_csrf=<s:property value="#session['_csrf']"/>',
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

                termMap[id] = new Terminal({
                        cols: Math.floor($('.output:first').innerWidth() / 7.2981), rows: 24,
                    <s:if test="%{userSettings !=null && userSettings.colors!=null && userSettings.colors.length==16}">
                        colors: [
                        <s:iterator status="stat" value="userSettings.colors">
                            '<s:property/>'<s:if test="%{#stat.count<16}">,</s:if>
                        </s:iterator>
                        ],
                    </s:if>
                    screenKeys: false,
                    useStyle: true,
                    cursorBlink: true,
                    convertEol: true
                });
                <s:if test="%{userSettings !=null && userSettings.bg !=null}">
                    termMap[id].colors[256] = '<s:property value="userSettings.bg"/>';
                </s:if>
                <s:if test="%{userSettings !=null && userSettings.fg!=null}">
                    termMap[id].colors[257] = '<s:property value="userSettings.fg"/>';
                </s:if>
                termMap[id].open($("#run_cmd_" + id).find('.output'));
                    
                resize($("#run_cmd_" + id));
                    
                termMap[id].write(output);

                
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


            //function to set all terminal bindings when creating a term window
            function setTerminalEvents(element)
            {

                //if terminal window toggle active for commands
                element.mousedown(function (e) {
                    //check for cmd-click / ctr-click
                    if (!e.ctrlKey && !e.metaKey) {
                        $(".run_cmd").removeClass('run_cmd_active');
                    }

                    if (element.hasClass('run_cmd_active')) {
                        element.removeClass('run_cmd_active');
                    } else {
                        element.addClass('run_cmd_active')
                    }
                });

                //set focus to term
                $(".output").mouseup(function (e) {
                    if(window.getSelection().toString()) {
                        termFocus = false;
                    } else {
                        termFocus = true;
                    }
                });

                $(".output").bind('copy', function () {
                    setTimeout(function () {
                        termFocus = true;
                        window.getSelection().removeAllRanges();
                    }, 100);
                });

                //set resizable
                element.resizable({
                    ghost: true,
                    stop: function (event, ui) {
                        resize($(this));
                    }
                });
            }
            
            //returns div for newly created terminal element
            function createTermElement(instanceId, hostId, displayLabel){
                var instance =
                        "<div id=\"run_cmd_" +instanceId + "\" class=\"run_cmd_active run_cmd\">"
                        + "<h6 class=\"term-header\">" + displayLabel + "</h6>"
                        + "<div class=\"term\">"
                        +   "<div id=\"output_" + instanceId + "\" class=\"output\"></div>"
                        + "</div>"
                        + "<div data-hostId=\""+ hostId +"\" class=\"host\"></div>"
                        +"</div>";
               return instance;
            }

            //function clones terminals based on active
            var newInstanceId=-1;
            $('#dup_session').click(function () {
                var instanceIds = getActiveTermsInstanceIds();
                for (var i = 0; i < instanceIds.length; i++) {

                    var instanceId = instanceIds[i];
                    var hostId=$('#run_cmd_'+instanceId).find(".host").attr("data-hostId");
                    var displayLabel=$('#run_cmd_'+instanceId).find(".term-header").text();
                    var newInstanceId=getNextInstanceId();

                    $(createTermElement(newInstanceId,hostId,displayLabel)).insertAfter($('#run_cmd_'+instanceId));

                    setTerminalEvents($("#run_cmd_"+newInstanceId));
                    
                    
                    //call server to create instances - returned the new cloned instance id
                    $.getJSON('../admin/createSession.action?systemSelectId=' + hostId + '&_csrf=<s:property value="#session['_csrf']"/>');

                   
                }

            });

            //function that connects to allocated hosts
            $('.connect_btn').click(function () {

                var hostId=$('#connectHostId').val();
                var newInstanceId=getNextInstanceId();
                var displayLabel=$('#connectHostId option:selected').text();

                $(createTermElement(newInstanceId,hostId,displayLabel)).prependTo(".termwrapper");

                setTerminalEvents($("#run_cmd_"+newInstanceId)); 

                //call server to create instances - returned the new cloned instance id
                $.getJSON('../admin/createSession.action?systemSelectId=' + hostId + '&_csrf=<s:property value="#session['_csrf']"/>');


            });
            
            //returns next instance id
            function getNextInstanceId() {
                var newInstanceId=1;

                for(var i=1;i<=$('.run_cmd').length;i++){

                    if($("#run_cmd_" + i).length == 0) {
                        newInstanceId=i;
                        break;
                    }
                    newInstanceId++;
                }
                return newInstanceId;
            }

            //set connected systems
            <s:iterator value="systemList">
                $(createTermElement(<s:property value="instanceId"/>,<s:property value="id"/>,'<s:property value="displayLabel"/>')).appendTo(".termwrapper");
                setTerminalEvents($("#run_cmd_"+<s:property value="instanceId"/>));
            </s:iterator>

            var y_offset = $('.run_cmd:first').innerHeight() - $('.run_cmd').find(".output:first").innerHeight();

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
            padding: 10px 0px;
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
                <div class="nav-img"><img src="<%= request.getContextPath() %>/img/keybox_40x40.png" alt="keybox"/>
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
                            <li><a id="connect_to_host" data-toggle="modal" data-target="#connect_to_host_dialog" href="#">Connect to Host</a></li>
                            <li><a id="dup_session" href="#">Duplicate Session</a></li>
                            <li><a id="reset_size" href="#">Reset Size</a></li>
                            <li><a id="disconnect" href="#">Disconnect</a></li>
                        </ul>
                    </li>
                    <li><a href="exitTerms.action?_csrf=<s:property value="#session['_csrf']"/>">Exit Terminals</a></li>
                </ul>
                <div class="align-right">
                    <s:form id="match_frm" theme="simple">
                        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                        <label>Sort By</label>&nbsp;&nbsp;<s:textfield id="match" name="match"
                                                                       placeholder="Bring terminals to top that match RegExp"
                                                                       size="40"
                                                                       theme="simple"/>
                        <div id="match_btn" class="btn btn-success">Start</div>
                    </s:form>
                </div>

                <div class="clear"></div>
                <div style="float:right;width:1px;height:1px;overflow:hidden">
                    <textarea name="dummy" id="dummy" size="1"
                              style="border:none;color:#FFFFFF;width:1px;height:1px"></textarea>
                    <input type="text" name="dummy2" id="dummy2" size="1"
                           style="border:none;color:#FFFFFF;width:1px;height:1px"/>
                </div>
            </s:if>
        </div>
        <!--/.nav-collapse -->
    </div>


</div>

<div class="term-container container">


    <div class="termwrapper">


    </div>
    </s:if>
    <s:else>
        <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        <h3>Composite SSH Terms</h3>

        <p class="error">No sessions could be created</p>
    </div>
    </s:else>

    <div id="connect_to_host_dialog" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                    <h4 class="modal-title">Connect to Host</h4>
                </div>
                <div class="modal-body">
                    <div class="row">

                            <s:select id="connectHostId" listKey="id" listValue="displayLabel"
                                      class="host_frm_select"
                                      list="allocatedSystemList"
                                      />

                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-default connect_btn" data-dismiss="modal">Connect</button>
                </div>
            </div>
        </div>
    </div>

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
                            <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
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
                            <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
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
                            <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
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
        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
        <s:hidden name="pendingSystemStatus.id"/>
    <s:if test="script!=null">
        <s:hidden name="script.id"/>
    </s:if>
    </s:form>

</body>
</html>
