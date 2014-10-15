<%@ page import="logs.ExecutionLog" %>



<div class="fieldcontain ${hasErrors(bean: executionLogInstance, field: 'state', 'error')} ">
	<label for="state">
		<g:message code="executionLog.state.label" default="State" />
		
	</label>
	<g:textField name="state" value="${executionLogInstance?.state}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: executionLogInstance, field: 'ruleId', 'error')} ">
	<label for="ruleId">
		<g:message code="executionLog.ruleId.label" default="Rule Id" />
		
	</label>
	<g:textField name="ruleId" value="${executionLogInstance?.ruleId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: executionLogInstance, field: 'sessionId', 'error')} ">
	<label for="sessionId">
		<g:message code="executionLog.sessionId.label" default="Session Id" />
		
	</label>
	<g:textField name="sessionId" value="${executionLogInstance?.sessionId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: executionLogInstance, field: 'time', 'error')} required">
	<label for="time">
		<g:message code="executionLog.time.label" default="Time" />
		<span class="required-indicator">*</span>
	</label>
	<g:datePicker name="time" precision="day"  value="${executionLogInstance?.time}"  />
</div>