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

class RuleController {

   static defaultAction = "list"
   
   /**
    * List available units and rules
    */
   def list() {
      
      // TODO:
      // El registry deberia escanear el repo y cargar las reglas
      def reg = Registry.instance
      
      
      // El executer es para preguntar por datos de las reglas que se estan ejecutando ahora
      return [units: reg.getUnits(), executer:Executer.instance]
   }
   
   /**
    * TODO: poner esta accion en el Controller de ExecutionLog
    * Listado de reglas que ya terminaron (archivadas, errored o expired).
    * @return
    */
   def listFinished() {
      
      if (!params.max) params.max = 15
      if (!params.offset) params.offset = 0
      
      def finished = ExecutionLog.withCriteria {
         or {
            eq('state', ExecutionLog.STATE_ERRORED)
            eq('state', ExecutionLog.STATE_ARCHIVED)
            eq('state', ExecutionLog.STATE_EXPIRED)
         }
         if (params.max instanceof Integer)
            maxResults(params.max)
         else
            maxResults(Integer.parseInt(params.max))
         
         if (params.offset instanceof Integer)
            firstResult(params.offset)
         else
            firstResult(Integer.parseInt(params.offset))
         
         order("time", "desc")
      }
      
      def count = ExecutionLog.withCriteria {
         projections {
            count("id")
         }
         or {
            eq('state', ExecutionLog.STATE_ERRORED)
            eq('state', ExecutionLog.STATE_ARCHIVED)
            eq('state', ExecutionLog.STATE_EXPIRED)
         }
      }
      
      //println count[0]
      
      render(view:"/executionLog/list",
            model:[
               executionLogInstanceList:finished,
               executionLogInstanceTotal:count[0]])
   }
   
   
   
   def loadRules() {
      
      // TODO: http://code.google.com/p/xre-ui/issues/detail?id=2
      // El registry deberia escanear el repo y cargar las reglas
      def reg = Registry.instance
      
      def rules = ['rules/rule_date_diff.xrl.xml',
                   'rules/rule4.xrl.xml',
                   'rules/rule5.xrl.xml',
                   'rules/rule6.xrl.xml',
                   'rules/rule7_error_url.xrl.xml']
      def unit
      
      // Para agarrar errores en la carga y el parseo
      rules.each {
         try
         {
            unit = new Parser().parse(new File(it))
            reg.addUnit(unit)
            
            // TODO: guardar log de carga correcta
         }
         catch (Exception e)
         {
            // TODO: guardar log de carga erronea
            println e.message
         }
         finally
         {
            
         }
      }
      
      redirect(action:"list")
   }
   
   
   // http://code.google.com/p/xre-ui/issues/detail?id=2
   def repoRules() {
      
      def ruleFiles = [:]
      
      
      // TODO: sacar a config
      def repo = 'rules/'
      
      def p = ~/.*\.xml/
      new File(repo).eachFileMatch(p) { f ->
         
         ruleFiles[f] = new XmlSlurper().parseText(f.getText())
      }
      
      
      def reg = Registry.instance
      //return [units: reg.getUnits(), executer:Executer.instance]
      
      
      return [ruleFiles: ruleFiles, reg: reg]
   }
   
   
   // =================== Ciclo de Vida =======================
   
   def add(String unitId, String ruleId)
   {
      def executer = Executer.instance
      def sessId = executer.add(unitId, ruleId)
      
      def rule = executer.rules[sessId]
      dolog(rule, null, ExecutionLog.STATE_ADDED)
      
      flash.message = "rule added to executer"
      redirect(action:"list")
   }
   
   
   // Agrego una regla para probar
   def init(String sessId)
   {
      def executer = Executer.instance
      //def sessId = executer.add(unitId, ruleId)
      def rule = executer.rules[sessId]
      
      // La regla puede haber expirado pero la GUI no se actualizo,
      // entonces puedo hacer init sobre una regla ya expirada.
      // Con esto evito que de error.
      if (!rule)
      {
         flash.message = "La regla $sessId ya fue removida del contexto de ejecucion"
      }
      else
      {
         try
         {
            // Los params necesarios para resolver las variables con los requests.
            def errors = executer.init(sessId, params)
            println "errors: " + errors
            
            if (errors)
            {
               dolog(rule, errors, ExecutionLog.STATE_ERRORED)
               flash.message = "rule initialization error"
            }
            else
            {
               dolog(rule, null, ExecutionLog.STATE_INITIALIZED)
               flash.message = "rule initialized"
            }
         }
         catch (Exception e)
         {
            dolog(rule, [e], ExecutionLog.STATE_ERRORED)
            flash.message = "rule initialization error"
         }
      }
      
      redirect(action:"list")
   }
   
   
   def exec(String sessId)
   {
      def executer = Executer.instance
      def rule = executer.rules[sessId]
      def ret
      
      // La regla puede haber expirado pero la GUI no se actualizo,
      // entonces puedo hacer init sobre una regla ya expirada.
      // Con esto evito que de error.
      if (!rule)
      {
         flash.message = "La regla $sessId ya fue removida del contexto de ejecucion"
      }
      else
      {
         try
         {
            dolog(rule, null, ExecutionLog.STATE_ACTIVE)
            
            // -------------------------------------------------------------------------
            // Paso 3: se ejecuta en un momento posterior pero indefinidamente luego del paso 2
            // TODO: permitir pasarle valores para los input declarados en la regla
            //ret = executer.execute(sessId, ['command':'avg'])
            ret = executer.execute(sessId, params)
            // -------------------------------------------------------------------------
         
            dolog(rule, null, ExecutionLog.STATE_FINISHED)
         }
         catch (Exception e)
         {
            dolog(rule, [e], ExecutionLog.STATE_ERRORED)
         }
      }
         
      //def xvals = ret.collect{k,v->v.value}
      
      //render ret as XML // La Var no tiene value!!!
      // el resultado esta accesible en rule.result
      redirect(action:"list")
   }
   
   
   /**
    * Luego de exec se puede ejecutar el pedido del resultado.
    * Cuando se pide el resultado, se remueve la regla del executer.
    * TODO: si la regla está terminada pero no se pide el resultado
    *       en X período de tiempo, se debería remover automáticamente
    *       marcando el "timeout de pedido de resultado".
    * @param sessId
    * @return
    */
   def result(String sessId)
   {
      def executer = Executer.instance
      
      // Mapa de valores simples para el redirect
      def rparams = [:]
      
      
      def rule = executer.remove(sessId)
      //if (!rule) throw new Exception("Rule with session id '$sessionId' not found")
      
      // La regla puede haber expirado pero la GUI no se actualizo,
      // entonces puedo hacer init sobre una regla ya expirada.
      // Con esto evito que de error.
      if (!rule)
      {
         flash.message = "La regla $sessId ya fue removida del contexto de ejecucion"
      }
      else
      {
         // Log de archived (para que la regla tenga estado "archived")
         dolog(rule, null, ExecutionLog.STATE_ARCHIVED)

         flash.message = "Rule "+ rule.context.executionSessionId +" removed from executor"
         
         rule.result.each {key, value ->
            rparams[key] = value.value
         }
      }
      
      // Los resultados se ven en la URL del redirect
      redirect(action:"list", params:rparams)
   }
   
   
   /**
    * Idem a result, pero quita del executer las reglas errored o expired.
    * @param sessId
    * @return
    */
   def ack(String sessId)
   {
      def executer = Executer.instance
      
      // Mapa de valores simples para el redirect
      def rparams = [:]
      
      def rule = executer.remove(sessId)
      
      // La regla puede haber expirado pero la GUI no se actualizo,
      // entonces puedo hacer init sobre una regla ya expirada.
      // Con esto evito que de error.
      if (!rule)
      {
         flash.message = "La regla $sessId ya fue removida del contexto de ejecucion"
      }
      else
      {
         // Log de archived (para que la regla tenga estado "archived")
         // La regla se queda errored o expired
         //dolog(rule, null, ExecutionLog.STATE_ARCHIVED)
   
         flash.message = "Rule "+ rule.context.executionSessionId +" removed from executor"
         
         rule.result.each {key, value ->
            rparams[key] = value.value
         }
      }
      
      // Los resultados se ven en la URL del redirect
      redirect(action:"list", params:rparams)
   }
   
   // =================== /Ciclo de Vida =======================
   
   
   
   def showRule(String sessId)
   {
      def executer = Executer.instance
      render executer.rules[sessId] as grails.converters.XML
   }
   
   
   
   // test de hacer un request
   def requi()
   {
      def url = "http://localhost:8089/number_aggregation.xml"
      def http = new HTTPBuilder(url)
      http.request( Method.GET, ContentType.XML ) {
         
         //uri.path = '/ajax/services/search/web'
         uri.query =[ command:'avg' ]
       
         headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
       
         // response handler for a success response code:
         response.success = { resp, xml ->
         
            // xml es groovy.util.slurpersupport.NodeChild
            
            println 'XML: ' + xml.name()
         }
      }
      
      render "fin request"
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