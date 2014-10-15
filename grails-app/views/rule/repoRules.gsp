<%@page import="logs.ExecutionLog"%><html>
  <head>
    <title>Repo Rules</title>
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
      <%-- <g:link action="loadRules">Cargar reglas</g:link> --%>
    </div>
  
    <table>
      <tr>
        <th>rule id</th>
        <th>rule name</th>
        <th>loaded</th>
        <th>actions</th>
      </tr>
      <g:each in="${ruleFiles}" var="ruleFile">
        <tr>
          <td>${ruleFile.value.header.id.text()}</td>
          <td>${ruleFile.key.name}</td>
          <td>${reg.containsUnit(ruleFile.value.header.id.text())}</td>
          <td></td>
        </tr>
      </g:each>
    </table>
  </body>
</html>