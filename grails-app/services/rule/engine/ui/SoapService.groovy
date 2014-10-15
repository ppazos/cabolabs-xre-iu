package rule.engine.ui

import com.thoughtworks.xstream.XStream
import execution.Executer
import java.util.List
import java.util.Map
import logs.ExecutionLog
import rules.Registry
import rules.Rule

import javax.jws.*                 // cxfjax @WebMethod ...
import javax.xml.bind.annotation.* // para las anotaciones

import javax.xml.ws.*

class SoapService {

   //static expose = ['cxf']
   static expose = ['cxfjax'] // http://grails.org/plugin/cxf
   static exclude = ["dolog"]
   
   
   final Executer executer = Executer.instance
   
   
   /**
    * Devuelve las reglas cargadas en el registro de reglas.
    * @return
    */
   //@ResponseWrapper(className = "java.util.HashMap")
   //public LinkedHashMap<String, ArrayList<String>> rules()
   //public Map<String, List<String>> rules()
   /**
    * Error con Array:
    * <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
         <soap:Body>
            <soap:Fault>
               <faultcode>soap:Server</faultcode>
               <faultstring>
                  rule.engine.ui.SoapService.rules()[Ljava/lang/Object;
               </faultstring>
            </soap:Fault>
         </soap:Body>
      </soap:Envelope>
    */
   //@ResponseWrapper(className = "java.lang.reflect.Array")
   //public String[] rules()
   //public LinkedHashMap<String, String> rules()
   public Object[] rules()
   {
      println "rules"
      
      def reg = Registry.instance
      
      println reg
      
      def units = reg.getUnits()
      
      println units
      
      //def res = [:]
      // lo creo con el tipo especifico
//      LinkedHashMap<String, String> res = new LinkedHashMap<String, String>()
//      String[] res = new String[units.size()] // TEST
      Object[] res = new Object[units.size()]
      
      int i = 0
      units.each { unitId, unit ->
         
         //res[unitId] = []
//         res.put(unitId, unitId) // test
         
         res[i] = unitId
         
         /*
         unit.rules.each { ruleId, rule ->
            
            res[unitId] << ruleId
         }
         */
         
         i++
      }
      
      println res

      return res
   }
   
   //http://jax-ws.java.net/jax-ws-21-ea1/docs/annotations.html
   @WebMethod
   @XmlElementWrapper
   public List rulesList()
   {
      println "rules"
      
      def reg = Registry.instance
      
      println reg
      
      def units = reg.getUnits()
      
      println units
      
      def res = []
      // lo creo con el tipo especifico
//      LinkedHashMap<String, String> res = new LinkedHashMap<String, String>()

      units.each { unitId, unit ->
         
         res << unitId
         
         /*
         unit.rules.each { ruleId, rule ->
            
            res[unitId] << ruleId
         }
         */
      }
      
      println res

      return res
   }
   
   
   public Object[][] rules2()
   {
      println "rules2"
      
      def reg = Registry.instance
      
      println reg
      
      def units = reg.getUnits()
      
      println units
      
      Object[] res = new Object[units.size()]
      
      int i = 0
      units.each { unitId, unit ->
         
         //res[unitId] = []
//         res.put(unitId, unitId) // test
         
         //res[i] = unitId
         res[i] = new Object[unit.rules.size()+1]
         
         // En el lugar 0 pongo el unit id, en los siguientes los ruleIds
         res[i][0] = unitId
         
         int j = 1
         unit.rules.each { ruleId, rule ->
            
            //res[unitId] << ruleId
            
            res[i][j] = ruleId
            j++
         }
         
         i++
      }
      
      println res

      return res
   }
   
   
   /**
    * 
    * @param unitId
    * @param ruleId
    * @return
    */
   public String add(String unitId, String ruleId)
   {
      def sessId = executer.add(unitId, ruleId) // Tira excepcion sino encuentra la regla
      return sessId
   }
   
   
   /**
    * 
    * @param sessId
    * @param params
    * @return
    */
   public Map init(String sessId, Map params)
   {
      def rule = executer.rules[sessId]
      
      // Los params necesarios para resolver las variables con los requests.
      def errors = executer.init(sessId, params) // Tira excepcion si no encuentra el sessId

      if (errors)
      {
         dolog(rule, errors, ExecutionLog.STATE_ERRORED)
         return errors
      }
      
      dolog(rule, null, ExecutionLog.STATE_INITIALIZED)
      return null
   }
   
   
   /**
    * 
    * @param sessId
    * @param params
    */
   public void exec(String sessId, Map params)
   {
      // FIXME: poner esto como un metodo
      def rule = executer.rules[sessId]
      
      if (!rule) throw new Exception("Rule with session id '$sessionId' not found")
      
      def ret

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
   
   
   /**
    * 
    * @param sessId
    * @return
    */
   public Map result(String sessId)
   {
      def rule = executer.remove(sessId)
      if (!rule) throw new Exception("Rule with session id '$sessionId' not found")
      
      
      // Log de archived (para que la regla tenga estado "archived")
      dolog(rule, null, ExecutionLog.STATE_ARCHIVED)

      
      // Mapa de valores simples
      def rparams = [:]
      rule.result.each {key, value ->
         rparams[key] = value.value
      }
      
      return rparams
   }
   
   
   /**
    * 
    * @param sessId
    * @return
    */
   public Map ack(String sessId)
   {
      def rule = executer.remove(sessId)
      if (!rule) throw new Exception("Rule with session id '$sessionId' not found")

      
      // Mapa de valores simples para el redirect
      def rparams = [:]
      rule.result.each {key, value ->
         rparams[key] = value.value
      }
      
      return rparams
   }
   
   
   /**
    * 
    * @param rule
    * @param errors
    * @param state
    */
   private void dolog(Rule rule, List errors, String state)
   {
      // Log de error
      def log = new ExecutionLog(
         ruleId: rule.id,
         sessionId: rule.context.executionSessionId,
         state: state)
      
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