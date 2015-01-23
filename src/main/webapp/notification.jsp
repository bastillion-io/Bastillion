<%-- 
    Document   : notification.jsp
    Created on : 22.01.2015, 14:49:11
    Author     : ptusch
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html>
    <head>
    </head>
    <body>
        <%-- Only render if there is any content to show --%>
        <c:if test="${not empty notificationClass}">
            <div class="alertContainer">               
                <%-- Alert if the password was changed successfully --%>
                <div class="<s:property value="%{notificationClass}"/>">
                    <a href="#" class="close" data-dismiss="alert">&times;</a>
                    <span class="alertMessage"><s:property value="%{notificationText}"/></span>
                </div>
            </div>
            
            <%-- Set the session- variables to something less dangerous... --%>
            <c:set var="notificationClass" scope="session" value=""/>
            <c:set var="notificationText" scope="session" value=""/>
        </c:if>
    </body>
</html>
