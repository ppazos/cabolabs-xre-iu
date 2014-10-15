
<%@ page import="logs.ExecutionLog" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'executionLog.label', default: 'ExecutionLog')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-executionLog" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-executionLog" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list executionLog">
			
				<g:if test="${executionLogInstance?.state}">
				<li class="fieldcontain">
					<span id="state-label" class="property-label"><g:message code="executionLog.state.label" default="State" /></span>
					
						<span class="property-value" aria-labelledby="state-label"><g:fieldValue bean="${executionLogInstance}" field="state"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${executionLogInstance?.ruleId}">
				<li class="fieldcontain">
					<span id="ruleId-label" class="property-label"><g:message code="executionLog.ruleId.label" default="Rule Id" /></span>
					
						<span class="property-value" aria-labelledby="ruleId-label"><g:fieldValue bean="${executionLogInstance}" field="ruleId"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${executionLogInstance?.sessionId}">
				<li class="fieldcontain">
					<span id="sessionId-label" class="property-label"><g:message code="executionLog.sessionId.label" default="Session Id" /></span>
					
						<span class="property-value" aria-labelledby="sessionId-label"><g:fieldValue bean="${executionLogInstance}" field="sessionId"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${executionLogInstance?.time}">
				<li class="fieldcontain">
					<span id="time-label" class="property-label"><g:message code="executionLog.time.label" default="Time" /></span>
					
						<span class="property-value" aria-labelledby="time-label"><g:formatDate date="${executionLogInstance?.time}" /></span>
					
				</li>
				</g:if>			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${executionLogInstance?.id}" />
					<g:link class="edit" action="edit" id="${executionLogInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
