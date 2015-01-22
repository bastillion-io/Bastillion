<%-- 
    Document   : notification.jsp
    Created on : 22.01.2015, 14:49:11
    Author     : ptusch
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
        <c:if test="${not empty notificationClass}">
            <div class="alertContainer">               
                <%-- Alert if the password was changed successfully --%>
                <div class="<s:property value="%{notificationClass}"/>">
                    <a href="#" class="close" data-dismiss="alert">&times;</a>
                    <span class="alertMessage"><s:property value="%{notificationText}"/></span>
                </div>
            </div>
            
            <%-- Remove the session variable --%>
            <c:remove var="notificationClass" scope="session"/>
            <c:remove var="notificationText" scope="session"/>
        </c:if>
    </body>
</html>
