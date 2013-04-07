/*
 * $Id: validation.js 692578 2008-09-05 23:30:16Z davenewton $
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function clearErrorMessages(form) {
    clearErrorMessagesXHTML(form);
}

function clearErrorMessagesXHTML(form) {

    // get field table
    var table;
    for (var i = 0; i < form.childNodes.length; i++) {
        if (form.childNodes[i].tagName != null && form.childNodes[i].tagName.toLowerCase() == 'table') {
            table = form.childNodes[i];
            break;
        }
    }

    if (table == null) {
        return;
    }

    // clear out any rows with an "errorFor" attribute
    var rows = table.rows;
    if (rows == null){
        return;
    }

    var rowsToDelete = new Array();
    for(var i = 0; i < rows.length; i++) {
        var r = rows[i];
        // allow blank errorFor values on dojo markup
        if (r.getAttribute("errorFor") != null) {
            rowsToDelete.push(r);
        }
    }

    // now delete the rows
    for (var i = 0; i < rowsToDelete.length; i++) {
        var r = rowsToDelete[i];
        table.deleteRow(r.rowIndex);
        //table.removeChild(rowsToDelete[i]); 
    }
}

function clearErrorLabels(form) {
    clearErrorLabelsXHTML(form);
}

function clearErrorLabelsXHTML(form) {
    // set all labels back to the normal class
    var elements = form.elements;
    for (var i = 0; i < elements.length; i++) {

        var parentEl = elements[i];
        // search for the parent table row, abort if the form is reached
        // the form may contain "non-wrapped" inputs inserted by Dojo
        while (parentEl.nodeName.toUpperCase() != "TR" && parentEl.nodeName.toUpperCase() != "FORM") {
            parentEl = parentEl.parentNode;
        }
        if (parentEl.nodeName.toUpperCase() == "FORM") {
            parentEl = null;
        }

         //if labelposition is 'top' the label is on the row above
        if(parentEl && parentEl.cells) {
          var labelRow = parentEl.cells.length > 1 ? parentEl : StrutsUtils.previousElement(parentEl, "tr");
          if (labelRow) {
              var cells = labelRow.cells;
              if (cells && cells.length >= 1) {
                  var label = cells[0].getElementsByTagName("label")[0];
                  if (label) {
                      label.setAttribute("class", "label");
                      label.setAttribute("className", "label"); //ie hack cause ie does not support setAttribute
                  }
              }
          }
        }
    }

}

function addError(e, errorText) {
    addErrorXHTML(e, errorText);
}

function addErrorXHTML(e, errorText) {
    try {
        var row = (e.type ? e : e[0]);
        while(row.nodeName.toUpperCase() != "TR") {
            row = row.parentNode;
        }
        var table = row.parentNode;
        var error = document.createTextNode(errorText);
        var tr = document.createElement("tr");
        var td = document.createElement("td");
        var span = document.createElement("span");
        td.align = "center";
        td.valign = "top";
        td.colSpan = 2;
        span.setAttribute("class", "errorMessage");
        span.setAttribute("className", "errorMessage"); //ie hack cause ie does not support setAttribute
        span.appendChild(error);
        td.appendChild(span);
        tr.appendChild(td);
        tr.setAttribute("errorFor", e.id);
        table.insertBefore(tr, row);

        // update the label too
        //if labelposition is 'top' the label is on the row above
        var labelRow = row.cells.length > 1 ? row : StrutsUtils.previousElement(tr, "tr");
        var label = labelRow.cells[0].getElementsByTagName("label")[0];
        if (label) {
            label.setAttribute("class", "errorLabel");
            label.setAttribute("className", "errorLabel"); //ie hack cause ie does not support setAttribute
        }
    } catch (e) {
        alert(e);
    }
}
