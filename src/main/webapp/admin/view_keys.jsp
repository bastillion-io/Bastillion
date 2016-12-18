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

            //call delete action
            $(".del_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deletePublicKey.action?publicKey.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>&_csrf=<s:property value="#session['_csrf']"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function () {
                $(this).parents('.modal').find('form').submit();
            });

            //regenerate auth keys btn
            $(".gen_auth_keys_btn").button().click(function () {
                $("#gen_auth_keys").submit();
            });


            $(".sort,.sortAsc,.sortDesc").click(function () {
                var id = $(this).attr('id')

                if ($('#viewKeys_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewKeys_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewKeys_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewKeys_sortedSet_orderByField').attr('value', id);
                $("#viewKeys").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

            <s:if test="#session['privateKey']!=null">
                window.location='../admin/downloadPvtKey.action?publicKey.keyNm=<s:property value="%{#parameters.keyNm}" escapeJavaScript="true"/>&_csrf=<s:property value="#session['_csrf']"/>';
            </s:if>
            
            $('.new_key_label a').click(function() {
                hideNewKeyInputs();
            });

            $('.existing_key_label a').click(function() {
                hideExistingKeyInputs();
            });
            
            function hideNewKeyInputs() {
                //hide new key input
                $('.new_key').closest('tr').hide();
                $('.new_key_label').hide();
                //show existing key inputs
                $('.existing_key_label').show();
                $('.existing_key').closest('tr').show();
            }
            
            function hideExistingKeyInputs() {
                //hide existing key inputs
                $('.existing_key').closest('tr').hide();
                $('.existing_key_label').hide();
                //reset existing key values
                $('select.existing_key').val('');
                //show new key inputs
                $('.new_key_label').show();
                $('.new_key').closest('tr').show();
            }
            
            <s:if test="existingKeyId!=null">
                hideNewKeyInputs();
            </s:if>
            <s:else>
                hideExistingKeyInputs();
            </s:else>
            

        });
    </script>

    <s:if test="fieldErrors.size > 0 || actionErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function () {
                <s:if test="publicKey.id>0">
                $("#edit_dialog_<s:property value="publicKey.id"/>").modal();
                </s:if>
                <s:else>
                $("#add_dialog").modal();
                </s:else>
            });
        </script>
    </s:if>


    <title>KeyBox - Manage Keys</title>

</head>
<body>


<jsp:include page="../_res/inc/navigation.jsp"/>

<div class="container">
    <s:form action="viewKeys">
        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
        <s:hidden name="sortedSet.orderByDirection"/>
        <s:hidden name="sortedSet.orderByField"/>
    </s:form>

    <h3>Manage SSH Keys</h3>

    <p>Add / Delete SSH keys for current user.</p>

    <s:if test="%{#session.userType==\"M\" || (profileList!= null && !profileList.isEmpty()) }">
        
        <s:if test="%{#session.userType==\"M\"}">
            <table>
                <tr>
                    <td class="align_left">
                        <a href="../manage/viewKeys.action?_csrf=<s:property value="#session['_csrf']"/>" class="btn btn-danger" >View / Disable SSH Keys</a>
                    </td>
                </tr>
            </table>
        </s:if>

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
        <div class="scrollWrapper">
            <table class="table-striped scrollableTable" >
                <thead>

                <tr>

                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_KEY_NM"/>" class="sort">Key
                        Name
                    </th>

                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_PROFILE"/>" class="sort">
                        Profile
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_TYPE"/>" class="sort">
                        Type
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_FINGERPRINT"/>" class="sort">
                        Fingerprint
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_CREATE_DT"/>" class="sort">
                        Created
                    </th>
                    <th>&nbsp;</th>
                </tr>
                </thead>
                <tbody>
                <s:iterator var="publicKey" value="sortedSet.itemList" status="stat">
                    <tr>
                        <td><s:property value="keyNm"/></td>
                        <td>
                            <s:if test="%{#publicKey.profile==null}">
                                All Systems
                            </s:if>
                            <s:else>
                                <s:property value="profile.nm"/>
                            </s:else>
                        </td>
                        <td>[ <s:property value="type"/> ]</td>
                        <td><s:property value="fingerprint"/></td>
                        <td><s:date name="createDt" nice="true"/></td>
                        <td>
                            <div>
                                <s:if test="!forceUserKeyGenEnabled">
                                    <button class="btn btn-default spacer spacer-left" data-toggle="modal"
                                            data-target="#edit_dialog_<s:property value="id"/>">Edit
                                    </button>
                                </s:if>
                                    <button id="del_btn_<s:property value="id"/>" class="btn btn-default del_btn spacer spacer-right">Delete
                                    </button>
                                &nbsp;&nbsp;&nbsp;
                                <div style="clear:both"></div>
                            </div>
                        </td>
                    </tr>
                </s:iterator>
                </tbody>
            </table>
            </div>
        </s:if>


        <button class="btn btn-default add_btn spacer spacer-bottom" data-toggle="modal" data-target="#add_dialog">Add SSH Key</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <div class="new_key_label">
                                <div class="alert alert-success">
                                    .. or <strong>select an <a href="#" class="alert-link">existing key</a></strong>
                                </div>
                                <s:if test="forceUserKeyGenEnabled">
                                    <h4 class="modal-title">Generate &amp; Download an SSH Key</h4>
                                </s:if>
                                <s:else>
                                    <h4 class="modal-title">Add Public SSH Key</h4>
                                </s:else>
                            </div>

                            <div class="existing_key_label">
                                <div class="alert alert-success">
                                    .. or <strong>create a <a href="#" class="alert-link">new key</a></strong>
                                </div>
                                <h4 class="modal-title">Select &amp; Assign an SSH Key</h4>
                            </div>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:actionerror/>
                            <s:form action="savePublicKey" class="save_public_key_form_add" autocomplete="off">
                                <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                <s:textfield name="publicKey.keyNm" label="Key Name" size="15"/>
                                <s:if test="%{#session.userType==\"M\"}">
                                    <s:select name="publicKey.profile.id" list="profileList" headerKey=""
                                              headerValue="All Systems"
                                              listKey="id" listValue="%{nm}" label="Profile" value="%{profile.id}"/>
                                </s:if>
                                <s:else>
                                    <s:select name="publicKey.profile.id" list="profileList"
                                              listKey="id" listValue="%{nm}" label="Profile" value="%{profile.id}"/>
                                </s:else>
                                <s:if test="forceUserKeyGenEnabled">
                                    <s:password class="new_key" name="publicKey.passphrase" label="Passphrase"/>
                                    <s:password class="new_key" name="publicKey.passphraseConfirm" label="Confirm Passphrase"/>
                                </s:if>
                                <s:else>
                                    <s:textarea class="new_key" name="publicKey.publicKey" label="Public Key" rows="8" cols="55"/>
                                </s:else>
                                <s:select class="existing_key" name="existingKeyId" list="userPublicKeyList" listKey="id" listValue="%{keyNm + ' ('+fingerprint+')'}" label="Existing Key" value="%{existingKeyId}" headerKey=""
                                headerValue="-Select Key-"/>
                                <s:hidden name="sortedSet.orderByDirection"/>
                                <s:hidden name="sortedSet.orderByField"/>
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


    <s:if test="!forceUserKeyGenEnabled">
        <s:iterator var="publicKey" value="sortedSet.itemList" status="stat">
            <div id="edit_dialog_<s:property value="id"/>" class="modal fade">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <div class="new_key_label">
                                <div class="alert alert-success">
                                    .. or <strong>select an <a href="#" class="alert-link">existing key</a></strong>
                                </div>
                                <h4 class="modal-title">Edit Public SSH Key</h4>
                            </div>

                            <div class="existing_key_label">
                                <div class="alert alert-success">
                                    .. or <strong>create a <a href="#" class="alert-link">new key</a></strong>
                                </div>
                                <h4 class="modal-title">Select &amp; Assign an SSH Key</h4>
                            </div>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <s:actionerror/>
                                <s:form action="savePublicKey" id="save_public_key_form_edit_%{id}" autocomplete="off">
                                    <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                                    <s:hidden name="publicKey.id" value="%{id}"/>
                                    <s:textfield name="publicKey.keyNm" value="%{keyNm}" label="Key Name" size="15"/>
                                    <s:if test="%{#session.userType==\"M\"}">
                                        <s:select name="publicKey.profile.id" list="profileList" headerKey=""
                                                  headerValue="All Systems"
                                                  listKey="id" listValue="%{nm}" label="Profile" value="%{profile.id}"/>
                                    </s:if>
                                    <s:else>
                                        <s:select name="publicKey.profile.id" list="profileList"
                                                  listKey="id" listValue="%{nm}" label="Profile" value="%{profile.id}"/>
                                    </s:else>
                                    <s:textarea class="new_key" name="publicKey.publicKey" value="%{publicKey}" label="Public Key"
                                                rows="8" cols="55"/>
                                    <s:select class="existing_key" name="existingKeyId" list="userPublicKeyList" listKey="id" listValue="%{keyNm + ' ('+fingerprint+')'}" label="Existing Key" value="%{existingKeyId}"   headerKey=""
                                              headerValue="-Select Key-"/>
                                    <s:hidden name="sortedSet.orderByDirection"/>
                                    <s:hidden name="sortedSet.orderByField"/>
                                </s:form>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel
                            </button>
                            <button type="button" class="btn btn-default submit_btn">Submit</button>
                        </div>
                    </div>
                </div>
            </div>
        </s:iterator>
    </s:if>


    </s:if>
    <s:else>
        <div class="actionMessage">
            <p class="error">No profiles have been assigned</p>
        </div>
    </s:else>
</div>

</body>
</html>
