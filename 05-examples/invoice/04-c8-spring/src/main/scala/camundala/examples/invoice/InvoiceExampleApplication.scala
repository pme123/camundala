package camundala.examples.invoice

import camundala.camunda8.CaseClassJsonMapperConfig
import io.camunda.zeebe.spring.client.annotation.Deployment
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig extends CaseClassJsonMapperConfig

@SpringBootApplication
@Deployment(resources =
  Array("classpath*:invoice.v3.bpmn", "classpath*:reviewInvoice.v2.bpmn", "classpath*:invoice*.dmn")
)
class InvoiceExampleApplication

object InvoiceExampleApplication:

  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[InvoiceExampleApplication], args*)
end InvoiceExampleApplication
