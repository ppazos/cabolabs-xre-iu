package logs

class ExecutionLog {

   String ruleId // execution id
   String sessionId // id de ejecucion
   String state
   
   //int timeout // secs
   
   Date time = new Date() // momento en el que fue creado el log
   
   // Regla serializada a XML con xstream
   // TOOD: guardar en disco
   //String xmlRule
   
   // Se registra algun mensaje por ejemplo la excepcion que ocurrio
   List msgs
   static hasMany = [msgs:String]
   
   // TODO: enum
   static String STATE_ADDED       = "execution.state.added"
   static String STATE_INITIALIZED = "execution.state.initialized"
   static String STATE_ACTIVE      = "execution.state.active"
   static String STATE_EXPIRED     = "execution.state.expired"
   static String STATE_ERRORED     = "execution.state.errored"
   static String STATE_FINISHED    = "execution.state.finished"
   static String STATE_ARCHIVED    = "execution.state.archived"
   
   static constraints = {
      state(nullable:false)
      msgs(nullable:true)
   }
}