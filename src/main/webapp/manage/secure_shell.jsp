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

    $('#dummy').focus();

    $("#set_password_dialog").dialog({
        autoOpen: false,
        height: 200,
        minWidth: 500,
        modal: true
    });
    $("#set_passphrase_dialog").dialog({
        autoOpen: false,
        height: 200,
        minWidth: 500,
        modal: true
   });
    $("#error_dialog").dialog({
        autoOpen: false,
        height: 200,
        width: 500,
        modal: true
    });
    $("#upload_push_dialog").dialog({
        autoOpen: false,
        height: 350,
        width: 530,
        modal: true,
        open: function(event, ui) {
            $(".ui-dialog-titlebar-close").show();
        },
        close: function() {
            $("#upload_push_frame").attr("src", "");
        }
    });

    $(".termwrapper").sortable({
        helper : 'clone'
    });

    //submit add or edit form
    $(".submit_btn").button().click(function() {
        <s:if test="pendingSystemStatus!=null">
        $(this).prev().submit();
        </s:if>
        $("#error_dialog").dialog("close");
    });
    //close all forms
    $(".cancel_btn").button().click(function() {
        $("#set_password_dialog").dialog("close");
        window.location = 'getNextPendingSystemForTerms.action?pendingSystemStatus.id=<s:property value="pendingSystemStatus.id"/>&script.id=<s:if test="script!=null"><s:property value="script.id"/></s:if>';

    });


      //if terminal window toggle active for commands
      $(".run_cmd").click(function() {

           //check if all terminals are selected
           var allSelected = true;
           $(".run_cmd").each(function(index) {
                if(!$(this).hasClass('run_cmd_active')){
                    allSelected=false;
                }
           });

           //if all terminals are selected make only the clicked one active
           if(allSelected) {
                $(".run_cmd").removeClass('run_cmd_active');
                $(this).addClass('run_cmd_active');

           } else {
                //de-active or active clicked terminal
                if ($(this).hasClass('run_cmd_active')) {
                    $(this).removeClass('run_cmd_active')
                } else {
                    $(this).addClass('run_cmd_active')
                }
           }

      });

    $('#select_all').click(function() {
        $(".run_cmd").addClass('run_cmd_active');
    });



    $('#upload_push').click(function() {


        //get id list from selected terminals
        var ids = [];
        $(".run_cmd_active").each(function(index) {
            var id = $(this).attr("id").replace("run_cmd_", "");
            ids.push(id);
        });
        var idListStr = '?action=upload';
        ids.forEach(function(entry) {
            idListStr = idListStr + '&idList=' + entry;
        });

        $("#upload_push_frame").attr("src", "/manage/setUpload.action" + idListStr);
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
                setInterval(function() {
                    $("#composite_terms_frm").submit();
                 }, 2000);
            </s:if>
        </s:else>
    </s:elseif>






    <s:if test="pendingSystemStatus==null">

        var keys = {};
        $(document).keypress(function(e) {
            var keyCode= (e.keyCode)? e.keyCode: e.charCode;

            var idListStr = '';
            $(".run_cmd_active").each(function(index) {
                var id = $(this).attr("id").replace("run_cmd_", "");
                idListStr = idListStr + '&idList=' + id;
            });

            if(String.fromCharCode(keyCode) && String.fromCharCode(keyCode)!='' && !keys[17]){
                $.ajax({ url: 'runCmd.action?command=' +String.fromCharCode(keyCode) + idListStr});
            }

        });
        //function for command keys (ie ESC, CTRL, etc..)
        $(document).keydown(function (e) {
            var keyCode= (e.keyCode)? e.keyCode: e.charCode;
            keys[keyCode]=true;
            if (keys[27] || keys[37] || keys[38] || keys[39] || keys[40] ||  keys[13] || keys[8] || keys[9] || keys[17]) {
                    var idListStr = '';
                    $(".run_cmd_active").each(function(index) {
                        var id = $(this).attr("id").replace("run_cmd_", "");
                        idListStr = idListStr + '&idList=' + id;
                    });
                    $.ajax({ url: 'runCmd.action?keyCode=' + keyCode + idListStr});
                }

        });

        $(document).keyup(function (e) {
            var keyCode= (e.keyCode)? e.keyCode: e.charCode;
            delete keys[keyCode];
            $('#dummy').focus();
        });

        $(document).click(function (e) {
            $('#dummy').focus();
        });



    var termMap = {};

    $(".output").each(function(index) {
        var id = $(this).attr("id").replace("output_", "");
        termMap[id] =  new Terminal(80, 24);
        termMap[id].open($(this));
    });


    setInterval(function() {
        $.getJSON('getOutputJSON.action', function(data) {
            var append = false;
            $.each(data, function(key, val) {
                if (val.output != '') {
                   termMap[val.sessionId].write(val.output);
                }
            });
        });
    }, 500);
    </s:if>

});
</script>
<style type="text/css">
    .content {
        width: 99%;
        padding:5px;
        margin:0;
        border:none;
    }
    .page {
        padding:10px;
    }
</style>

<title>KeyBox - Composite Terms</title>

</head>
<body>

<div class="page">

    <s:if test="(schSessionMap!= null && !schSessionMap.isEmpty()) || pendingSystemStatus!=null">
        <div class="content">

            <s:if test="pendingSystemStatus==null">


                <div id="select_all" class="top_link">Select All</div>
                <div id="upload_push" class="top_link">Upload &amp; Push</div>
                 <div class="top_link" ><a href="/manage/exitTerms.action">Exit Terminals</a></div>
                 <div class="top_link" style="float:right;"><input type="text" name="dummy" id="dummy" size="1" style="border:none;color:#FFFFFF;width:1px;height:1px"/></div>
                <div class="clear"></div>

            </s:if>
            <div class="termwrapper">
                <s:iterator value="schSessionMap">
                    <div id="run_cmd_<s:property value="key"/>" class="run_cmd_active run_cmd">

                        <h4><s:property value="value.hostSystem.displayLabel"/></h4>


                        <div id="term" class="term">
                            <div id="output_<s:property value="key"/>" class="output"></div>
                        </div>

                    </div>
                </s:iterator>
            </div>


            <div id="set_password_dialog" title="Enter Password">
                <p class="error"><s:property value="pendingSystemStatus.errorMsg"/></p>

                <p>Enter password for <s:property value="pendingSystemStatus.hostSystem.displayLabel"/>

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

            <div id="set_passphrase_dialog" title="Enter Passphrase">
                <p class="error"><s:property value="pendingSystemStatus.errorMsg"/></p>
                <s:form id="passphrase_frm" action="createTerms">
                    <s:hidden name="pendingSystemStatus.id"/>
                    <s:password name="passphrase" label="Passphrase" size="15" value="" autocomplete="off"/>
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

            <div id="upload_push_dialog" title="Upload &amp; Push">
                <iframe id="upload_push_frame" width="500px" height="300px" style="border: none;">

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

        <div class="content" style="width: 70%">
            <p class="error">No sessions could be created</p>
        </div>
    </s:else>

</div>

</body>
</html>
