package camundala.examples.invoice

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.camunda.bpm.BpmPlatform
import org.camunda.bpm.example.invoice.InvoiceProcessApplication
import org.springframework.context.event.EventListener

import javax.annotation.PostConstruct

@SpringBootApplication
@EnableProcessApplication
class InvoiceServletProcessApplication

object InvoiceServletProcessApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[InvoiceServletProcessApplication], args: _*)

  lazy val invoicePa = new InvoiceProcessApplication()

  @EventListener
  def onPostDeploy(event: PostDeployEvent): Unit =
    println("event.getProcessEngine: " + event.getProcessEngine)
    invoicePa.startFirstProcess(event.getProcessEngine)
