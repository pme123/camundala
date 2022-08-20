package camundala.camunda8

import com.fasterxml.jackson.databind.{DeserializationFeature, json}
import com.fasterxml.jackson.module.scala.*
import io.camunda.zeebe.client.api.JsonMapper
import io.camunda.zeebe.client.impl.ZeebeObjectMapper
import org.springframework.context.annotation.{Bean, Configuration}


trait CaseClassJsonMapperConfig :
  @Bean
  def jsonMapper: JsonMapper =
    new ZeebeObjectMapper(
      json.JsonMapper.builder()
        .addModule(DefaultScalaModule)
        .build() :: ClassTagExtensions
    )
