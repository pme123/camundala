package camundala.helper.dev.company.docs

import camundala.api.{ApiConfig, DocProjectConfig, ProjectGroup}
import os.{pwd, write}

case class DependencyGraphCreator()(using
    val apiConfig: ApiConfig,
    val configs: Seq[DocProjectConfig]
) extends DependencyCreator:

  def createIndex: String =
    create(
      configs,
      pack =>
        s"""|   click ${pack.name} href "../${pack.name}/OpenApi.html" "${pack.name} API Documentation""""
    )

  def createDependencies(using configs: Seq[DocProjectConfig]): String =
    create(
      configs,
      pack =>
        s"""|   click ${pack.name} href "./dependencies/${pack.name}.html" "${pack.name} Dependencies""""
    )

  def createProjectDependencies(using configs: Seq[DocProjectConfig]): Unit =
    val graphsForProjects = treeForEachProjects(configs)
    graphsForProjects.foreach(g =>
      write.over(
        apiConfig.basePath / "src" / "docs" / "dependencies" / s"${g.name}.md",
        g.graph
      )
    )
  end createProjectDependencies

  private def create(
      versionedConfigs: Seq[DocProjectConfig],
      link: Package => String
  ): String =
    val configs = versionedConfigs
      .groupBy(_.projectName)
      .map { case _ -> v => v.maxBy(_.version) }
      .toSeq

    def subgraph(projectGroup: ProjectGroup) =
      s"""
         |    subgraph ${projectGroup.name}
         |    ${configs
          .filter(p => apiConfig.projectsConfig.hasProjectGroup(p.projectName, projectGroup))
          .map { co =>
            s"${co.projectName}(${co.projectName})"
          }
          .mkString(" & ")}
         |    end
         |    """.stripMargin

    s"""
       |flowchart TB
       |${configs
        .map { config =>
          val color     = colorMap.getOrElse(config.projectName, "#fff")
          val depConfig = getUniqueDependencies(config, configs)
          val tree      =
            if depConfig.nonEmpty then
              s"${config.projectName} --> ${depConfig
                  .map(d => d.projectName)
                  .mkString(" & ")}"
            else config.projectName
          s"""
             |   $tree
             |   ${link(Package(config.projectName, config.minorVersion))}
             |   style ${config.projectName} fill:$color
             |""".stripMargin
        }
        .mkString("\n")}
       |${apiConfig.projectGroups
        .map { group =>
          subgraph(group)
        }
        .mkString("\n\n")}
       |$groupStyles
       |""".stripMargin
  end create

  private def treeForEachProjects(
      configs: Seq[DocProjectConfig]
  ): Seq[ProjectTree] =
    val groupedConfigs = configs.filter(!_.isWorker).groupBy(_.projectName)
    groupedConfigs.map { case name -> gConfigs =>
      val trees = toPackageTree(gConfigs, configs)
      ProjectTree(name, treeForEachProject(trees))
    }.toSeq
  end treeForEachProjects

  case class PackageTree(
      mainPackage: Package,
      fromPackages: Seq[Package],
      toPackages: Seq[Package]
  ):
    lazy val allTrees: Seq[Package] =
      mainPackage +: (fromPackages ++ toPackages)
  end PackageTree

  case class ProjectTree(name: String, graph: String)

  private def toPackageTree(
      configs: Seq[DocProjectConfig],
      allConfigs: Seq[DocProjectConfig]
  ): Seq[PackageTree] =
    configs.map { c =>
      val mainPackage  = Package(c.projectName, c.minorVersion)
      val fromPackages = allConfigs
        .filter { aC =>
          aC.dependencies.exists(aD =>
            aD.projectName == mainPackage.name && aD.minorVersion == mainPackage.minorVersion
          )
        }
        .map { aC => Package(aC.projectName, aC.minorVersion) }
      val toPackages   = c.dependencies.map { cd =>
        Package(cd.projectName, cd.minorVersion)
      }.toSeq
      println(s"mainPackage: $mainPackage")
      println(s"fromPackages: $fromPackages")
      println(s"toPackages: $toPackages")
      PackageTree(mainPackage, fromPackages, toPackages)
    }

  private def treeForEachProject(packageTrees: Seq[PackageTree]) =
    val packageName = packageTrees.head.mainPackage.name
    val trees       = packageTrees.map(treeForEachVersion)
    s"""
       |# $packageName
       |${releaseConfig.releasedLabel}
       |
       |_**[API Documentation](../../$packageName/OpenApi.html)**_
       |
       |${trees
        .sortBy(_._1.versionNumber)
        .reverse
        .map { case pack -> treeAsStr =>
          s"""## ${pack.show}
             |
             |${printGraph(treeAsStr)}
             |""".stripMargin
        }
        .mkString}
       |$printColorLegend
       |""".stripMargin
  end treeForEachProject

  private def treeForEachVersion(packageTree: PackageTree) =
    val mainPackage = packageTree.mainPackage

    def subgraph(projectGroup: ProjectGroup) =
      s"""
         |    subgraph ${projectGroup.name}
         |    ${packageTree.allTrees.distinct
          .filter(p => apiConfig.projectsConfig.hasProjectGroup(p.name, projectGroup))
          .map { p =>
            p.showRect
          }
          .mkString(" & ")}
         |    end
         |    """.stripMargin

    mainPackage ->
      s"""
         |flowchart TB
         |${s"""
              |    ${packageTree.fromPackages.map(_.show).mkString(" & ")} ${
               if
                 packageTree.fromPackages.nonEmpty
               then "-->"
               else ""
             }
              |    ${mainPackage.show} ${
               if packageTree.toPackages.nonEmpty then "-->"
               else ""
             }
              |    ${packageTree.toPackages.map(_.show).mkString(" & ")}
              |""".stripMargin}
         |${apiConfig.projectGroups
          .map { group =>
            subgraph(group)
          }
          .mkString("\n")}
         |
         |$groupStyles
         |
         |${packageTree.allTrees.map { pT =>
          val color = colorMap.getOrElse(pT.name, "#fff")
          s"""
             |    click ${pT.show} href "./${pT.name}.html#${pT.show}" "${pT.name} Dependencies"
             |    style ${pT.show} fill:$color
             |""".stripMargin
        }.mkString}

         |""".stripMargin
  end treeForEachVersion

  private def getUniqueDependencies(
      toCheckConfig: DocProjectConfig,
      configs: Seq[DocProjectConfig]
  ) =
    val configsToCheck  = configs.filter { c =>
      toCheckConfig.dependencies.exists(_.projectName == c.projectName)

    }
    val filteredConfigs = toCheckConfig.dependencies.filterNot { dep =>
      configsToCheck
        .flatMap(_.dependencies)
        .distinct
        .exists(d => dep.projectName == d.projectName)
    }
    filteredConfigs
  end getUniqueDependencies

  private def groupStyles =
    apiConfig.projectGroups
      .map(pg =>
        s"   style ${pg.name} fill:${pg.fill},color:${pg.color},stroke:${pg.color};"
      )
      .mkString("\n") + "\n   linkStyle default stroke:#999,color:red;"

end DependencyGraphCreator
