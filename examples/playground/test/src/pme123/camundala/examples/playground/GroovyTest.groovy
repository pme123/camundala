package pme123.camundala.examples.playground

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.Execution
import org.junit.Test
import static groovy.test.GroovyAssert.*
import static org.junit.Assert.assertEquals

class GroovyTest {


    def execution = [
            getVariable: { String key -> [
                    'existingAddr' : "{	    'id': '362350000000135',		  'street': 'Murtengasse 22',		  'zipCode': '3600',		  'city': 'Thun',		  'countryIso': 'CH'		}",
                    'newAddr' : "{		  'street': 'Sonnenweg 28',		  'zipCode': '6414',		  'city': 'Oberarth',		  'countryIso': 'CH'		}",
                    'initiator' : "{    'id': '234234234',    'email': 'hans@example.com',    'username': 'hans',    'languageIso': 'de'}",
                    'customerId' : 'myCompany'
            ].get(key)},
            getBusinessKey: { 'myBusinessKey' }
    ] as org.camunda.bpm.engine.delegate.DelegateExecution

    @Test
    void indexOutOfBoundsAccess() {
        def custId = execution.getVariable("customerId")
        def initiator = execution.getVariable("initiator")
        def businessKey = execution.getBusinessKey()
        assertEquals('myCompany', custId)
        assertEquals("{    'id': '234234234',    'email': 'hans@example.com',    'username': 'hans',    'languageIso': 'de'}", initiator)
        assertEquals('myBusinessKey', businessKey)
    }
}
