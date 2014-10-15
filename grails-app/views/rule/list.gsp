<%@page import="logs.ExecutionLog"%><html>
  <head>
    <title>An Example Page</title>
    <meta name="layout" content="main" />
    <style>
      h1 {
        background-color: #eeeeee;
        padding: 5px;
        margin: 0;
      }
      h2 {
        background-color: #f6f6f6;
        padding: 5px;
        margin: 0;
      }
      .actions {
        padding: 5px;
      }
    </style>
  </head>
  <body>
    <g:if test="${flash.message}">
      <div class="message" role="status">${flash.message}</div>
    </g:if>
    
    <div class="actions">
      <g:link action="loadRules">Cargar reglas</g:link>
    </div>
  
    <g:each in="${units.keySet()}" var="unitId">
    
      <h1>Unit: ${unitId} (reglas: ${units[unitId].rules.size()})</h1>

      <g:each in="${units[unitId].rules.keySet()}" var="ruleId">
      
        <%--
        <h2>Rule: ${ruleId}</h2>
        --%>
        
        <div class="actions">
          Rule: ${ruleId} |
          <g:link action="add" params="[unitId:unitId, ruleId:ruleId]">New execution</g:link>
        </div>
        
        <g:set var="xrules" value="${executer.getRules(ruleId)}" />
          
        <table>
          <tr>
            <th>Unit: ${unitId} (reglas: ${units[unitId].rules.size()})</th>
            <th>Initialized</th><!-- usar executer.getRules para saber cuales se estan ejecutando -->
            <th>Started</th>
            <th>Finished</th>
            <th>Archived</th>
            <th>Expired</th>
            <th>Errored</th>
          </tr>
          
          <!-- para cada regla en ejecucion con el mismo ruleId -->
          <g:each in="${xrules}" var="xrule">
            <tr>
              <td>
                ${xrule.context.executionSessionId}<br/>
                
                <%-- Inicializar regla agregada al contexto de ejecucion --%>
                <g:showIfAdded rule="${xrule}">
                  <g:form action="init" params="[sessId:xrule.context.executionSessionId]">
	                 <g:each in="${xrule.resolutors}" var="resolt">
	                   <%--${resolt.value.locator}<br/>
	                   Solo los resolutors http tienen paras por ahora
	                   --%>
	                   <g:if test="${resolt.value.type == 'http'}">
	                     <g:each in="${resolt.value.params}" var="param">
	                       ${param.name} (${param.type}) <g:field type="text" name="${param.name}" /><br/>
	                     </g:each>
	                   </g:if>
	                 </g:each>
                    <g:submitButton name="init" value="Initialize" />
                  </g:form>
                </g:showIfAdded>
                
                <%-- ejecuta solo si esta inicializada --%>
                <g:showIfInitialized rule="${xrule}">
                  <g:form action="exec" params="[sessId:xrule.context.executionSessionId]">
                    <g:each in="${xrule.input}" var="einput">
                    <%-- http://grails.org/doc/2.1.0/ref/Tags/field.html --%>
                      ${einput.key} (${einput.value.type}) <g:field type="text" name="${einput.key}" /><br/>
                    </g:each>
                    <g:submitButton name="execute" value="Execute" />
                  </g:form>
                </g:showIfInitialized>
                
                <g:showIfFinished rule="${xrule}">
                  <g:link action="result" params="[sessId:xrule.context.executionSessionId]">get result</g:link><br/>
                </g:showIfFinished>
                
                <g:showIfErrored rule="${xrule}">
                  <g:link action="ack" params="[sessId:xrule.context.executionSessionId]">ack</g:link><br/>
                </g:showIfErrored>
                <g:showIfExpired rule="${xrule}">
                  <g:link action="ack" params="[sessId:xrule.context.executionSessionId]">ack</g:link><br/>
                </g:showIfExpired>
                
                <g:link action="showRule" params="[sessId:xrule.context.executionSessionId]" target="_blank">show</g:link><br/>
                
                <%
                // Test para saber el estado actual de ejecucion de la regla.
                
                /* Me devuelve todos los estados y fechas, pero solo los datos no los objetos.
                def c = ExecutionLog.createCriteria() 
                def result = c { 
                  eq("sessionId", xrule.context.executionSessionId) 
                
                  projections { 
                    max("time")
                    groupProperty("state")
                  } 
                
                  //maxResults(5) 
                  //order("theOrder", "asc") 
                  //fetchMode("story", org.hibernate.FetchMode.EAGER) 
                }
                //println result[0] as List
                
                println result as grails.converters.XML
                
                <br/>
                Current state: ${result[0][1]}
                */


                // Log con maxima fecha
                // Devuelve 1 objeto log
                def query = ExecutionLog.where {
                  sessionId == xrule.context.executionSessionId
                }
                
                def log = query.list(sort:"time", order:"desc", max:1)[0]
                //println log.state
                
                // usando get para obtener un resultado da un error:
                // Class org.hibernate.NonUniqueResultException
                // Message query did not return a unique result: 3
                //println query.get() as grails.converters.XML

                %>
                <g:if test="${log}">
                  <g:img dir="images/rule_icons" file="${log.state}.png" width="32" height="32" title="${log.state}"/>
                </g:if>
              </td>
              <td><!-- initialized -->
              
                <g:set var="xlog" value="${ExecutionLog.findBySessionIdAndState(xrule.context.executionSessionId, ExecutionLog.STATE_INITIALIZED)}" />
          
                <g:if test="${xlog}">
	              ${xlog?.time}<br/>
	              
	              Rule variables:
	              <table>
	                <tr>
	                  <th>name</th>
	                  <th>type</th>
	                  <th>value</th>
	                </tr>
	                <%-- Para las reglas inicializadas, las variables están en definitions,
	                     todavía no se copiaron al contexto, se copian en el execute.
	                  <g:each in="${xrule.context.values.keySet()}" var="valueName">
	                    <tr>
	                      <td>${valueName}</td>
	                      <td>${xrule.context.values[valueName].type}</td>
	                      <td>${xrule.context.values[valueName].value}</td>
	                    </tr>
	                  </g:each>
	                --%>
	                <g:each in="${xrule.definitions}" var="entry">
	                  <tr>
	                    <td>${entry.key}</td>
	                    <td>${entry.value.type}</td>
	                    <td>${entry.value.value}</td>
	                  </tr>
	                </g:each>
	              </table>
	                
		           Init params:
		           <table>
		             <tr>
                     <th>name</th>
                     <th>type</th>
                     <th>value</th>
                   </tr>
	                <g:each in="${xrule.resolutors}" var="resolt">
	                  <%--${resolt.value.locator}<br/>--%>
	                  <tr>
	                    <g:if test="${resolt.value.type == 'http'}">
	                      <g:each in="${resolt.value.params}" var="param">
	                        <td>${param.name}</td>
	                        <td>${param.type}</td>
	                        <td>${param.value}</td>
	                      </g:each>
	                    </g:if>
	                  </tr>
	                </g:each>
	              </table>
	            </g:if>
              </td>
              <td><!-- active -->
                <g:set var="xlog" value="${ExecutionLog.findBySessionIdAndState(xrule.context.executionSessionId, ExecutionLog.STATE_ACTIVE)}" />
                <g:if test="${xlog}">
                  ${xlog?.time}<br/>
                  Input params:
                  <table>
                    <tr>
                      <th>name</th>
                      <th>type</th>
                      <th>value</th>
                    </tr>
                    <g:each in="${xrule.input}" var="input">
                      <tr>
                        <td>${input.value.name}</td>
                        <td>${input.value.type}</td>
                        <td>${input.value.value}</td>
                      </tr>
                    </g:each>
                  </table>
                </g:if>
              </td>
              <td><!-- finished -->
              
                <g:showIfFinished rule="${xrule}">
                
                  <g:set var="xlog" value="${ExecutionLog.findBySessionIdAndState(xrule.context.executionSessionId, ExecutionLog.STATE_FINISHED)}" />
              
                  ${xlog?.time}<br/>
                  
                  <g:if test="${xrule.result}">
                    Returned values
                    <table>
                      <tr>
                        <th>name</th>
                        <th>type</th>
                        <th>value</th>
                      </tr>
                      <g:each in="${xrule.result.keySet()}" var="valueName">
                        <tr>
                          <td>${valueName}</td>
                          <td>${xrule.context.values[valueName].type}</td>
                          <td>${xrule.context.values[valueName].value}</td>
                        </tr>
                      </g:each>
                    </table>
                  </g:if>
                  <g:else>
                    Execution doesn't returned any values
                  </g:else>
                </g:showIfFinished>
              </td>
              <td><!-- Archived -->
                <g:set var="xlog" value="${ExecutionLog.findBySessionIdAndState(xrule.context.executionSessionId, ExecutionLog.STATE_ARCHIVED)}" />
          
                ${xlog?.time}<br/>
              </td>
              <td><!-- Expired -->
                <g:set var="xlog" value="${ExecutionLog.findBySessionIdAndState(xrule.context.executionSessionId, ExecutionLog.STATE_EXPIRED)}" />
          
                ${xlog?.time}<br/>
              </td>
              <td><!-- Errored -->
                <g:set var="xlog" value="${ExecutionLog.findBySessionIdAndState(xrule.context.executionSessionId, ExecutionLog.STATE_ERRORED)}" />
          
                ${xlog?.time}<br/>
                
                ${xlog?.msgs}
              </td>
            </tr>
          </g:each>
        </table>
      </g:each>
    </g:each>
  </body>
</html>