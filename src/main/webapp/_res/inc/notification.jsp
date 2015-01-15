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
     * 
     * Document   : notification
     * Created on : 15.01.2015, 10:21:46
     * Author     : ptusch
     * */
%>

<script type="text/javascript">
        $(document).ready(function() {
            var param = getUrlParameter("showPasswordNotification", '?');
            
            if (!param) {
                $(".alert").hide();
            } 
            else {
                var text = getParameter(param, '&');

                if (param.indexOf('&') > 0)
                    param = param.substring(0, param.indexOf('&'));
                
                $(".alert").addClass("alert-" + param);
                
                //Basic handlers for the text
                if (param === "danger") {
                   $(".alertMessage").html(text);
                }
                
                else if (param === "warning") {
                   $(".alertMessage").html(text);
                }
                
                else if (param === "success") {
                    $(".alertMessage").html(text);
                }
            }
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
        
        function getUrlParameter(sParam, char)
        {
            var sPageURL = window.location.search.substring(1);
            sPageURL = decodeURIComponent(sPageURL);
            var sURLVariables = sPageURL.split(char);
            for (var i = 0; i < sURLVariables.length; i++) 
            {
                var sParameterName = sURLVariables[i].split('=');
                if (sParameterName[0] == sParam) 
                {
                    if (sParameterName[1]) {
                        return sParameterName[1];
                    }
                    
                    else  {
                        return sParameterName[0];
                    }
                }
            }
        } 
        
        //Get the right parameter from the separator sign
        function getParameter(sParam, char) {
            if (sParam.indexOf(char) <= -1) {
                //Return ALT + 0129
                return "";
            }
            
            var variables = sParam.split(char);
            
            return variables[1];
        }

</script>

<div class="alertContainer">
    <div class="alert">
        <a href="#" class="close" data-dismiss="alert">&times;</a>
        <span class="alertMessage"></span>
    </div>
</div>