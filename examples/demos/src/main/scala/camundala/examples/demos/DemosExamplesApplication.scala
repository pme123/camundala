package camundala.examples.demos

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.camunda.bpm.BpmPlatform
import org.springframework.context.event.EventListener

import javax.annotation.PostConstruct

@SpringBootApplication
@EnableProcessApplication
class DemosExamplesApplication

object DemosExamplesApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[DemosExamplesApplication], args: _*)
