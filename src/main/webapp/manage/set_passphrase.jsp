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


            $("#change_pass_btn").button().click(function() {
                $('#passphraseSubmit').submit();
            });
        });

    </script>

    <title>KeyBox - Set Admin Passphrase</title>
</head>
<body>

<div class="page">
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="content">

        <h3>Set Passphrase for System Generated SSH Keys </h3>


          <p>For added security you may set a passphrase for the system generated SSH key. You will be asked to provide
            this passphrase each time you access a system or systems.</p>



        <s:actionerror/>
        <s:form action="passphraseSubmit">
            <s:if test="hasCustomPassphrase">
                <s:password name="prevPassphrase" label="Current Passphrase" autocomplete="off"/>
            </s:if>
            <s:password name="passphrase" label="New Passphrase" autocomplete="off"/>
            <s:password name="passphraseConfirm" label="Confirm New Passphrase" autocomplete="off"/>
            <tr>
                <td>&nbsp;</td>
                <td align="right">
                    <div id="change_pass_btn" class="login">Change Passphrase</div>
                </td>
            </tr>
        </s:form>

    </div>


</div>

</body>
</html>
