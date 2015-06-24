<%
    /**
     * Copyright 2015 Robert Vorkoeper - robert-vor@gmx.de
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
     	
	     function populateKeyNames(awsCredentials_id,ec2Region) {
	         $.getJSON('getKeyPairJSON.action?ec2Key.awsCredentials.id='+$("#saveEC2Key_ec2Key_awsCredentials_id").val()+'&ec2Key.ec2Region='+$("#saveEC2Key_ec2Key_ec2Region").val(), function(result) {
	
	           $("#saveEC2Key_ec2Key_keyname option").remove();
	             var options = $("#saveEC2Key_ec2Key_keyname");
	             options.append($("<option />").val('').text('-Select Key Name-'));
	             $.each(result, function() {
	                 if(this.keyName!=null){
	                     options.append($("<option />").val(this.keyName).text(this.keyName));
	                 }
	             });
	
	           $("#saveEC2Key_ec2Key_keyname option[value='<s:property value="ec2Key.keyname"/>']").attr("selected",true);
	         });
	     }
          
        $(document).ready(function () {

        	//submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).parents('.modal').find('form').submit();
            });
        	
          	//delete action for System Key
            $(".del_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteApplicationKey.action?applicationKey.id=' + id
            });
        	
          	//delete action for EC2 Key
            $(".del_EC2Key_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_EC2Key_btn_", "");
                window.location = 'deleteEC2Key.action?ec2Key.id=' + id
            });
          	
        	//Disable System Key
            $(".disable_btn").button().click(function () {
                var id = $(this).attr('id').replace("disable_btn_", "");
                window.location = 'disableApplicationKey.action?applicationKey.id=' + id
			});
            
          	//Enable System Key
            $(".enable_btn").button().click(function () {
                var id = $(this).attr('id').replace("enable_btn_", "");
                window.location = 'enableApplicationKey.action?applicationKey.id=' + id
            });
   		
        });
    </script> 

	<s:if test="fieldErrors.size > 0 || actionErrors.size > 0">
		<script type="text/javascript">
            $(document).ready(function () {
                <s:if test="applicationKey.ec2Region == 'NO_EC2_REGION'">
                $("#add_dialog").modal();
                </s:if>
                <s:else>
                populateKeyNames();
                $("#add_EC2dialog").modal();
                </s:else>
            });
        </script>
	</s:if>

    <title>KeyBox - View / Create System SSH Keys</title>

</head>
<body>


<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">

    <h3>View / Create System SSH Keys</h3>
    <p>Mange Keys used for initial server connection. When disabling / deleting a key please ensure to update all systems you have assigned the disabled key to.(Manage Systems)</p>
        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <div class="scrollWrapper">
            <table class="table-striped scrollableTable" >
                <thead>
	                <tr>
	                    <th>
	                    	KeyName
	                    </th>
	                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_USERNAME"/>" class="sort" --%>>
	                        Username
	                    </th>
	                    <%-- <th id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_SYSTEM"/>" class="sort">
	                        System
	                    </th> --%>
	                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_TYPE"/>" class="sort" --%>>
	                        Type
	                    </th>
	                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_FINGERPRINT"/>" class="sort" --%>>
	                        Fingerprint
	                    </th>
	                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_CREATE_DT"/>" class="sort" --%>>
	                        Created
	                    </th>
	                    <th> <!-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_ENABLED"/>" class="sort" > -->
	                    	Action
	                    </th>
	                </tr>
                </thead>
                <tbody>
                <s:iterator var="applicationKey" value="sortedSet.itemList" status="stat">
                    <tr>
                    	<td><s:property value="keyname"/></td>
                        <td><s:property value="username"/></td>
                        <td>[ <s:property value="type"/> ]</td>
                        <td><s:property value="fingerprint.fingerprint"/></td>
                        <td><s:date name="createDt"/></td>
                        <td>
                        	<s:if test="userId>0">
	                            <div>
	                            	<button type="button" class="btn btn-default del_ms_btn" data-toggle="modal" data-target="#del_ms_<s:property value="id"/>">Delete</button>
	                                <s:if test="%{enabled}">
	                                     <button class="btn btn-default btn-danger btn-disable_enable spacer spacer-left disable_btn" data-toggle="modal"
	                                            id="disable_btn_<s:property value="id"/>">Disable
	                                     </button>
	                                </s:if>
	                                <s:else>
	                                     <button class="btn btn-default btn-success btn-disable_enable spacer spacer-left enable_btn" data-toggle="modal"
	                                            id="enable_btn_<s:property value="id"/>">Enable
	                                     </button>
	                                </s:else>
	                             </div>
                             </s:if>
                        </td>
                    </tr>
                </s:iterator>
                </tbody>
            </table>
            </div>
        </s:if>
        
        <button class="btn btn-default add_btn spacer spacer-bottom" data-toggle="modal" data-target="#add_dialog" style="min-width:120px">Add System Key</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Add System SSH Key</h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:actionerror/>
                            <s:form action="saveApplicationKey" autocomplete="off" method="post" enctype="multipart/form-data">
                            	<s:textfield name="applicationKey.keyname" label="Name"/>
                            	<s:file name="appKeyFile" accept=".key,.pem,.ppk" label="Privat Key File" />
                            	
                            	<s:password name="applicationKey.passphrase" label="New Passphrase"/>
                                <s:password name="applicationKey.passphraseConfirm" label="Confirm New Passphrase"/>
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
        
        <s:iterator var="appKey" value="sortedSet.itemList" status="stat">
        	<div id="del_ms_<s:property value="id"/>" class="modal fade">
        		<div class="modal-dialog" style="width: 450px;">
        			<div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <h4 class="modal-title">Delete System Key</h4>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <s:form>
                                    The initial system key is on the systems in use:
                              	    <ul>
                                    	<s:iterator value="useOnSystem" status="statusVar">
                                        	<li><s:property/></li>
                                        </s:iterator>
                                    </ul>
                                    <br>
                                    Please adjust the systems after deletion the System Key
                                </s:form>
                            </div>
                        </div>
                        <div class="modal-footer">
                        	
                            <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                            <button type="button" class="btn btn-default del_btn" id="del_btn_<s:property value="id"/>">Delete</button>
                        </div>
        			</div>
        		</div>
        	</div>
        </s:iterator>

		<h3>Manage EC2 Keys</h3>
		<s:if test="awsCredList.isEmpty()">
	        <div class="actionMessage">
	            <p class="error">
	         EC2 Keys not available (<a href="viewAWSCred.action">Set AWS Credentials</a>).
	            </p>
	        </div>
	    </s:if>
	    <s:else>
			<p>Import and register EC2 keys below. An EC2 server will only show up after its private key has been imported</p>
			<s:if test="sortedEC2Set.itemList!= null && !sortedEC2Set.itemList.isEmpty()">
	            <div class="scrollWrapper">
	            <table class="table-striped scrollableTable" >
	                <thead>
		                <tr>
		                    <th>
		                    	KeyName
		                    </th>
		                    
		                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_USERNAME"/>" class="sort" --%>>
		                        Username
		                    </th>
		                    <%-- <th id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_SYSTEM"/>" class="sort">
		                        System
		                    </th> --%>
		                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_TYPE"/>" class="sort" --%>>
		                        Type
		                    </th>
		                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_FINGERPRINT"/>" class="sort" --%>>
		                        Fingerprint
		                    </th>
		                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_CREATE_DT"/>" class="sort" --%>>
		                        Created
		                    </th>
		                    <th <%-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_EC2REGION"/>" class="sort" --%>>
		                        EC2 Region
		                    </th>
		                    <th> <!-- id="<s:property value="@com.keybox.manage.db.ApplicationDB@SORT_BY_ENABLED"/>" class="sort" > -->
		                    	Action
		                    </th>
		                </tr>
	                </thead>
	                <tbody>
	                <s:iterator var="applicationKey" value="sortedEC2Set.itemList" status="stat">
	                    <tr>
	                    	<td><s:property value="keyname"/></td>
	                        <td><s:property value="username"/></td>
	                        <td>[ <s:property value="type"/> ]</td>
	                        <td><s:property value="fingerprint.fingerprint"/></td>
	                        <td><s:date name="createDt"/></td>
	                        <td>
		                        <s:set var="ec2Region" value="%{ec2Region}"/>
		                        <s:property value="%{ec2RegionMap.get(#ec2Region)}"/>
	                        </td>
	                        <td>
	                        	<s:if test="userId>0">
		                            <div>
		                            	<button type="button" class="btn btn-default del_EC2Key_btn" data-toggle="modal" id="del_EC2Key_btn_<s:property value="id"/>">Delete</button>
		                                <s:if test="%{enabled}">
		                                     <button class="btn btn-default btn-danger btn-disable_enable spacer spacer-left disable_btn" data-toggle="modal"
		                                            id="disable_btn_<s:property value="id"/>">Disable
		                                     </button>
		                                </s:if>
		                                <s:else>
		                                     <button class="btn btn-default btn-success btn-disable_enable spacer spacer-left enable_btn" data-toggle="modal"
		                                            id="enable_btn_<s:property value="id"/>">Enable
		                                     </button>
		                                </s:else>
		                             </div>
	                             </s:if>
	                        </td>
	                    </tr>
	                </s:iterator>
	                </tbody>
	            </table>
	            </div>
	        </s:if>
			<button class="btn btn-default add_btn spacer spacer-bottom" data-toggle="modal" data-target="#add_EC2dialog" style="min-width:120px">Add EC2 Key</button>
	        <div id="add_EC2dialog" class="modal fade">
	            <div class="modal-dialog">
	                <div class="modal-content">
	                    <div class="modal-header">
	                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
	                        <h4 class="modal-title">Add System EC2 Key</h4>
	                    </div>
	                    <div class="modal-body">
	                        <div class="row">
	                            <s:actionerror/>
	                            <s:form action="saveEC2Key" autocomplete="off" method="post" enctype="multipart/form-data"> 
	                            	<s:if test="awsCredList.size()==1">
	                                    <s:hidden name="ec2Key.awsCredentials.id" value="%{awsCredList.get(0).getId()}"/>
	                                </s:if>
	                                <s:else>
	                                    <s:select name="ec2Key.awsCredentials.id" list="awsCredList" listKey="id" listValue="accessKey" label="Access Key" />
	                                </s:else>
	                                <s:select name="ec2Key.ec2Region"  list="ec2RegionMap" label="EC2 Region" headerKey="" headerValue="-Select-" onchange="populateKeyNames();" />
	                                <s:select name="ec2Key.keyname" label="Key Name" list="#{'':'-Select Region Above-'}"/>
	                            	<s:file name="ec2KeyFile" accept=".key,.pem,.ppk" label="Privat Key File" />
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
       	</s:else>
</div>

</body>
</html>
