package camundala.examples.demos.worker

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

//sbt> exampleDemosWorker/run
@SpringBootApplication
@Component("ValiantProductWorkerTestApp")
@ConfigurationPropertiesScan
@ComponentScan(basePackages =
  Array(
    "camundala.camunda7.worker.context", // for context
    "camundala.examples.demos.worker"
  )
)
class WorkerApp

object WorkerApp:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[WorkerApp], args*)
end WorkerApp
