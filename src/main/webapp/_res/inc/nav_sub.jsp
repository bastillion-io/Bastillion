 <%@ taglib prefix="s" uri="/struts-tags" %>
 <ul style="padding:0;list-style: none;">
    <li style="float:left;padding:0 15px 15px 0"><a href="viewSystems.action?selectForm=true<s:if test="script!=null">&script.id=<s:property value="script.id"/></s:if>">
    <s:if test="script!=null">Execute Script for Systems</s:if>
    <s:else>Distribute Keys by System</s:else></a></li>

    <s:if test="script==null">
    <li style="float:left;padding:0 15px 15px 0"><a href="viewUsers.action?selectForm=true">Distribute Keys by User</a></li>
    </s:if>

    <li style="float:left;padding:0 15px 15px 0"><a href="viewProfiles.action?selectForm=true<s:if test="script!=null">&script.id=<s:property value="script.id"/></s:if>">
    <s:if test="script!=null">Execute Script for Profile</s:if>
    <s:else>Distribute Keys by Profile</s:else></a></li>
</ul>
<div style="clear:both;"/>