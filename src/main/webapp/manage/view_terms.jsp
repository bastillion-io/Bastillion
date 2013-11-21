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

    $.ajaxSetup({ cache: false });

    $(".termwrapper").sortable({
            helper : 'clone'
    });
    //submit add or edit form
    $(".submit_btn").button().click(function() {
        $(this).parents('form:first').submit();
    });


    $(".clear_btn").button().click(function() {
        $('#filter_frm_filter').val('');
        $(this).parents('form:first').submit();
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


               $.getJSON('getJSONTermOutputForSession.action?sessionId=<s:property value="sessionAudit.id"/>&hostSystemId='+id+'&t='+new Date().getTime(), function(data) {
                   $.each(data, function(key, val) {
                       if (val.output != '' && val.hostSystemId!=null) {

                               $("#output_"+val.hostSystemId+"> .terminal").empty();
                               var output=val.output;
                               output = output.replace(/\r\n\r\n/g, '\r\n \r\n');
                               var outputList = output.split('\r\n');
                               for(var i=0; i<outputList.length;i++){
                                   $("#output_"+val.hostSystemId+"> .terminal").append("<pre>"+outputList[i]+"</pre>");;
                               }


                       }
                   });
                });

           });
  }

  loadTerms();

    $("#filter_frm").submit(function(){

        filterTerms();

        return false;

        });

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
    .terminal {
        background-color: rgb(240, 240, 240);
        color: rgb(77, 77, 77);
        width:550px;
        height:300px;
        overflow-y:scroll;

    }
    .terminal pre {
        padding:0;
        margin:2px;
        white-space: pre-wrap;
        word-wrap: break-word;

    }

</style>

<title>KeyBox - Session Terms</title>

</head>
<body>

<div class="page">

 <div class="content">
 <s:if test="sessionAudit!= null">
 <div style="float:left;">
    <b>Audit  ( <s:property value="sessionAudit.user.username"/>
    <s:if test="sessionAudit.user!=null && sessionAudit.user.lastNm!=null">
        - <s:property value="sessionAudit.user.lastNm"/>, <s:property value="sessionAudit.user.firstNm"/>
    </s:if> ) </b>
 </div>
 <div class="top_link" ><a href="viewSessions.action">Exit Audit</a></div>

<div class="clear"></div>

<div style="float:left;padding-bottom:10px;">
<s:form id="filter_frm" theme="simple">
<s:label value=""/>
<s:textfield name="filter" type="text"/><div class="submit_btn">Filter</div><div class="clear_btn">Clear</div>
</s:form>
</div>
        <div class="clear"></div>

            <div class="termwrapper">
                <s:iterator value="sessionAudit.hostSystemList">
                    <div id="run_cmd_<s:property value="id"/>" class="run_cmd_active run_cmd">

                        <h4><s:property value="displayLabel"/></h4>

                        <div id="term" class="term">
                            <div id="output_<s:property value="id"/>" class="output">
                            <div class="terminal" >
                            </div>
                            </div>



                        </div>



                    </div>
                </s:iterator>
            </div>
            </s:if>


</div>


</div>

</body>
</html>
