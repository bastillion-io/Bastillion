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
            $("#view_btn").button().click(function () {
                $("#viewSystems").submit();
            });

            $(".select_frm_btn").button().click(function() {
                $("#select_frm").submit();
            });
            //select all check boxes
            $("#select_frm_systemSelectAll").click(function() {
                if ($(this).is(':checked')) {
                    $(".systemSelect").prop('checked', true);
                } else {
                    $(".systemSelect").prop('checked', false);
                }
            });

            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewSystems_sortedSet_orderByField').attr('value', id);
                $("#viewSystems").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

    });
    </script>

    <title>KeyBox - Manage Systems</title>
</head>
<body>

    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">

            <s:if test="script!=null">
                <h3>Execute Script on Systems</h3>
                <p>Run <b> <a data-toggle="modal" data-target="#script_dialog"><s:property value="script.displayNm"/></a></b> on the selected systems below</p>

                <div id="script_dialog" class="modal fade">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                                <h4 class="modal-title">View Script: <s:property value="script.displayNm"/></h4>
                            </div>
                            <div class="modal-body">
                                <div class="row">
                                    <pre><s:property value="script.script"/></pre>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Close</button>

                            </div>
                        </div>
                    </div>
                </div>
            </s:if>
            <s:else>
                <h3>Composite SSH Terminals</h3>
                <p>Select the systems below to generate composite SSH sessions in multiple terminals</p>
            </s:else>
		<s:actionerror/>
        <s:form id="viewSystems" action="viewSystems" theme="simple">
        <s:hidden name="sortedSet.orderByDirection"/>
        <s:hidden name="sortedSet.orderByField"/>
        <s:if test="script!=null">
            <s:hidden name="script.id"/>
        </s:if>
        <s:if test="profileList!= null && !profileList.isEmpty()">
           <div>
                     <table>
                        <tr>
                            <td class="align_left">
                                <s:select name="sortedSet.filterMap['%{@com.keybox.manage.db.SystemDB@FILTER_BY_PROFILE_ID}']" listKey="id" listValue="nm"
                                class="view_frm_select"
                                list="profileList"
                                headerKey=""
                                headerValue="-Select Profile-"/>
                            </td>
                            <td>
                                <div id="view_btn" class="btn btn-default">Filter</div>
                            </td>
                        </tr>
                     </table>
           </div>
        </s:if>
        </s:form>
        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">

              <s:form action="selectSystemsForCompositeTerms" id="select_frm" theme="simple">
                   <s:if test="script!=null">
                        <s:hidden name="script.id"/>
                 </s:if>
                <div class="scrollWrapper">
                <table class="table-striped scrollableTable">
                    <thead>

                    <tr>
                        <th><s:checkbox name="systemSelectAll" cssClass="systemSelect"
                                            theme="simple"/></th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Display
                            Name
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_INSTANCE_ID"/>" class="sort"> 
                    		AWS Instance ID
                    	</th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host
                        </th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_STATUS"/>" class="sort">Status
                    	</th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>

                    <s:iterator var="system" value="sortedSet.itemList" status="stat">
                        <tr>
                            <td>
                            	<s:if test="(enabled && applicationKey.enabled)|| ismanager">
                            		<s:checkboxlist name="systemSelectId" list="#{id:''}" cssClass="systemSelect"
                                                theme="simple"/>
                            	</s:if>
                            </td>
                            <td>
                                <s:property value="displayNm"/>
                            </td>
                            <td><s:property value="instance"/></td>
                            <td><s:property value="user"/></td>
                            <td><s:property value="host"/>:<s:property value="port"/></td>
                            <td>
                            	<s:if test="!(enabled && applicationKey.enabled)">
                            		<div class="error">Temporarily disabled</div>
                            	</s:if>
                            	
	                            <s:if test="statusCd=='INITIAL'">
	                                <div class="warning">Not Started</div>
	                            </s:if>
	                            <s:elseif test="statusCd=='AUTHFAIL'">
	                                <div class="warning">Authentication Failed</div>
	                            </s:elseif>
	                            <s:elseif test="statusCd=='HOSTFAIL'">
				                	<div class="error">DNS Lookup Failed</div>
				                </s:elseif>
				                <s:elseif test="statusCd=='PRIVATKEYFAIL'">
				                	<div class="error">System Key disable</div>
				                </s:elseif>
	                            <s:elseif test="statusCd=='KEYAUTHFAIL'">
	                                <div class="warning">Passphrase Authentication Failed</div>
	                            </s:elseif>
	                            <s:elseif test="statusCd=='GENERICFAIL'">
	                                <div class="error">Failed</div>
	                            </s:elseif>
	                            <s:elseif test="statusCd=='SUCCESS' || statusCd=='RUNNING'">
	                                <div class="success">Success</div>
	                            </s:elseif>
	                            <s:elseif test="statusCd=='PENDING'">
	                                <div class="warning">Pending</div>
	                            </s:elseif>
	                            <s:elseif test="statusCd=='SHUTTING-DOWN' || statusCd=='TERMINATED' || statusCd=='STOPPING' || statusCd=='STOPPED'">
	                                <div class="error">Server Down</div>
	                            </s:elseif>
                        	</td>
                            <td>
                                <button type="button" class="btn btn-default ssh_btn" data-toggle="modal" data-target="#ssh_access_<s:property value="id"/>">Your SSH Access</button>
                            </td>   
                        </tr>
                    </s:iterator>
                    </tbody>
                </table>
                </div>
            </s:form>
        </s:if>
        <s:if test="script!=null && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="btn btn-default select_frm_btn spacer spacer-bottom">Execute Script</div>
        </s:if>
        <s:elseif test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="btn btn-default select_frm_btn spacer spacer-bottom">Create SSH Terminals</div>
        </s:elseif>
        <s:else>
            <div class="actionMessage">
                <p class="error">Systems not available
                    <s:if test="%{#session.userType==\"M\"}">
                    (<a href="../manage/viewSystems.action">Manage Systems</a>)
                    </s:if>.
                 </p>
                </div>
        </s:else>
        
        <s:iterator var="system" value="sortedSet.itemList" status="stat">
            <div id="ssh_access_<s:property value="id"/>" class="modal fade">
                <div class="modal-dialog" style="width: 800px;">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <h4 class="modal-title">Connect To Your Instance</h4>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <s:form>
                                    <H4>To access your instance:</H4>
                                    <ol>
                                        <li>Locate your private key file(s).
                                        Keybox provides the file names based on the keys you've added.</li>
                                        <li>Your key must not be publicly viewable for SSH to work if you use an ssh-client within Unix/Linux environment.<br><br>
                                            Use this command if needed:
                                            
                                            <s:if test="publicKeyList!= null && !publicKeyList.isEmpty()">
                                                <s:iterator var="system" value="publicKeyList" status="stat">
                                                    <pre>chmod 400 <s:property value="keyNm"/>.key</pre>
                                                </s:iterator>
                                            </s:if>
                                            <s:else>
                                                <pre>chmod 400 excample.key</pre>
                                            </s:else>
                                            
                                            Ensure that  ~./.ssh folder is set to 600
                                                            
                                        <li>Example:<br>
                                            <s:if test="publicKeyList!= null && !publicKeyList.isEmpty()">
                                                <s:iterator var="system" value="publicKeyList" status="stat">
                                                    Profile 
                                                    	<s:if test="profile== null">
    	                                                	All Systems:
                                                    	</s:if>
                                                    	<s:else>
	                                                    	<s:property value="profile.nm"/>:
                                                    	</s:else>
                                                    <br>
                                                    <pre>ssh -i <s:property value="keyNm"/>.key <s:property value="user"/>@<s:property value="host"/></pre>
                                                </s:iterator>
                                            </s:if>
                                            <s:else>
                                                <pre>ssh -i excample.key <s:property value="user"/>@<s:property value="host"/></pre>
                                            </s:else>
                                        </li>
                                    </ol>
                                </s:form>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal" aria-hidden="true">Close</button>
                        </div>
                    </div>
                </div>
            </div>
       </s:iterator>
    </div>
</body>
</html>
