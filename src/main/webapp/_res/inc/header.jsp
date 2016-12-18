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
<script src="<%= request.getContextPath() %>/_res/js/jquery-2.1.3.js"></script>
<script src="<%= request.getContextPath() %>/_res/js/jquery.floatThead.js"></script>
<script src="<%= request.getContextPath() %>/_res/js/tty/term.js"></script>
<script src="<%= request.getContextPath() %>/_res/js/bootstrap.js"></script>

<link rel="stylesheet" href="<%= request.getContextPath() %>/_res/css/bootstrap.css"/>
<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/_res/css/keybox.css"/>
<link rel="icon" href="<%= request.getContextPath() %>/img/keybox.ico" type="image/x-icon"/>

<script type="text/javascript">
    $(document).ready(function () {

        $.ajaxSetup({cache: false,async: false});

        $(function () {
            $("a").tooltip({
                'selector': '',
                'placement': 'bottom',
                'container': 'body'
            });
        });
        $(function () {
            var tabindex = 1;
            $('input,textarea,select,.btn').each(function () {
                if (this.type != "hidden") {
                    var $input = $(this);
                    $input.attr("tabindex", tabindex);
                    tabindex++;
                }
            });

            $(".btn").keyup(function (event) {
                if (event.keyCode == 13) {
                    $(this).click();
                }
            });

            $("form input, form select").keydown(function (event) {
                if (event.keyCode == 13) {
                    $(this).closest("form").submit();
                }
            });
        });

        if ($('.scrollWrapper').height() >= 450) {
            
            $('.scrollWrapper').addClass('scrollWrapperActive');
            $('.scrollableTable').floatThead({
                scrollContainer: function ($table) {
                    return $table.closest(".scrollWrapper");
                }
            });
        }

        $(".scrollableTable tr:even").css("background-color", "#e0e0e0");

        $(':input:enabled:visible:first').focus();

        $('.modal').on('shown.bs.modal', function () {
            $('input:enabled:visible:first').focus();
        });

        //disable double-click on btns
        $("form").submit(function() {
            $('.btn').attr('disabled', 'disabled');
            return true;
        });

    });
</script>



