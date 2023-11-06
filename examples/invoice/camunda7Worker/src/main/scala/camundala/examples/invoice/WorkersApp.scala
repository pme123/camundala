package camundala.examples.invoice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan

//sbt> exampleInvoiceWorkerC7/run
@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = Array(
  "camundala.camunda7.worker", // for context
  "camundala.examples.invoice.worker"))
class WorkersApp

object WorkersApp:
  
  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[WorkersApp], args: _*)
end WorkersApp
