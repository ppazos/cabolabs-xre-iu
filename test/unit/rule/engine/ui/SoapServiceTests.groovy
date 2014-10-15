package rule.engine.ui

import grails.test.mixin.*
import org.junit.*
import parser.Parser
import wslite.soap.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(SoapService)
class SoapServiceTests {

   void testAdd()
   {
      def reg = Registry.instance
      
      def rules = ['rules/rule4.xrl.xml',
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
            
            //units: reg.getUnits()
            //in="${units.keySet()}" var="unitId">
            //units[unitId].rules
            //in="${units[unitId].rules.keySet()}" var="ruleId">
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
   }
   
   void testAddRule()
   {
      //fail "Implement me"
      
      def client = new SOAPClient('http://localhost:8080/rule-engine-ui/services/soap?wsdl')
      def response = client.send(SOAPAction:'http://localhost:8080/rule-engine-ui/services/soap/add') {
          body {
              add('xmlns':'http://ui.engine.rule/') {
                  unitId('')
                  ruleId('')
              }
          }
      }
      
      println response
   }
}