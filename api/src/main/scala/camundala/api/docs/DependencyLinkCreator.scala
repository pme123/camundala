package camundala.api
package docs

case class DependencyLinkCreator()(implicit
    val apiConfig: ApiConfig,
    val configs: Seq[PackageConf],
    val releaseConfig: ReleaseConfig
) extends DependencyCreator {

  def createIndex(dependencyGraph: String): Unit = {
    val indexPage = create("Valiant Process Documentation", dependencyGraph)
    os.write.over(apiConfig.basePath / "src" / "docs" / "index.md", indexPage)
  }

  def createDependencies(dependencyGraph: String): Unit = {
    val depPage = create("Dependencies Overview", dependencyGraph)
    os.write.over(
      apiConfig.basePath / "src" / "docs" / "overviewDependencies.md",
      depPage
    )
  }

  private def create(title: String, dependencyGraph: String): String = {
    val packages: Seq[Package] = configs
      .groupBy(_.name)
      .map { case _ -> v =>
        Package(v.maxBy(_.version))
      }
      .toSeq

    def linkGroup(projectGroup: ProjectGroup) =
      s"""
         |## $title
         |${packages
        .filter(p => apiConfig.gitConfigs.hasProjectGroup(p.name, projectGroup))
        .map { co =>
          s"""- **${co.name}** [API Doc](../${co.name}/OpenApi.html "${co.name} API Documentation") - [Dependencies](./dependencies/${co.name}.html "${co.name} Dependencies")"""
        }
        .mkString("\n")}
         |""".stripMargin

    s"""
       |{%
       |laika.versioned = true
       |%}
       |
       |# $title
       |${releaseConfig.releasedLabel}
       |
       |## BPMN Projects
       |${printGraph(dependencyGraph)}
       |
       |$printColorLegend
       |
       |${releaseConfig.projectGroups.map { group =>
      linkGroup(group)
    }}
       |""".stripMargin
  }

}
