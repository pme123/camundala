package camundala.api
package docs

import os.{pwd, write}

case class DependencyGraphCreator()(implicit
    val apiConfig: ApiConfig,
    val configs: Seq[PackageConf],
    val releaseConfig: ReleaseConfig
) extends DependencyCreator {

  def createIndex: String = {
    create(
      configs,
      pack =>
        s"""|   click ${pack.name} href "../${pack.name}/OpenApi.html" "${pack.name} API Documentation""""
    )
  }

  def createDependencies(implicit configs: Seq[PackageConf]): String = {
    create(
      configs,
      pack =>
        s"""|   click ${pack.name} href "./dependencies/${pack.name}.html" "${pack.name} Dependencies""""
    )
  }

  def createProjectDependencies(implicit configs: Seq[PackageConf]): Unit = {
    val graphsForProjects = treeForEachProjects(configs)
    graphsForProjects.foreach(g =>
      write.over(
        pwd / "src" / "docs" / "dependencies" / s"${g.name}.md",
        g.graph
      )
    )
  }

  private def create(
      versionedConfigs: Seq[PackageConf],
      link: Package => String
  ): String = {
    val configs = versionedConfigs
      .groupBy(_.name)
      .map { case _ -> v => v.maxBy(_.version) }
      .toSeq

    def subgraph(projectGroup: ProjectGroup) =
      s"""
         |    subgraph ${projectGroup.name}}
         |    ${configs
        .filter(p => apiConfig.gitConfigs.hasProjectGroup(p.name, projectGroup))
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
          else ""
        s"""
       |   $tree
       |   ${link(Package(config.name, config.minorVersion))}
       |   style ${config.name} fill:$color
       |""".stripMargin
      }
      .mkString("\n")}
       |${releaseConfig.projectGroups.map { group =>
      subgraph(group)
    }}
       |""".stripMargin
  }

  private def treeForEachProjects(
      configs: Seq[PackageConf]
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
      configs: Seq[PackageConf],
      allConfigs: Seq[PackageConf]
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
      }
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
        .filter(p => apiConfig.gitConfigs.hasProjectGroup(p.name, projectGroup))
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
         |${releaseConfig.projectGroups.map { group =>
             subgraph(group)
             }.mkString("\n")
         }
         |
         |$styles
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
      toCheckConfig: PackageConf,
      configs: Seq[PackageConf]
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

  private def styles =
    """
      |   style valiant-projects fill:#ddd,color:purple,stroke:purple;
      |   style valiant-general fill:#ddd,color:purple,stroke:purple;
      |   style valiant-general-projects fill:#ddd,color:purple,stroke:purple;
      |   style finnova fill:#ddd,color:green,stroke:green;
      |   linkStyle default stroke:#999,color:red;
      |""".stripMargin

}
