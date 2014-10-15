package rules

import grails.converters.*

// rule-engine.jar
import logs.ExecutionLog
import parser.Parser
import rules.Registry
import com.thoughtworks.xstream.XStream
import execution.Executer
//import groovyx.net.http.* // HttpBuilder test
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.ContentType

class RestController {

   static defaultAction = "list"
   
   /**
    * List available units and rules
    */
   def list() {
      
      // TODO:
      // El registry deberia escanear el repo y cargar las reglas
      def reg = Registry.instance
      
      
      // El executer es para preguntar por datos de las reglas que se estan ejecutando ahora
      //return [units: reg.getUnits(), executer:Executer.instance]
      
      def result = [:]
      
      reg.getUnits().each { unitId, unit ->
         
         // reglas para la unit
         result[unitId] = [:]
         
         unit.rules.each { ruleId, rule ->
            
            result[unitId] << ['id':rule.id, 'name':rule.name, 'description':rule.description]
         }
      }
      
      /*
       * {
          "unit_rule_date_diff.v1": {
              "id": "rule_date_diff.v1",
              "name": "regla de prueba para calculos de diferencias entre fechas",
              "description": null
          },
          "rule4.v1": {
              "id": "rule_4_1.v1",
              "name": "Regla con return de valores",
              "description": null
          },
          "rule5.v1": {
              "id": "rule_5_1.v1",
              "name": "Regla con return de valores",
              "description": null
          },
          "mod.rule6.v1": {
              "id": "rule_6_1.v1",
              "name": "Regla con return de agregaciones",
              "description": null
          },
          "mod.rule7_error_url.v1": {
              "id": "rule7_error_url.v1",
              "name": "Regla con errores",
              "description": null
          }
         }
       */
      
      println result
      
      if (!params.format || params.format == "json")
         render result as JSON
      else if (params.format == "xml")
         render result as XML
      else
        throw new Exception("format not supported "+ params.format +" options: 'xml' or 'json'")
   }
   
   
   // =================== Ciclo de Vida =======================
   
   def add(String unitId, String ruleId)
   {
      // TODO verificar parametros
      
      def executer = Executer.instance
      def sessionId = executer.add(unitId, ruleId)
      
      def rule = executer.rules[sessionId]
      dolog(rule, null, ExecutionLog.STATE_ADDED)
      
      //flash.message = "rule added to executer"
      //redirect(action:"list")
      def result = ['sessionId': sessionId, 'msg': "rule added to executer"]
      
      if (!params.format || params.format == "json")
         render result as JSON
      else if (params.format == "xml")
         render result as XML
      else
        throw new Exception("format not supported "+ params.format +" options: 'xml' or 'json'")
   }
   
   
   // Agrego una regla para probar
   def init(String sessionId)
   {
      // TODO verificar parametros
      
      def executer = Executer.instance
      def rule = executer.rules[sessionId]
      
      def msg
      
      // La regla puede haber expirado pero la GUI no se actualizo,
      // entonces puedo hacer init sobre una regla ya expirada.
      // Con esto evito que de error.
      if (!rule)
      {
         msg = "La regla $sessionId ya fue removida del contexto de ejecucion"
      }
      else
      {
         try
         {
            // Los params necesarios para resolver las variables con los requests.
            def errors = executer.init(sessionId, params)
            //println "errors: " + errors
            
            if (errors)
            {
               dolog(rule, errors, ExecutionLog.STATE_ERRORED)
               msg = "rule initialization error"
            }
            else
            {
               dolog(rule, null, ExecutionLog.STATE_INITIALIZED)
               msg = "rule initialized"
            }
         }
         catch (Exception e)
         {
            dolog(rule, [e], ExecutionLog.STATE_ERRORED)
            msg = "rule initialization error"
         }
      }
      
      def result = ['sessionId': sessionId, 'msg': msg]
      
      if (!params.format || params.format == "json")
         render result as JSON
      else if (params.format == "xml")
         render result as XML
      else
        throw new Exception("format not supported "+ params.format +" options: 'xml' or 'json'")
   }
   
   
   def exec(String sessionId)
   {
      // TODO: verificar parametros
      
      def executer = Executer.instance
      def rule = executer.rules[sessionId]
      def ret
      def msg
      
      // La regla puede haber expirado pero la GUI no se actualizo,
      // entonces puedo hacer init sobre una regla ya expirada.
      // Con esto evito que de error.
      if (!rule)
      {
         msg = "La regla $sessionId ya fue removida del contexto de ejecucion"
      }
      else
      {
         try
         {
            dolog(rule, null, ExecutionLog.STATE_ACTIVE)
            
            // -------------------------------------------------------------------------
            // Paso 3: se ejecuta en un momento posterior pero indefinidamente luego del paso 2
            // TODO: permitir pasarle valores para los input declarados en la regla
            //ret = executer.execute(sessionId, ['command':'avg'])
            ret = executer.execute(sessionId, params)
            // -------------------------------------------------------------------------
         
            dolog(rule, null, ExecutionLog.STATE_FINISHED)
            
            msg = "Ejecucion exitosa"
         }
         catch (Exception e)
         {
            dolog(rule, [e], ExecutionLog.STATE_ERRORED)
            
            msg = "Ocurrio un problema en la ejecucion"
         }
      }
         
      //def xvals = ret.collect{k,v->v.value}
      def result = ['sessionId': sessionId, 'msg': msg]
      
      if (!params.format || params.format == "json")
         render result as JSON
      else if (params.format == "xml")
         render result as XML
      else
        throw new Exception("format not supported "+ params.format +" options: 'xml' or 'json'")
   }
   
   
   /**
    * Luego de exec se puede ejecutar el pedido del resultado.
    * Cuando se pide el resultado, se remueve la regla del executer.
    * TODO: si la regla está terminada pero no se pide el resultado
    *       en X período de tiempo, se debería remover automáticamente
    *       marcando el "timeout de pedido de resultado".
    * @param sessionId
    * @return
    */
   def result(String sessionId)
   {
      // TODO: verificar parametros
      
      def executer = Executer.instance
      
      // Mapa de valores simples para el redirect
      def rparams = [:]
      def msg
      
      def rule = executer.remove(sessionId)
      //if (!rule) throw new Exception("Rule with session id '$sessionId' not found")
      
      // La regla puede haber expirado pero la GUI no se actualizo,
      // entonces puedo hacer init sobre una regla ya expirada.
      // Con esto evito que de error.
      if (!rule)
      {
         msg = "La regla $sessionId ya fue removida del contexto de ejecucion"
      }
      else
      {
         // Log de archived (para que la regla tenga estado "archived")
         dolog(rule, null, ExecutionLog.STATE_ARCHIVED)

         msg = "Rule "+ rule.context.executionSessionId +" removed from executor"
         
         rule.result.each {key, value ->
            rparams[key] = value.value
         }
      }
      
      def result = ['sessionId': sessionId, 'msg': msg, 'result': rparams]
      
      if (!params.format || params.format == "json")
         render result as JSON
      else if (params.format == "xml")
         render result as XML
      else
        throw new Exception("format not supported "+ params.format +" options: 'xml' or 'json'")
   }
   
   
   /**
    * Idem a result, pero quita del executer las reglas errored o expired.
    * @param sessionId
    * @return
    */
   def ack(String sessionId)
   {
      // TODO: verificar parametros
      
      def executer = Executer.instance
      
      def msg
      
      def rule = executer.remove(sessionId)
      
      // La regla puede haber expirado pero la GUI no se actualizo,
      // entonces puedo hacer init sobre una regla ya expirada.
      // Con esto evito que de error.
      if (!rule)
      {
         msg = "La regla $sessionId ya fue removida del contexto de ejecucion"
      }
      else
      {
         // Log de archived (para que la regla tenga estado "archived")
         // La regla se queda errored o expired
         //dolog(rule, null, ExecutionLog.STATE_ARCHIVED)
   
         msg = "Rule "+ rule.context.executionSessionId +" removed from executor"
      }
      
      def result = ['sessionId': sessionId, 'msg': msg]
      
      if (!params.format || params.format == "json")
         render result as JSON
      else if (params.format == "xml")
         render result as XML
      else
        throw new Exception("format not supported "+ params.format +" options: 'xml' or 'json'")
   }
   
   // =================== /Ciclo de Vida =======================
   
   
   // testing
   def showRule(String sessionId)
   {
      // TODO: verificar parametros
      
      def executer = Executer.instance
      render executer.rules[sessionId] as grails.converters.JSON
   }
   
   /**
    * 
    */
   private void dolog(Rule rule, List errors, String state)
   {
      // Log de error
      def log = new ExecutionLog(
         ruleId: rule.id,
         sessionId: rule.context.executionSessionId,
         state: state)
      
      List msgs = []
      
      errors.each {
         msgs << it.class.toString() + ": " + it.message
      }
      
      log.msgs = msgs
      
      //println errors?.message
      
      XStream xstream = new XStream()
      
      /* TODO: log sin pretty print opcional.
       * String strXML = "";
         XStream xs = new XStream();
         StringWriter sw = new StringWriter();
         xs.marshal(this,  new CompactWriter(sw));
         strXML = sw.toString();
       */
      
      def dir
      def filename
      File file
      if (errors && errors.size()>0)
      {
         // Quita la parte dinamica de groovy
         def errors_sanitized = []
         errors.each { err ->
            errors_sanitized << org.codehaus.groovy.runtime.StackTraceUtils.sanitize(err)
         }
         
         println errors_sanitized?.message
         
         // Guarda error en filesystem porque es muy grande para la base
         // TODO: repo configurable
         dir = "./error_repo"
         filename = rule.context.executionSessionId + "_" + String.format("%tY%<tm%<tdT%<tH%<tM%<tS", log.time) + ".xml"
         file = new File("$dir/$filename")
         file << xstream.toXML(errors_sanitized)
      }
      
      // TODO: repo configurable
      dir = "./archived_rules"
      filename = rule.context.executionSessionId + "_" + String.format("%tY%<tm%<tdT%<tH%<tM%<tS", log.time) +"_"+ state +".xml"
      file = new File("$dir/$filename")
      file << xstream.toXML(rule)
      
      
      if (!log.save())
      {
         println "error al salvar log: " + log.errors
      }
   }
}