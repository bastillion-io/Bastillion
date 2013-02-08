<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>

    <jsp:include page="../_res/inc/header.jsp"/>

       <script type="text/javascript">
        $(document).ready(function() {


            $("#change_pass_btn").button().click(function() {
                $('#passwordSubmit').submit();
            });
        });

    </script>

    <title>KeyBox - Set Admin Password</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <h3>Set Admin Password</h3>
        <p>Change your administrative password below</p>

        <s:actionerror/>
        <s:form action="passwordSubmit">
            <s:password name="login.prevPassword" label="Current Password"  autocomplete="off"/>
            <s:password name="login.password" label="New Password" autocomplete="off"/>
            <s:password name="login.passwordConfirm" label="Confirm New Password"  autocomplete="off"/>
            <tr> <td>&nbsp;</td>
                <td align="right">  <div id="change_pass_btn" class="login" >Change Password</div></td>
            </tr>
        </s:form>

    </div>


</div>

</body>
</html>
