<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                $('#passwordSubmit').submit();
            });
            
            
        });
        
        function onKeyCaller(event) {
                if (!event) {
                    return;
                }

                //Enter
                if (event.keyCode == 13) {
                        $('#change_pass_btn').click();
                }
        }
    </script>

    <title>KeyBox - Set Admin Password</title>
</head>
<body>
    <jsp:include page="../_res/inc/navigation.jsp"/>

    <div class="container">
        
        <c:if test="${not empty showPasswordNotification}">
            <div class="alertContainer">
                
                <%-- Alert if the password was changed successfully --%>
                <s:if test="showPasswordNotification=='success'">
                    <div class="alert alert-success">
                        <a href="#" class="close" data-dismiss="alert">&times;</a>
                        <span class="alertMessage">Password successfully changed!</span>
                    </div>
                </s:if>
                
                <%-- Alert if the passwords didn't match--%>
                <s:elseif test="showPasswordNotification=='warning'">
                    <div class="alert alert-danger">
                        <a href="#" class="close" data-dismiss="alert">&times;</a>
                        <span class="alertMessage">The passwords do not match!</span>
                    </div>
                </s:elseif>
                
                <%-- Alert if the password was wrong --%>
                <s:else>
                    <div class="alert alert-danger">
                        <a href="#" class="close" data-dismiss="alert">&times;</a>
                        <span class="alertMessage">The current password was wrong!</span>
                    </div>
                </s:else>
            </div>
            
            <%-- Remove the session variable --%>
            <c:remove var="showPasswordNotification" scope="session"/>
        </c:if>

        <h3>Set Admin Password</h3>
        <p>Change your administrative password below</p>

        <s:actionerror/>
        <s:form action="passwordSubmit" autocomplete="off">
            <s:password name="auth.prevPassword" label="Current Password" placeholder="Mandatory field"/>
            <s:password name="auth.password" label="New Password" placeholder="Mandatory field"/>
            <s:password name="auth.passwordConfirm" label="Confirm New Password" placeholder="Mandatory field" onkeydown="onKeyCaller(event)"/>
            </tr>
        </s:form>
            <div id="change_pass_btn" class="btn btn-default">Change Password</div>
            
        <h3>Set OTP Authentication</h3>
        <p>Reset OTP page- settings per user</p>
        <s:if test="showOtpPage==true">
            <button onclick="window.location='otpDisable.action'" class="btn btn-danger">Disable OTP Authentication</button>
        </s:if>
        <s:else>
            <button onclick="window.location='otpEnable.action'" class="btn btn-default">Enable OTP Authentication</button>
        </s:else>
    </div>
</body>
</html>
