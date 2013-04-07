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
<s:set id="selectForm"><s:property value="#parameters['selectForm']"/></s:set>
<s:set id="terms"><s:property value="#parameters['terms']"/></s:set>
<!DOCTYPE html>
<html>
<head>

    <jsp:include page="../_res/inc/header.jsp"/>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#add_dialog").dialog({
                autoOpen: false,
                height: 255,
                width: 400,
                modal: true
            });
            $(".edit_dialog").dialog({
                autoOpen: false,
                height: 255,
                width: 400,
                modal: true
            });
            $("#script_dia").dialog({
                autoOpen: false,
                height: 350,
                width: 350,
                modal: true
            });
            //open add dialog
            $("#add_btn").button().click(function() {
                $("#add_dialog").dialog("open");
            });
            //open edit dialog
            $(".edit_btn").button().click(function() {
                //get dialog id to open
                var id = $(this).attr('id').replace("edit_btn_", "");
                $("#edit_dialog_" + id).dialog("open");

            });
            $("#script_btn").click(function() {
                $("#script_dia").dialog("open");
            });
            //call delete action
            $(".del_btn").button().click(function() {
                var id = $(this).attr('id').replace("del_btn_", "");
                window.location = 'deleteProfile.action?profile.id=' + id + '&sortedSet.orderByDirection=<s:property value="sortedSet.orderByDirection" />&sortedSet.orderByField=<s:property value="sortedSet.orderByField"/>';
            });
            //submit add or edit form
            $(".submit_btn").button().click(function() {
                $(this).prev().submit();
            });
            //close all forms
            $(".cancel_btn").button().click(function() {
                $("#add_dialog").dialog("close");
                $(".edit_dialog").dialog("close");
            });
            $(".select_frm_btn").button().click(function() {
                //change form action if executing script
                <s:if test="script!=null||#terms=='true'">
                $("#select_frm").attr("action", "selectProfilesForCompositeTerms.action");
                </s:if>

                $("#select_frm").submit();
            });
            //select all check boxs
            $("#select_frm_profileSelectAll").click(function() {
                if ($(this).is(':checked')) {
                    $(".profileSelect").attr('checked', true);
                } else {
                    $(".profileSelect").attr('checked', false);
                }
            });
            $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewProfiles_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewProfiles_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewProfiles_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewProfiles_sortedSet_orderByField').attr('value', id);
                $("#viewProfiles").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>

            $('.scrollableTable').tableScroll({height:500});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");
        });
    </script>
    <s:if test="fieldErrors.size > 0">
        <script type="text/javascript">
            $(document).ready(function() {
                <s:if test="profile.id>0">
                $("#edit_dialog_<s:property value="profile.id"/>").dialog("open");
                </s:if>
                <s:else>
                $("#add_dialog").dialog("open");
                </s:else>
            });
        </script>
    </s:if>

    <title>KeyBox - Manage System Profiles</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">
        <s:form action="viewProfiles">
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
            <s:hidden name="selectForm"/>
            <s:hidden name="terms"/>
            <s:if test="script!=null">
                <s:hidden name="script.id"/>
            </s:if>
        </s:form>


        <s:if test="#selectForm=='true'">
            <s:if test="script!=null">
                <h3>Execute Script on System Profiles</h3>
                <jsp:include page="../_res/inc/scripts_nav.jsp"/>
                <p>Run <b><a id="script_btn" href="#"><s:property value="script.displayNm"/></a></b> on the selected
                    system profiles below
                </p>

                <div id="script_dia" title="View Script">
                    <pre><s:property value="script.script"/></pre>
                </div>
            </s:if>
            <s:elseif test="#terms=='true'">
                <h3>Composite SSH Terminals</h3>
                <jsp:include page="../_res/inc/terms_nav.jsp"/>
                <p>Select the system profiles below to generate composite SSH sessions in multiple terminals</p>
            </s:elseif>
            <s:else>
                <h3>Distribute Authorized Key for System Profiles</h3>
                <jsp:include page="../_res/inc/key_nav.jsp"/>
                <p>Select the system profiles below to generate and set the authorized key file</p>
            </s:else>

        </s:if>
        <s:else>
            <h3>Manage System Profiles</h3>

            <p>Add / Delete profiles or select a profile below to assign systems to that profile.</p>
        </s:else>


        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <s:form action="selectProfilesForAuthKeys" id="select_frm" theme="simple">
                <s:if test="script!=null">
                    <s:hidden name="script.id"/>
                </s:if>
                <table class="vborder scrollableTable">
                    <thead>


                    <tr>
                        <s:if test="#selectForm=='true'">
                            <th><s:checkbox name="profileSelectAll" cssClass="profileSelect"
                                            theme="simple"/></th>
                        </s:if>
                        <th id="<s:property value="@com.keybox.manage.db.ProfileDB@SORT_BY_PROFILE_NM"/>" class="sort">
                            Profile Name
                        </th>
                        <s:if test="#selectForm=='true'"></s:if>
                        <s:else>
                            <th>&nbsp;</th>
                        </s:else>
                    </tr>
                    </thead>
                    <tbody>


                    <s:iterator var="profile" value="sortedSet.itemList" status="stat">
                        <tr>
                            <s:if test="#selectForm=='true'">
                                <td>
                                    <s:checkboxlist name="profileSelectId" list="#{id:''}" cssClass="profileSelect"
                                                    theme="simple"/>
                                </td>
                            </s:if>
                            <td>
                                <a href="viewProfileSystems.action?profile.id=<s:property value="id"/>"
                                   title="Manage Systems in Profile">
                                    <s:property value="nm"/>
                                </a>
                            </td>

                            <s:if test="#selectForm=='true'"></s:if>
                            <s:else>
                                <td>
                                    <div id="edit_btn_<s:property value="id"/>" class="edit_btn" style="float:left">Edit
                                    </div>
                                    <div id="del_btn_<s:property value="id"/>" class="del_btn" style="float:left">Delete
                                    </div>
                                    <div style="clear:both"/>
                                </td>
                            </s:else>
                        </tr>


                    </s:iterator>
                    </tbody>
                </table>
            </s:form>
        </s:if>
        <s:if test="#selectForm=='true'">
            <s:if test="script!=null && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <div class="select_frm_btn">Execute Script</div>
            </s:if>
            <s:elseif test="#terms=='true' && sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <div class="select_frm_btn">Create SSH Terminals</div>
            </s:elseif>
            <s:elseif test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
                <div class="select_frm_btn">Distribute Authorized Keys</div>
            </s:elseif>
            <s:else>
                <div class="error">There are no profiles defined. New profiles may be defined <a
                        href="viewProfiles.action">here</a>.
                </div>
            </s:else>
        </s:if>
        <s:else>

            <div id="add_btn">Add Profile</div>
            <div id="add_dialog" title="Add Profile">
                <s:form action="saveProfile" class="save_profile_form_add">
                    <s:textfield name="profile.nm" label="Profile Name" size="15"/>
                    <s:textarea name="profile.desc" label="Profile Description" rows="5" cols="25"/>
                    <s:hidden name="sortedSet.orderByDirection"/>
                    <s:hidden name="sortedSet.orderByField"/>
                    <s:hidden name="selectForm"/>
                </s:form>
                <div class="submit_btn">Submit</div>
                <div class="cancel_btn">Cancel</div>
            </div>


            <s:iterator var="profile" value="sortedSet.itemList" status="stat">
                <div id="edit_dialog_<s:property value="id"/>" title="Edit Profile" class="edit_dialog">
                    <s:form action="saveProfile" id="save_profile_form_edit_%{id}">
                        <s:textfield name="profile.nm" value="%{nm}" label="Profile Name" size="15"/>
                        <s:textarea name="profile.desc" value="%{desc}" label="Profile Description" rows="5"
                                    cols="25"/>
                        <s:hidden name="profile.id" value="%{id}"/>
                        <s:hidden name="sortedSet.orderByDirection"/>
                        <s:hidden name="sortedSet.orderByField"/>
                        <s:hidden name="selectForm"/>
                    </s:form>
                    <div class="submit_btn">Submit</div>
                    <div class="cancel_btn">Cancel</div>
                </div>
            </s:iterator>
        </s:else>
    </div>


</div>

</body>
</html>
