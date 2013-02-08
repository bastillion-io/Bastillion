<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>

    <jsp:include page="../_res/inc/header.jsp"/>

    <script type="text/javascript">
        $(document).ready(function() {



            //open add dialog
            $("#assign_sys").button().click(function() {
                $('#assignSystemsToProfile').submit();
            });

            //select all check boxs
            $("#assignSystemsToProfile_systemSelectAll").click(function() {

                if ($(this).is(':checked')) {
                    $(".systemSelect").attr('checked', true);
                } else {
                    $(".systemSelect").attr('checked', false);
                }
            });

               $(".sort,.sortAsc,.sortDesc").click(function() {
                var id = $(this).attr('id')

                if ($('#viewProfileSystems_sortedSet_orderByDirection').attr('value') == 'asc') {
                    $('#viewProfileSystems_sortedSet_orderByDirection').attr('value', 'desc');

                } else {
                    $('#viewProfileSystems_sortedSet_orderByDirection').attr('value', 'asc');
                }

                $('#viewProfileSystems_sortedSet_orderByField').attr('value', id);
                $("#viewProfileSystems").submit();

            });
            <s:if test="sortedSet.orderByField!= null">
            $('#<s:property value="sortedSet.orderByField"/>').attr('class', '<s:property value="sortedSet.orderByDirection"/>');
            </s:if>
            $('.scrollableTable').tableScroll({height:400});
            $(".scrollableTable tr:odd").css("background-color", "#e0e0e0");


            <s:if test="profile.hostSystemList!= null && !profile.hostSystemList.isEmpty()">
            <s:iterator var="system" value="profile.hostSystemList" status="stat">
            $('#systemSelectId_<s:property value="id"/>').attr('checked', true);
            </s:iterator>
            </s:if>


        });
    </script>

    <title>KeyBox - Assign Systems to Profile</title>

</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

       <s:form action="viewProfileSystems">
            <s:hidden name="sortedSet.orderByDirection"/>
            <s:hidden name="sortedSet.orderByField"/>
           <s:hidden name="profile.id"/>
            <s:hidden name="genAuthKeys"/>
        </s:form>

        <h3>Assign Systems to Profile</h3>

        <p>Select the systems below to be assigned to the current profile.</p>

        <h4><s:property value="profile.nm"/></h4>
        <p><s:property value="profile.desc"/></p>


        <s:if test="sortedSet.itemList!= null && !sortedSet.itemList.isEmpty()">
            <s:form action="assignSystemsToProfile" theme="simple">
                <s:hidden name="profile.id"/>

                <table class="vborder scrollableTable">
                    <thead>


                    <tr>
                        <th><s:checkbox name="systemSelectAll" cssClass="systemSelect" fieldValue="true" theme="simple"/></th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_NAME"/>" class="sort">Display Name</th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_USER"/>" class="sort">User</th>
                        <th id="<s:property value="@com.keybox.manage.db.SystemDB@SORT_BY_HOST"/>" class="sort">Host</th>
                    </tr>
                    </thead>

                    <tbody>


                    <s:iterator var="system" value="sortedSet.itemList" status="stat">
                        <tr>
                            <td>
                                <s:checkbox id="systemSelectId_%{id}" name="systemSelectId" cssClass="systemSelect"
                                            fieldValue="%{id}"
                                            value="checked" theme="simple"/>
                            </td>
                            <td>
                                <s:property value="displayNm"/>

                            </td>
                            <td><s:property value="user"/></td>
                            <td><s:property value="host"/>:<s:property value="port"/></td>
                        </tr>

                    </s:iterator>
                    </tbody>
                </table>
            </s:form>
        </s:if>


        <div id="assign_sys" class="assign_sys_btn">Assign</div>


    </div>
</div>
</body>
</html>
