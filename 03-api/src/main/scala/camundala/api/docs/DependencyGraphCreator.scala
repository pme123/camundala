package camundala.api
package docs

import os.{pwd, write}

case class DependencyGraphCreator()(using
    val apiConfig: ApiConfig,
    val configs: Seq[ApiProjectConf]
) extends DependencyCreator:

  def createIndex: String = {
    create(
      configs,
      pack =>
        s"""|   click ${pack.name} href "../${pack.name}/OpenApi.html" "${pack.name} API Documentation""""
    )
  }

  def createDependencies(using configs: Seq[ApiProjectConf]): String = {
    create(
      configs,
      pack =>
        s"""|   click ${pack.name} href "./dependencies/${pack.name}.html" "${pack.name} Dependencies""""
    )
  }

  def createProjectDependencies(using configs: Seq[ApiProjectConf]): Unit = {
    val graphsForProjects = treeForEachProjects(configs)
    graphsForProjects.foreach(g =>
      write.over(
        pwd / "src" / "docs" / "dependencies" / s"${g.name}.md",
        g.graph
      )
    )
  }

  private def create(
                      versionedConfigs: Seq[ApiProjectConf],
                      link: Package => String
  ): String = {
    val configs = versionedConfigs
      .groupBy(_.name)
      .map { case _ -> v => v.maxBy(_.version) }
      .toSeq

    def subgraph(projectGroup: ProjectGroup) =
      s"""
         |    subgraph ${projectGroup.name}
         |    ${configs
        .filter(p => apiConfig.projectsConfig.hasProjectGroup(p.name, projectGroup))
        .map { co =>
          s"${co.name}(${co.name})"
        }
        .mkString(" & ")}
         |    end
         |    """.stripMargin

    s"""
       |flowchart TB
       |${configs
      .map { config =>
        val color = colorMap.getOrElse(config.name, "#fff")
        val depConfig = getUniqueDependencies(config, configs)
        val tree =
          if (depConfig.nonEmpty)
            s"${config.name} --> ${depConfig
               .map(d => d.name)
               .mkString(" & ")}"
          else config.name
        s"""
       |   $tree
       |   ${link(Package(config.name, config.minorVersion))}
       |   style ${config.name} fill:$color
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
  }

  private def treeForEachProjects(
      configs: Seq[ApiProjectConf]
  ): Seq[ProjectTree] = {
    val groupedConfigs = configs.groupBy(_.name)
    groupedConfigs.map { case name -> gConfigs =>
      val trees = toPackageTree(gConfigs, configs)
      ProjectTree(name, treeForEachProject(trees))
    }.toSeq
  }

  case class PackageTree(
      mainPackage: Package,
      fromPackages: Seq[Package],
      toPackages: Seq[Package]
  ) {
    lazy val allTrees: Seq[Package] =
      mainPackage +: (fromPackages ++ toPackages)
  }

  case class ProjectTree(name: String, graph: String)

  private def toPackageTree(
                             configs: Seq[ApiProjectConf],
                             allConfigs: Seq[ApiProjectConf]
  ): Seq[PackageTree] = {
    configs.map { c =>
      val mainPackage = Package(c.name, c.minorVersion)
      val fromPackages = allConfigs
        .filter { aC =>
          aC.dependencies.exists(aD =>
            aD.name == mainPackage.name && aD.minorVersion == mainPackage.minorVersion
          )
        }
        .map { aC => Package(aC.name, aC.minorVersion) }
      val toPackages = c.dependencies.map { cd =>
        Package(cd.name, cd.minorVersion)
      }.toSeq
      println(s"mainPackage: $mainPackage")
      println(s"fromPackages: $fromPackages")
      println(s"toPackages: $toPackages")
      PackageTree(mainPackage, fromPackages, toPackages)
    }
  }

  private def treeForEachProject(packageTrees: Seq[PackageTree]) = {
    val packageName = packageTrees.head.mainPackage.name
    val trees = packageTrees.map(treeForEachVersion)
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
  }

  private def treeForEachVersion(packageTree: PackageTree) = {
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
         |    ${packageTree.fromPackages.map(_.show).mkString(" & ")} ${if (
                 packageTree.fromPackages.nonEmpty
               ) "-->"
               else ""}
         |    ${mainPackage.show} ${if (packageTree.toPackages.nonEmpty) "-->"
               else ""}
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
  }

  private def getUniqueDependencies(
                                     toCheckConfig: ApiProjectConf,
                                     configs: Seq[ApiProjectConf]
  ) = {
    val configsToCheck = configs.filter { c =>
      toCheckConfig.dependencies.exists(_.name == c.name)

    }
    val filteredConfigs = toCheckConfig.dependencies.filterNot { dep =>
      configsToCheck
        .flatMap(_.dependencies)
        .distinct
        .exists(d => dep.name == d.name)
    }
    filteredConfigs
  }

  private def groupStyles =
    apiConfig.projectGroups
      .map(pg =>
        s"   style ${pg.name} fill:${pg.fill},color:${pg.color},stroke:${pg.color};"
      )
      .mkString("\n") + "\n   linkStyle default stroke:#999,color:red;"

end DependencyGraphCreator
