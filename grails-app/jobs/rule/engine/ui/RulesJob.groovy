package rule.engine.ui

import java.util.List

import com.thoughtworks.xstream.XStream
import rules.Rule
import execution.Executer
import logs.ExecutionLog
import grails.gorm.DetachedCriteria

// http://grails-plugins.github.com/grails-quartz/guide/scheduling.html
class RulesJob {
   
   def executer = Executer.instance
   
   
   static triggers = {
      //simple repeatInterval: 5000l // execute job once in 5 seconds
      simple repeatInterval: 20000l // execute job once in 20 seconds
   }

   
   def execute()
   {
      Date now = new Date()
      
      
      //long timeout_msec = 3600 * 1000 // 1 hora // FIXME: configurable
      long timeout_msec = 60 * 1000 // 10 segundo // FIXME: configurable
      long diff_msec = now.getTime() - timeout_msec // msecs del date de timeout
      Date timeout = new Date(diff_msec)
      
      // Los logs antes del threshold ya fueron revisados
      long threshold_msec = 120 * 1000 // 60 segundos
      long diff_th_msec = now.getTime() - threshold_msec // msecs del date de timeout
      Date threshold = new Date(diff_th_msec)
      
      
      // Logs que tienen el id igual que otro pero con menor tiempo (son los que no me interesan)
      
      
      def currentStates

      /*
      currentStates = ExecutionLog.withCriteria {
         projections {
            groupProperty 'sessionId'
            max 'time'
         }
         or {
            eq('state', ExecutionLog.STATE_ERRORED)
            eq('state', ExecutionLog.STATE_FINISHED)
         }
         lt('time', timeout) // Si el time es menor que el timeout, es que el timeout ya paso
         
         // agregando un criterio de fecha minima, evito de
         // seleccionar cosas viejas que ya fueron procesadas.
         // el criterio es que "todo lo que este antes de un 
         // tiempo menor al timeout + el delay de ejecucion del 
         // job, ya fue revisado en ejecuciones anteriores del job".
         gt('time', threshold)
      }
      
      println "currentStates: " + currentStates
      */
      
      
      // Para ponerlos en expired y sacarlos del contexto de ejecucion:
      // - Quiero los agregados que no fueron inicializados
      // - Quiero los inicializados que no fueron ejecutados
      
      currentStates = ExecutionLog.findAll( 
         "FROM ExecutionLog el " +
         "WHERE (el.state = ? OR el.state = ?) AND " +
         "      el.time < ? AND " + // timeout
         "      NOT EXISTS( " + // no debe existir otro log con el mismo sessionId y mayor fecha
         "        SELECT el1.id " +
         "        FROM ExecutionLog el1 " +
         "        WHERE el1.sessionId = el.sessionId AND el1.time > el.time " +
         "      )",
         [ExecutionLog.STATE_ADDED, ExecutionLog.STATE_INITIALIZED, timeout]
      )
      
      println "currentStates add/init: " + currentStates

      
      // Expired
      currentStates.each { elog ->
         
         //println elog.id + ": " + elog.state + ", " + elog.sessionId + ", " + elog.time
         
         // Sino existe creo que no hace nada ...
         def rule = executer.remove(elog.sessionId)
         
         // sino hay regla, es que ya hice el expired
         // FIXME: pero si el estado actual es expired, no deberia tener
         //        el log en el resultado de la busqueda, hay que afinar el criterio...
         // El tema es que luego de que pasa el threshold ya no aparece en el resultado.
         if (rule)
            dolog(rule, null, ExecutionLog.STATE_EXPIRED)
         else
           println "rule es null"
      }
      
      
      // Para hacerles ack y sacarlos del contexto de ejecucion:
      // - Quiero los logs errados
      // - Quiero los finalizados que no se les pidio el resultado
      //   (no se hace get result, eso lo deberia hacer el usuario, se hace ack).
      
      currentStates = ExecutionLog.findAll(
         "FROM ExecutionLog el " +
         "WHERE (el.state = ? OR el.state = ?) AND " +
         "      el.time < ? AND el.time > ? AND " + // timeout
         "      NOT EXISTS( " + // no debe existir otro log con el mismo sessionId y mayor fecha
         "        SELECT el1.id " +
         "        FROM ExecutionLog el1 " +
         "        WHERE el1.sessionId = el.sessionId AND el1.time > el.time " +
         "      )",
         [ExecutionLog.STATE_ERRORED, ExecutionLog.STATE_FINISHED, timeout, threshold]
      )
      
      println "currentStates err/fin: " + currentStates
      
      
      // Como esto no crea un log solo hace remove, en la consulta anterior va a seguir
      // apareciendo el resultado. Para que deje de traer resultados viejos, uso el threshold.
      
      
      // Ack: solo remueve, no cambia de estado
      currentStates.each { elog ->
         
         //println elog.id + ": " + elog.state + ", " + elog.sessionId + ", " + elog.time
         
         // Sino existe creo que no hace nada ...
         def rule = executer.remove(elog.sessionId)
         
         /*
         if (rule)
            dolog(rule, null, ExecutionLog.STATE_EXPIRED)
         else
           println "rule es null"
         */
      }
      
      
      

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