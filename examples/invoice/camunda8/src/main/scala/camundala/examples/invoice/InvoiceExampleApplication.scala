package camundala.examples.invoice

import io.camunda.zeebe.spring.client.EnableZeebeClient
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.event.EventListener

import javax.annotation.PostConstruct

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = Array("classpath*:invoice.v3.bpmn","classpath*:*nvoice*.dmn"))
class InvoiceExampleApplication

object InvoiceExampleApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[InvoiceExampleApplication], args: _*)

