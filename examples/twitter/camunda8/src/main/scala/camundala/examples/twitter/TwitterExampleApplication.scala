package camundala.examples.twitter

import io.camunda.zeebe.spring.client.EnableZeebeClient
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = Array("classpath*:twitter*.bpmn"))
class TwitterExampleApplication

object TwitterExampleApplication :

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[TwitterExampleApplication], args:_*)

