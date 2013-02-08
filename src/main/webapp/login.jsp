<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
<head>

    <jsp:include page="_res/inc/header.jsp"/>

    <script type="text/javascript">
        $(document).ready(function() {


            $("#login_btn").button().click(function() {
                $('#loginSubmit').submit();
            });
        });

    </script>
    <title>KeyBox - Login </title>
</head>
<body>

<div class="page">
    <div style="float: left;margin-top: 5px;margin-left: -10px"><img
            src="<%= request.getContextPath() %>/img/keybox_50x38.png"/></div>

    <h3>

        KeyBox - Login
    </h3>

    <div class="content" style="border-left:none;">

        <s:actionerror/>
        <s:form action="loginSubmit">
            <s:textfield name="login.username" label="Username"/>
            <s:password name="login.password" label="Password"/>
            <tr> <td>&nbsp;</td>
                <td align="right">  <div id="login_btn" class="login" >Login</div></td>
            </tr>
        </s:form>



    </div>
</div>

</body>
</html>
