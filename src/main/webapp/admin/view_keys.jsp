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
            //Select the first textbox in modal
            $('.modal').on('shown.bs.modal', function () {
                $('input:text:visible:first').focus();
            });
            
            //call delete action
            $(".del_btn").button().click(function () {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deletePublicKey.action?publicKey.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
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
                var id = $(this).attr('id');

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


            $('.scrollableTable').tableScroll({height: 500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
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
        <s:hidden name="sortedSet.orderByDirection"/>
        <s:hidden name="sortedSet.orderByField"/>
    </s:form>

    <h3>Manage SSH Keys</h3>

    <p>Add / Delete keys and assign to profile in order to be set on systems.</p>

    <s:if test="%{#session.userType==\"M\" || (profileList!= null && !profileList.isEmpty()) }">

        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <table class="table-striped scrollableTable">
                <thead>
                <tr>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_KEY_NM"/>" class="sort">Key
                        Name
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_PROFILE"/>" class="sort">
                        Profile
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_KEY_TP"/>" class="">
                        Key Type
                    </th>
                    <th id="<s:property value="@com.keybox.manage.db.PublicKeyDB@SORT_BY_KEY_FP"/>" class="">
                        SSH Fingerprint
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
                        <td>
                            <s:if test="%{#publicKey.keyTp==null}">
                                ---
                            </s:if>
                            <s:else>
                                <s:property value="keyTp"/>
                            </s:else>
                        </td>
                        <td>
                            <s:if test="%{#publicKey.keyFp==null}">
                                ---
                            </s:if>
                            <s:else>
                                <s:property value="keyFp"/>
                            </s:else>
                        </td>
                        <td>
                            <div style="width:150px">
                                <button class="btn btn-default" data-toggle="modal"
                                        data-target="#edit_dialog_<s:property value="id"/>" style="float:left">Edit
                                </button>

                                <button id="del_btn_<s:property value="id"/>" class="btn btn-default del_btn"
                                        style="float:left">Delete
                                </button>
                                &nbsp;&nbsp;&nbsp;
                                <div style="clear:both"></div>
                            </div>
                        </td>
                    </tr>
                </s:iterator>
                </tbody>
            </table>
        </s:if>


        <button class="btn btn-default add_btn" data-toggle="modal" data-target="#add_dialog">Add Public Key</button>
        <div id="add_dialog" class="modal fade">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                        <h4 class="modal-title">Add Public Key</h4>
                    </div>
                    <div class="modal-body">
                        <div class="row">
                            <s:form action="savePublicKey" class="save_public_key_form_add" autocomplete="off">
                                <s:textfield name="publicKey.keyNm" label="Key Name" size="15" placeholder="Mandatory field"/>
                                <s:if test="%{#session.userType==\"M\"}">
                                    <s:select name="publicKey.profile.id" list="profileList" headerKey=""
                                              headerValue="All Systems"
                                              listKey="id" listValue="%{nm}" label="Profile" value="%{profile.id}"/>
                                </s:if>
                                <s:else>
                                    <s:select name="publicKey.profile.id" list="profileList"
                                              listKey="id" listValue="%{nm}" label="Profile" value="%{profile.id}"/>
                                </s:else>
                                <s:textarea name="publicKey.publicKey" rows="15" cols="55" placeholder="Mandatory field"/>
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


        <s:iterator var="publicKey" value="sortedSet.itemList" status="stat">
            <div id="edit_dialog_<s:property value="id"/>" class="modal fade">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                            <h4 class="modal-title">Edit Public Key</h4>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <s:form action="savePublicKey" id="save_public_key_form_edit_%{id}" autocomplete="off">
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
                                    <s:textarea name="publicKey.publicKey" value="%{publicKey}" label="Public Key"
                                                rows="15" cols="55"/>
                                    <s:textfield name="publicKey.keyTp" value="%{keyTp}" label="Key Type" size="10" disabled="true"/>
                                    <s:textfield name="publicKey.keyFp" value="%{keyFp}" label="Fingerprint" size="40" disabled="true"/>
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
    <s:else>
        <div class="actionMessage">
            <p class="error">No profiles have been assigned</p>
        </div>
    </s:else>
</div>

</body>
</html>
