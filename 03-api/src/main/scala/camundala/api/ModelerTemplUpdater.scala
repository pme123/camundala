package camundala.api

import camundala.api.*
import camundala.api.docs.ApiProjectConf
import io.circe.parser
import io.circe.syntax.*

case class ModelerTemplUpdater(apiConfig: ApiConfig):

  def update(): Unit =
    os.makeDir.all(templConfig.templatePath)
    projectsConfig.init // pulls all dependencies.
    apiProjectConfig.dependencies
      .foreach: c =>
        val path = projectsConfig.gitDir / c.name / templConfig.templateRelativePath
        println(s"Fetch dependencies: ${c.name} > $path")
        if os.exists(path) then
          os.walk(path)
            .filter: p =>
              println(s" - PATH: ${p.last}")
              p.last.startsWith(c.name)
            .foreach: p =>
              println(s" - PATH filtered: ${p.last}")
              parser.parse(os.read(p))
                .flatMap:
                  _.as[MTemplate]
                .map:
                  case t
                      if t.elementType.value == AppliesTo.`bpmn:CallActivity` &&
                        t.name != apiProjectConfig.name =>
                    val idWithPrefix = s"${apiConfig.projectRefId(t.id)._1}:${t.id}"
                    println(s"Extend with prefix: $idWithPrefix")
                    val newTempl =
                      t.copy(properties =
                        t.properties
                          .map:
                            case p if p.value == t.id =>
                              p.copy(value = idWithPrefix)
                            case p => p
                      ).asJson
                        .deepDropNullValues
                        .toString
                    os.write.over(templConfig.templatePath / p.last, newTempl)

                  case t =>
                    println(s"Just copy MTemplate: ${t.id}")
                    os.copy(p, templConfig.templatePath / p.last, replaceExisting = true)
        else
          println(s"No Modeler Templates for $path")
        end if
  end update

  private lazy val templConfig = apiConfig.modelerTemplateConfig
  private lazy val projectsConfig = apiConfig.projectsConfig
  private lazy val apiProjectConfig = ApiProjectConf(apiConfig.projectConfPath)

end ModelerTemplUpdater
