<#--
/*
 * $Id: form-close-validate.ftl 759162 2009-03-27 14:49:39Z musachy $
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
-->
<#--
START SNIPPET: supported-validators
Only the following validators are supported:
* required validator
* requiredstring validator
* stringlength validator
* regex validator
* email validator
* url validator
* int validator
* double validator
END SNIPPET: supported-validators
-->
<#if ((parameters.validate?default(false) == true) && (parameters.performValidation?default(false) == true))>
<script type="text/javascript">
    function validateForm_${parameters.id?replace('[^a-zA-Z0-9_]', '_', 'r')}() {
        form = document.getElementById("${parameters.id}");
        clearErrorMessages(form);
        clearErrorLabels(form);

        var errors = false;
        var continueValidation = true;
    <#list parameters.tagNames as tagName>
        <#list tag.getValidators("${tagName}") as validator>
        // field name: ${validator.fieldName}
        // validator name: ${validator.validatorType}
        if (form.elements['${validator.fieldName}']) {
            field = form.elements['${validator.fieldName}'];
            var error = "${validator.getMessage(action)?js_string}";
            <#if validator.validatorType = "required">
            if (field.value == "") {
                addError(field, error);
                errors = true;
                <#if validator.shortCircuit>continueValidation = false;</#if>
            }
            <#elseif validator.validatorType = "requiredstring">
            if (continueValidation && field.value != null && (field.value == "" || field.value.replace(/^\s+|\s+$/g,"").length == 0)) {
                addError(field, error);
                errors = true;
                <#if validator.shortCircuit>continueValidation = false;</#if>
            }
            <#elseif validator.validatorType = "stringlength">
            if (continueValidation && field.value != null) {
                var value = field.value;
                <#if validator.trim>
                    //trim field value
                    while (value.substring(0,1) == ' ')
                        value = value.substring(1, value.length);
                    while (value.substring(value.length-1, value.length) == ' ')
                        value = value.substring(0, value.length-1);
                </#if>
                if ((${validator.minLength?c} > -1 && value.length < ${validator.minLength?c}) ||
                    (${validator.maxLength?c} > -1 && value.length > ${validator.maxLength?c})) {
                    addError(field, error);
                    errors = true;
                    <#if validator.shortCircuit>continueValidation = false;</#if>
                }
            }
            <#elseif validator.validatorType = "regex">
            if (continueValidation && field.value != null && !field.value.match("${validator.expression?js_string}")) {
                addError(field, error);
                errors = true;
                <#if validator.shortCircuit>continueValidation = false;</#if>
            }
            <#elseif validator.validatorType = "email">
            if (continueValidation && field.value != null && field.value.length > 0 && field.value.match(/\b(^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@([A-Za-z0-9-])+(\.[A-Za-z0-9-]+)*((\.[A-Za-z0-9]{2,})|(\.[A-Za-z0-9]{2,}\.[A-Za-z0-9]{2,}))$)\b/gi)==null) {
                addError(field, error);
                errors = true;
                <#if validator.shortCircuit>continueValidation = false;</#if>
            }
            <#elseif validator.validatorType = "url">
            if (continueValidation && field.value != null && field.value.length > 0 && field.value.match(/(^(ftp|http|https):\/\/(\.[_A-Za-z0-9-]+)*(@?([A-Za-z0-9-])+)?(\.[A-Za-z0-9-]+)*((\.[A-Za-z0-9]{2,})|(\.[A-Za-z0-9]{2,}\.[A-Za-z0-9]{2,}))(:[0-9]+)?([/A-Za-z0-9?#_-]*)?$)/gi)==null) {
                addError(field, error);
                errors = true;
                <#if validator.shortCircuit>continueValidation = false;</#if>
            }
            <#elseif validator.validatorType = "int">
            if (continueValidation && field.value != null) {
                if (<#if validator.min??>parseInt(field.value) <
                     ${validator.min?c}<#else>false</#if> ||
                        <#if validator.max??>parseInt(field.value) >
                           ${validator.max?c}<#else>false</#if>) {
                    addError(field, error);
                    errors = true;
                    <#if validator.shortCircuit>continueValidation = false;</#if>
                }
            }
            <#elseif validator.validatorType = "double">
            if (continueValidation && field.value != null) {
                var value = parseFloat(field.value);
                if (<#if validator.minInclusive??>value < ${validator.minInclusive}<#else>false</#if> ||
                        <#if validator.maxInclusive??>value > ${validator.maxInclusive}<#else>false</#if> ||
                        <#if validator.minExclusive??>value <= ${validator.minExclusive}<#else>false</#if> ||
                        <#if validator.maxExclusive??>value >= ${validator.maxExclusive}<#else>false</#if>) {
                    addError(field, error);
                    errors = true;
                    <#if validator.shortCircuit>continueValidation = false;</#if>
                }
            }
            </#if>
        }
        </#list>
    </#list>

        return !errors;
    }
</script>
</#if>
