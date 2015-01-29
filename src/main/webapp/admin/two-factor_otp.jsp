<%
    /**
     * Copyright 2014 Sean Kavanagh - sean.p.kavanagh6@gmail.com
     *
     * Licensed under the Apache License, Version 2.0 (the "License"); you may
     * not use this file except in compliance with the License. You may obtain a
     * copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
     * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
     * License for the specific language governing permissions and limitations
     * under the License.
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
                        <div class="nav-img"><img src="<%= request.getContextPath()%>/img/keybox_50x38.png" alt="keybox"/></div>
                        KeyBox</div>
                </div>
                <!--/.nav-collapse -->
            </div>
        </div>

        <div class="container">

            <h3>Setup Two-Factor Authentication</h3>

            <div class="row featurette">
                <div class="col-md-7">

                    <img src="qrImage.action?qrImage=<s:property value="qrImage"/>" alt="<s:property value="qrImage"/>"/>

                </div>
                <div class="col-md-5">
                    <p>
                        Scan the QR Code using <a href="https://fedorahosted.org/freeotp" target="_blank">FreeOTP</a> 
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

                    <div class="spacer spacer-left">
                        <button class="btn btn-danger" data-toggle="modal" data-target="#confirmOtpDisable">Disable</button>
                    </div>
                    <div class="spacer spacer-middle">
                        <button onclick="window.location = 'menu.action'" class="btn btn-warning">Skip for Now</button>
                    </div>
                    <div class="spacer spacer-right">
                        <button class="btn btn-default" data-toggle="modal" data-target="#confirmOtpSetup">Got It!</button>
                    </div>
                    
                    <!-- Confirm that you scanned the QR code -->
                    <div id="confirmOtpSetup" class="modal fade">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                                    <h4 class="modal-title">Confirm</h4>
                                </div>
                                <div class="modal-body">
                                    <div class="row">
                                        Did you scan the QR Code?<br>
                                        <br>
                                        You will be logged off!
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                                    <button onclick="window.location='otpSubmit.action'" type="button" class="btn btn-default">Submit</button>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Confirm that you don't want to see this page again -->
                    <div id="confirmOtpDisable" class="modal fade">
                        <div class="modal-dialog">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
                                    <h4 class="modal-title">Confirm</h4>
                                </div>
                                <div class="modal-body">
                                    <div class="row">
                                        Are you sure that you want to disable OTP Authentication?<br>
                                        <br>
                                        You can enable it in the settings!
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-default cancel_btn" data-dismiss="modal">Cancel</button>
                                    <button onclick="window.location='otpDisable.action?otp'" type="button" class="btn btn-default">Submit</button>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>

    </body>
</html>
