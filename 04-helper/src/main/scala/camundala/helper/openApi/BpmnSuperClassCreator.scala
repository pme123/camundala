package camundala.helper.openApi

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.info.Info

case class BpmnSuperClassCreator(
    info: Info,
    maybeDoc: Option[ExternalDocumentation]
):

  lazy val create: BpmnSuperClass =
    BpmnSuperClass(
      Option(info.getTitle).getOrElse("No Title in Open API"),
      Option(info.getVersion),
      Some(info.getDescription),
      Some(externalDoc.getDescription),
      Some(externalDoc.getUrl)
    )
  end create

  private lazy val externalDoc = maybeDoc.getOrElse(new ExternalDocumentation())

end BpmnSuperClassCreator
