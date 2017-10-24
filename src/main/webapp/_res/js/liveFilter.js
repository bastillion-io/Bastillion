/**

 Copyright 2017 - Luca Palano <contact@lpzone.it>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

var tableRowsBackgroundColors = [];

$(function(){
    tableRowsBackgroundColors.push($(".scrollWrapper > table > tbody > tr:odd").css("background-color"));
    tableRowsBackgroundColors.push($(".scrollWrapper > table > tbody > tr:even").css("background-color"));
});

// Create a case insensitive contains function
$.expr[':'].icontains = function(a, i, m) {
    return $(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
};

function liveFilter(inputFormId, cellsIndexes) {
    $("#"+inputFormId).keyup(function () {
        var valueToFind = $(this).val();
        if(valueToFind == "") {
            $(".scrollWrapper > table > tbody > tr").css("display", "table-row");
            $("#"+inputFormId).parent().removeClass("has-success").removeClass("has-error");
        } else {
            $(".scrollWrapper > table > tbody > tr").css("display", "none");
            $("#"+inputFormId).parent().addClass("has-error");
            $.each(cellsIndexes, function(index, value) {
                var domFoundedItems = $(".scrollWrapper > table > tbody > tr > td:nth-child(" + value + "):icontains(" + valueToFind + ")");
                if(domFoundedItems.length > 0) {
                    domFoundedItems.parent().css("display", "table-row");
                    $("#"+inputFormId).parent().removeClass("has-error").addClass("has-success");
                }
            });
        }
        $(".scrollWrapper > table > tbody > tr:visible:odd").css("background-color",tableRowsBackgroundColors[0]);
        $(".scrollWrapper > table > tbody > tr:visible:even").css("background-color",tableRowsBackgroundColors[1]);
    });
}