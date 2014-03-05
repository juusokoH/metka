<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<div class="singleCellTitle"><spring:message code="${fn:toUpperCase(page)}.field.${param.field}"/></div>
<form:hidden path="values['${param.field}']" />
<table id="${param.field}" class="dataTable autobuild">
    <thead>
        <tr>
        <c:forEach var="subfield" items="${configuration.fields[param.field].subfields}">
            <c:if test="${configuration.fields[subfield].summaryField == true}"><th><spring:message code="${fn:toUpperCase(page)}.field.${subfield}"/></th></c:if>
        </c:forEach>
        <c:if test="${configuration.fields[param.field].showSaveInfo == true}">
            <th><spring:message code="general.saveInfo.savedAt"/></th>
            <th><spring:message code="general.saveInfo.savedBy"/></th>
        </c:if>
        </tr>
    </thead>
    <tbody />
</table>
<%-- TODO: add some generic way to add rows --%>
<c:if test="${empty param.generalHandler or param.generalHandler == true}">
    <div class="rightAlignCell"><input type="button" class="button" value="<spring:message code="general.table.add"/>"
            onclick="showGeneralDialog('${param.field}', true)"/></div>
    <jsp:include page="../dialogs/generalContainerDialog.jsp">
        <jsp:param name="field" value="${param.field}" />
    </jsp:include>
</c:if>

<%--
<td colspan="${colspan}">

    <c:choose><c:when test="${multiline == false}">
        <form:input path="values['${param.field}']" readonly="${param.readOnly}"/> &lt;%&ndash; TODO: Implement translations &ndash;%&gt;
    </c:when><c:when test="${multiline == true}">
        <form:textarea path="values['${param.field}']" readonly="${param.readOnly}"/> &lt;%&ndash; TODO: Implement translations &ndash;%&gt;
    </c:when></c:choose>
</td>--%>
