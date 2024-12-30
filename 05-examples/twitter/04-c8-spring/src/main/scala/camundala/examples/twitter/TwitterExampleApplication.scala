package camundala.examples.twitter

import camundala.camunda8.CaseClassJsonMapperConfig
import io.camunda.zeebe.spring.client.annotation.Deployment
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig extends CaseClassJsonMapperConfig

@SpringBootApplication
@Deployment(resources = Array("classpath*:twitter*.bpmn"))
class TwitterExampleApplication

object TwitterExampleApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[TwitterExampleApplication], args*)
end TwitterExampleApplication
