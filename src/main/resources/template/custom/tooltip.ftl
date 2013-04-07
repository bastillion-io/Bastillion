<#--
/*
 * $Id: tooltip.ftl 738624 2009-01-28 21:12:12Z musachy $
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
<#if parameters.tooltip??><#t/>
      <img
      <#if parameters.tooltipIconPath??><#t/>
      	src='<@s.url value="${parameters.tooltipIconPath}" includeParams="none" encode="false" />'
      <#else><#t/>
      	src='<@s.url value="/struts/tooltip.gif" includeParams="none" encode="false" />'
      </#if><#t/>
      <#if parameters.jsTooltipEnabled?default('false') == 'true'>
          onmouseover="domTT_activate(this, event, 'content', '${parameters.tooltip}'<#t/>
          <#if parameters.tooltipDelay??><#t/>
          	<#t/>,'delay', '${parameters.tooltipDelay}'<#t/>
          </#if><#t/>
          <#t/>,'styleClass', '${parameters.tooltipCssClass?default("StrutsTTClassic")}'<#t/>
          <#t/>)" />
      <#else>
      	title="${parameters.tooltip?html}"
      	alt="${parameters.tooltip?html}" />
     </#if>
</#if><#t/>
