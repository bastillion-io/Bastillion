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
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<meta http-equiv="Pragma" content="no-cache"/>
<meta http-equiv="Expires" content="-1"/>
<script src="<%= request.getContextPath() %>/_res/js/jquery-1.8.3.js"></script>
<script src="<%= request.getContextPath() %>/_res/js/jquery-ui.js"></script>
<script src="<%= request.getContextPath() %>/_res/js/jquery.tablescroll.js"></script>
<script src="<%= request.getContextPath() %>/_res/js/tty/terms.js"></script>


<link rel="stylesheet" href="<%= request.getContextPath() %>/_res/css/jquery-ui/base/jquery-ui.css"/>
<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/_res/css/keybox.css"/>
<link rel="icon" href="<%= request.getContextPath() %>/img/keybox.ico" type="image/x-icon"/>

<script type="text/javascript">
  $(document).ready(function() {
    $(function() {
        $("a").tooltip();
    });
   $(function() {
     var tabindex = 1;
     $('input,textarea,select,.ui-button').each(function() {
        if (this.type != "hidden") {
          var $input = $(this);
          $input.attr("tabindex", tabindex);
          tabindex++;
        }
     });

     $(".ui-button").keyup(function(event){
         if(event.keyCode == 13){
             $(this).click();
         }
     });
   });

});
</script>



