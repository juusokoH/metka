<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page session="false" %>
<c:set var="context" value="${fn:toUpperCase(page)}" />
<!DOCTYPE HTML>
<html lang="fi">
<head>
    <jsp:include page="../inc/head.jsp" />
</head>
<body>
<jsp:include page="../inc/topMenu.jsp" />
<div class="wrapper">
    <div class="content">
        <h1 class="pageTitle"><spring:message code="${context}"/> - <spring:message code="${context}.field.${configuration[context].displayId}"/>&nbsp;${single.id} - <spring:message code="general.revision"/>&nbsp;${single.revision}
            <div class="floatRight normalText"><input type="button" onclick="MetkaJS.PathBuilder().add('download').add(MetkaJS.SingleObject.id).add(MetkaJS.SingleObject.revision).navigate()" value="<spring:message code='general.buttons.download' />"/></div></h1>
        <jsp:include page="../inc/prevNext.jsp" />
        <jsp:include page="${page}/view.jsp" />
        <div class="buttonsHolder">
            <jsp:include page="../inc/revHistory.jsp" />
            <input type="button" class="button"
                   value="<spring:message code='general.buttons.edit'/>"
                   onclick="MetkaJS.SingleObject.edit()"/>
            <jsp:include page="../inc/removeButton.jsp" />
        </div>
    </div>
</div>
</body>
</html>