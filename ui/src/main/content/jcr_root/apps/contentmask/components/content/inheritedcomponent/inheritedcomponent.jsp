<%@include file="/libs/foundation/global.jsp"%>
<%@taglib prefix="sl" uri="http://sling.apache.org/taglibs/sling" %>
<%@ taglib prefix="bedrock" uri="http://www.citytechinc.com/taglibs/bedrock"%>
<c:set var="inherited" value="${sl:adaptTo(resource,'com.icfi.aem.contentmask.components.content.InheritedComponent')}" />
<%@page session="false" import="com.day.cq.wcm.api.WCMMode"%>
<%
    WCMMode mode = WCMMode.fromRequest(slingRequest);
    boolean isEdit = mode == WCMMode.EDIT;
    pageContext.setAttribute("isEdit", isEdit);
%>
<c:if test="${isEdit}"><div class="js-inherited-component" data-inherited-component="${inherited.path}"></c:if>

<cq:include path="${inherited.path}" resourceType="${inherited.resourceType}"/>

<c:if test="${isEdit}"></div></c:if>