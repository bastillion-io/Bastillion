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

            $(".submit_btn").button().click(function() {
                $('#push').submit();
            });

            if ($('.uploadScrollWrapper').height() >= 200) {

                $('.uploadScrollWrapper').addClass('uploadScrollWrapperActive');
                $('.uploadScrollableTable').floatThead({
                    scrollContainer: function ($table) {
                        return $table.closest(".uploadScrollWrapper");
                    }
                });
            }
            $(".uploadScrollableTable tr:even").css("background-color", "#e0e0e0");

            <s:if test="pendingSystemStatus!=null">
            //set scroll
            var container = $('.uploadScrollWrapper'), scrollTo = $('#status_<s:property value="pendingSystemStatus.id"/>');
            container.scrollTop(scrollTo.offset().top - container.offset().top + container.scrollTop() - 55);
            </s:if>
            <s:if test="currentSystemStatus!=null && currentSystemStatus.statusCd=='GENERICFAIL'">
            $("#error_dialog").modal();
            </s:if>
            <s:elseif test="pendingSystemStatus!=null">
            $('#push').submit();
            </s:elseif>


        });
    </script>
    <style>
        body {
            padding: 10px;
        }
    </style>


</head>
<body style="background: #FFFFFF">

<h4>
    Pushing File: <s:property value="uploadFileName"/>
</h4>


<s:if test="hostSystemList!= null && !hostSystemList.isEmpty()">
    <div class="uploadScrollWrapper">

    <table class="table-striped uploadScrollableTable" >
        <thead>

        <tr>

            <th>Display Name</th>
            <th>User</th>
            <th>Host</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>

        <s:iterator value="hostSystemList" status="stat">
            <tr>

                <td>
                    <div id="status_<s:property value="id"/>"><s:property
                            value="displayNm"/></div>
                </td>
                <td><s:property value="user"/></td>
                <td><s:property value="host"/>:<s:property value="port"/></td>

                <td>
                   <s:if test="statusCd=='INITIAL'">
                    <div class="warning">Not Started</div>
                   </s:if>
                   <s:elseif test="statusCd=='AUTHFAIL'">
                    <div class="warning">Authentication Failed</div>
                   </s:elseif>
                   <s:elseif test="statusCd=='HOSTFAIL'">
                    <div class="error">DNS Lookup Failed</div>
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
    </div>
</s:if>
<s:else>
    <p class="error">No systems associated with upload</p>
</s:else>

<s:form action="push" method="GET">
    <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
    <s:hidden name="pushDir"/>
    <s:hidden name="uploadFileName"/>
</s:form>



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
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default submit_btn">OK</button>
            </div>
        </div>
    </div>
</div>

</body>
</html>