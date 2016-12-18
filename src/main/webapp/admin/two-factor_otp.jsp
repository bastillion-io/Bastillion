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

        <jsp:include page="/_res/inc/header.jsp"/>

        <script type="text/javascript">
            //break if loaded in frame
            if (top != self)
                top.location.replace(location);

            $(document).ready(function() {
                $(".submit_btn").button().click(function() {
                    $(this).parents('.container').find('form').submit();
                });
            });

        </script>
        <title>KeyBox - One-Time Password Setup</title>
    </head>
    <body>

        <div class="navbar navbar-default navbar-fixed-top" role="navigation">
            <div class="container" >

                <div class="navbar-header">
                    <div class="navbar-brand" >
                        <div class="nav-img"><img src="<%= request.getContextPath()%>/img/keybox_40x40.png" alt="keybox"/></div>
                        KeyBox</div>
                </div>
                <!--/.nav-collapse -->
            </div>
        </div>

        <div class="container">

            <h3>Setup Two-Factor Authentication</h3>

            <div class="row featurette">
                <div class="col-md-7">

                    <div class="panel panel-default">
                        <div class="panel-body" >
                            <img src="qrImage.action?qrImage=<s:property value="qrImage"/>&_csrf=<s:property value="#session['_csrf']"/>" alt="<s:property value="qrImage"/>"/>
                        </div>
                        <div class="panel-footer">
                            <label>Can't scan QR code?</label>&nbsp;&nbsp;<a href="#" onclick="$('#shared-secret').toggleClass('hidden');">Show secret</a>
                            <span id="shared-secret" class="hidden"> - <s:property value="sharedSecret"/></span>
                        </div>
                    </div>

                </div>
                <div class="col-md-5">
                    <p>
                        Scan the QR code using <a href="https://fedorahosted.org/freeotp" target="_blank">FreeOTP</a>
                        or <a href="https://github.com/google/google-authenticator" target="_blank">Google Authenticator</a>
                        on your Android or iOS device to setup two-factor authentication.
                    </p>
                    <table class="table table-striped table-hover ">
                        <thead>
                            <tr>
                                <th>FreeOTP</th>
                                <th>Link</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Android</td>
                                <td><a href="https://play.google.com/store/apps/details?id=org.fedorahosted.freeotp" target="_blank">Google Play</a></td>
                            </tr>

                            <tr>
                                <td>iOS</td>
                                <td><a href="https://itunes.apple.com/us/app/freeotp/id872559395" target="_blank">iTunes</a></td>
                            </tr>
                            <tr>
                                <th>Google Authenticator</th>
                                <th>Link</th>
                            </tr>
                            <tr>
                                <td>Android</td>
                                <td><a href="https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2" target="_blank">Google Play</a></td>
                            </tr>

                            <tr>
                                <td>iOS</td>
                                <td><a href="https://itunes.apple.com/us/app/google-authenticator/id388497605" target="_blank">iTunes</a></td>
                            </tr>
                        </tbody>
                    </table>
                    <s:if test="%{!@com.keybox.manage.action.OTPAction@requireOTP}">
                        <button onclick="window.location = 'menu.action?_csrf=<s:property value="#session['_csrf']"/>'" class="btn btn-danger spacer spacer-left" style="float:left">Skip for Now</button>
                    </s:if>
                    <s:form action="otpSubmit" theme="simple" >
                        <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
                        <s:hidden name="sharedSecret"/>
                        <s:submit cssClass="btn btn-default spacer spacer-right" value="Got It!"/>
                    </s:form>
                </div>
            </div>
        </div>
    </body>
</html>
