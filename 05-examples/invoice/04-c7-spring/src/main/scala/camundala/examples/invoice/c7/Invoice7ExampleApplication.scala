package camundala.examples.invoice.c7

import org.camunda.bpm.example.invoice.c7.InvoiceProcessApplication
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.event.EventListener

//sbt> exampleInvoiceC7/run
@SpringBootApplication
@EnableProcessApplication
class Invoice7ExampleApplication

object Invoice7ExampleApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[Invoice7ExampleApplication], args*)

  lazy val invoicePa = new InvoiceProcessApplication()

  @EventListener
  def onPostDeploy(event: PostDeployEvent): Unit =
    invoicePa.startFirstProcess(event.getProcessEngine)
end Invoice7ExampleApplication
