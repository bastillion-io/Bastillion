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

<script type="text/javascript">
$(document).ready(function() {


    $(".termwrapper").sortable({
            helper : 'clone'
    });
    //submit add or edit form
    $(".submit_btn").button().click(function() {
        filterTerms();
    });


    $(".clear_btn").button().click(function() {
        $('#filter_frm_filter').val('');
        filterTerms();
    });

    function filterTerms() {
        var filterVal=$('#filter_frm_filter').val();

        if(filterVal!=null && filterVal!='') {
            $(".output > .terminal > pre").each(function (index, value){

                if($(this).text().indexOf(filterVal)>=0) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        } else {
            $(".output > .terminal > pre").show();

        }

   }







  function loadTerms(){

        $(".output").each(function (index, value){

               var id = $(this).attr("id").replace("output_", "");

               $.getJSON('getJSONTermOutputForSession.action?sessionId=<s:property value="sessionAudit.id"/>&instanceId='+id+'&t='+new Date().getTime() +'&_csrf=<s:property value="#session['_csrf']"/>', function(data) {
                   $.each(data, function(key, val) {
                       if (val.output != '' && val.instanceId!=null) {

                               $("#output_"+val.instanceId+"> .terminal").empty();
                               var output=val.output;
                               output = output.replace(/\r\n\r\n/g, '\r\n \r\n');
                               var outputList = output.split('\r\n');
                               for(var i=0; i<outputList.length;i++){
                                   $("#output_"+val.instanceId+"> .terminal").append("<pre>"+outputList[i]+"</pre>");;
                               }


                       }
                   });
                });

           });
  }

  loadTerms();

});

</script>
<style type="text/css">
    .run_cmd {
        min-width:600px ;
	}
    .terminal {
        background-color: rgb(240, 240, 240);
        color: rgb(77, 77, 77);
        height:300px;
        overflow-y:scroll;

    }
    .terminal pre {
        padding:0;
        margin:2px;
        white-space: pre-wrap;
        word-wrap: break-word;
        background-color: #F5F5F5;
    }

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

<title>KeyBox - Session Terms</title>

</head>
<body>
<div class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container" >

        <div class="navbar-header">
            <div class="navbar-brand" >
            <div class="nav-img"><img src="<%= request.getContextPath() %>/img/keybox_40x40.png" alt="keybox"/></div>
             KeyBox</div>
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
                 <li><a href="viewSessions.action?_csrf=<s:property value="#session['_csrf']"/>">Exit Audit</a></li>
            </ul>


                <div class="align-right">
                    <s:form id="filter_frm" theme="simple">
                        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                        <s:label value=""/>
                        <s:textfield name="filter" type="text" class="spacer spacer-left"/><div class="btn btn-default submit_btn spacer spacer-middle">Filter</div><div class="btn btn-default clear_btn spacer spacer-right">Clear</div>
                    </s:form>
                </div>
                <div class="align-right" style="padding-top: 15px">
                    <b>Audit  ( <s:property value="sessionAudit.user.username"/>
                    <s:if test="sessionAudit.user!=null && sessionAudit.user.lastNm!=null">
                        - <s:property value="sessionAudit.user.lastNm"/>, <s:property value="sessionAudit.user.firstNm"/>
                    </s:if> ) </b>
                </div>
            <div class="clear"></div>
            </s:if>
        </div>
        <!--/.nav-collapse -->
    </div>
</div>


<div class="term-container container">
 <s:if test="sessionAudit!= null">



            <div class="termwrapper">
                <s:iterator value="sessionAudit.hostSystemList">
                    <div id="run_cmd_<s:property value="instanceId"/>" class="run_cmd_active run_cmd">

                        <h6 class="term-header"><s:property value="displayLabel"/></h6>

                        <div id="term" class="term">
                            <div id="output_<s:property value="instanceId"/>" class="output">
                            <div class="terminal" >
                            </div>
                            </div>



                        </div>



                    </div>
                </s:iterator>
            </div>
</s:if>


</div>




</body>
</html>
