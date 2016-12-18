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


            $("#upload_btn").button().click(function() {
                $('#upload').submit();
            });
        });

    </script>
    <style>
        body {
            padding: 10px;
        }
    </style>

    <title>KeyBox - Upload &amp; Push</title>
</head>
<body style="background: #FFFFFF">

<s:if test="idList!= null && !idList.isEmpty()">
<s:form action="upload" method="POST" enctype="multipart/form-data">
    <s:hidden name="_csrf" value="%{#session['_csrf']}"/>
    <s:file name="upload" label="File"/>
    <s:textfield name="pushDir" label="Destination Directory"/>

    <tr>
        <td>&nbsp;</td>
        <td>
            <div id="upload_btn" class="btn btn-default upload">Upload</div>
        </td>
    </tr>
</s:form>
</s:if>
<s:else>
    <p class="error">No systems associated with upload</p>
</s:else>

</body>
</html>