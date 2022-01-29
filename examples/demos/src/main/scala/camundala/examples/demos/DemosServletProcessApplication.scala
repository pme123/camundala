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
class DemosServletProcessApplication

object DemosServletProcessApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[DemosServletProcessApplication], args: _*)
