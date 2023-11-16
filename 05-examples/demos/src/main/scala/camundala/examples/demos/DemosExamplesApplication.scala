package camundala.examples.demos

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnableProcessApplication
class DemosExamplesApplication

object DemosExamplesApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[DemosExamplesApplication], args: _*)
