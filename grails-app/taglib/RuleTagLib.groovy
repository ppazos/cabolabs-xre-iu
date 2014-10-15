import logs.ExecutionLog

class RuleTagLib {

   /**
    * Si la regla fue agregada al contexto de ejecucion pero no fue inicializada.
    */
   def showIfAdded = { attrs, body ->
      
      if (!attrs.rule) throw new Exception("rule is mandatory")
      
      def query = ExecutionLog.where {
         sessionId == attrs.rule.context.executionSessionId
      }
      def log = query.list(sort:"time", order:"desc", max:1)[0]
      
      if (log.state == ExecutionLog.STATE_ADDED)
      {
         out << body()
      }
   }
   
   def showIfInitialized = { attrs, body ->
      
      if (!attrs.rule) throw new Exception("rule is mandatory")
      
      def query = ExecutionLog.where {
         sessionId == attrs.rule.context.executionSessionId
      }
      def log = query.list(sort:"time", order:"desc", max:1)[0]
      
      // Si se agregó pero no se inicializó, no hay log.
      //if (!log) return
      
      def currentState = log.state
         
      if (currentState == ExecutionLog.STATE_INITIALIZED)
      {
         out << body()
      }
   }
   
   def showIfActive = { attrs, body ->
      
      if (!attrs.rule) throw new Exception("rule is mandatory")
      
      def query = ExecutionLog.where {
         sessionId == attrs.rule.context.executionSessionId
      }
      def log = query.list(sort:"time", order:"desc", max:1)[0]
      
      // Si se agregó pero no se inicializó, no hay log.
      //if (!log) return
      
      def currentState = log.state
         
      if (currentState == ExecutionLog.STATE_ACTIVE)
      {
         out << body()
      }
   }
   
   def showIfFinished = { attrs, body ->
      
      if (!attrs.rule) throw new Exception("rule is mandatory")
      
      def query = ExecutionLog.where {
         sessionId == attrs.rule.context.executionSessionId
      }
      def log = query.list(sort:"time", order:"desc", max:1)[0]
      
      // Si se agregó pero no se inicializó, no hay log.
      //if (!log) return
      
      def currentState = log.state
         
      if (currentState == ExecutionLog.STATE_FINISHED)
      {
         out << body()
      }
   }
   
   def showIfErrored = { attrs, body ->
      
      if (!attrs.rule) throw new Exception("rule is mandatory")
      
      def query = ExecutionLog.where {
         sessionId == attrs.rule.context.executionSessionId
      }
      def log = query.list(sort:"time", order:"desc", max:1)[0]
      
      // Si se agregó pero no se inicializó, no hay log.
      //if (!log) return
      
      def currentState = log.state
         
      if (currentState == ExecutionLog.STATE_ERRORED)
      {
         out << body()
      }
   }
   
   def showIfExpired = { attrs, body ->
      
      if (!attrs.rule) throw new Exception("rule is mandatory")
      
      def query = ExecutionLog.where {
         sessionId == attrs.rule.context.executionSessionId
      }
      def log = query.list(sort:"time", order:"desc", max:1)[0]
      
      // Si se agregó pero no se inicializó, no hay log.
      //if (!log) return
      
      def currentState = log.state
         
      if (currentState == ExecutionLog.STATE_EXPIRED)
      {
         out << body()
      }
   }
}