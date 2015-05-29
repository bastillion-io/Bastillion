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
                
                $("#add_dialog").modal();
                
            });
        </script>
	</s:if>

    <title>KeyBox - View / Create System SSH Keys</title>

</head>
<body>


<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">

    <h3>View / Create System SSH Keys</h3>
    <p>Mange Keys used for initial server connection. When disabling / deleting a key please ensure to update all systems you have assigned the disabled key to.</p>
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
        
        <button class="btn btn-default add_btn spacer spacer-bottom" data-toggle="modal" data-target="#add_dialog">Add System Key</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog" style="width: 450px;">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Add System SSH Key</h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:actionerror/>
                            <s:form action="saveApplicationKey" class="save_Application_key_form_add" autocomplete="off" method="post" enctype="multipart/form-data">
                            	<s:textfield name="applicationKey.keyname" label="Name"/>
                            	<s:file name="appKeyFile" accept=".key,.pem,.ppk" label="Privat Key File" />
                            	
                            	<s:password class="new_key" name="applicationKey.passphrase" label="Passphrase"/>
                                <s:password class="new_key" name="applicationKey.passphraseConfirm" label="Confirm Passphrase"/>
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

</div>

</body>
</html>
