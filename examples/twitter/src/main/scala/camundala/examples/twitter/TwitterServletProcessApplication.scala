package camundala.examples.twitter

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@EnableProcessApplication
class TwitterServletProcessApplication

object TwitterServletProcessApplication :

  def main(args: Array[String]): Unit =
      SpringApplication.run(classOf[TwitterServletProcessApplication], args:_*)

