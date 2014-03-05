<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<c:set var="colspan" value="${empty param.colspan ? 1 : param.colspan}" />
<c:set var="multiline" value="${empty configuration.fields[param.field].multiline ? false : configuration.fields[param.field].multiline}" />
<td colspan="${colspan}">
    <div class="singleCellTitle"><spring:message code="${fn:toUpperCase(page)}.field.${param.field}"/></div>
<c:choose><c:when test="${multiline == false}">
    <form:input path="values['${param.field}']" readonly="${param.readOnly}"/> <%-- TODO: Implement translations --%>
</c:when><c:when test="${multiline == true}">
    <form:textarea path="values['${param.field}']" readonly="${param.readOnly}"/> <%-- TODO: Implement translations --%>
</c:when></c:choose>
</td>