package camundala.examples.twitter

import camundala.camunda8.CaseClassJsonMapperConfig
import io.camunda.zeebe.spring.client.EnableZeebeClient
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration


@Configuration
class AppConfig extends CaseClassJsonMapperConfig

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = Array("classpath*:twitter*.bpmn"))
class TwitterExampleApplication

object TwitterExampleApplication :

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[TwitterExampleApplication], args*)

