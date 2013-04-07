<#--
/*
 * $Id: form-validate.ftl 754994 2009-03-16 20:09:51Z musachy $
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
<#if parameters.validate?default(false) == true>
	<script type="text/javascript" src="${base}/struts/xhtml/validation.js"></script>
	<script type="text/javascript" src="${base}/struts/utils.js"></script>
	<#if parameters.onsubmit??>
		${tag.addParameter('onsubmit', "${parameters.onsubmit}; return validateForm_${parameters.id?replace('[^a-zA-Z0-9_]', '_', 'r')}();")}
	<#else>
		${tag.addParameter('onsubmit', "return validateForm_${parameters.id?replace('[^a-zA-Z0-9_]', '_', 'r')}();")}
	</#if>
</#if>
