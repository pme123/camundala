package camundala.examples.invoice.worker

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
class WorkerApp

object WorkerApp:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[WorkerApp], args: _*)
end WorkerApp
