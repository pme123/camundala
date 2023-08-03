package camundala.api
package docs

trait DependencyCreator :

  protected implicit def apiConfig: ApiConfig
  protected implicit def configs: Seq[PackageConf]
  protected implicit def releaseConfig: ReleaseConfig = readReleaseConfig
  

  case class Package(name: String, minorVersion: String) {
    lazy val show = s"$name:$minorVersion"
    lazy val showRect = s"$name:$minorVersion($name:$minorVersion)"
    lazy val versionNumber = minorVersion.replace(".", "").toInt
  }

  object Package {
    def apply(conf: PackageConf): Package =
      Package(conf.name, conf.minorVersion)
  }

  lazy val colors = apiConfig.gitConfigs.colors
  lazy val colorMap = colors.toMap

  protected def printGraph(dependencyGraph: String): String =
    s"""
      |<div>
      |      <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
      |
      |      <script>
      |        mermaid.initialize(
      |        {startOnLoad: true}
      |        );
      |      </script>
      |<div class="mermaid">
      |%%{init: {"flowchart": {"htmlLabels": true, "curve": "cardinal", "useMaxWidth": true, "rankSpacing": 70}}
      |}%%
      |
      |$dependencyGraph
      |
      |      </div>
      |</div>
      |""".stripMargin

  protected lazy val printColorLegend: String =
    if(colors.nonEmpty)
      s"""
        |<div class="colorLegend">
        |  <p>Color Legend:</p>
        |  <ul>
        |${
        colors
        .filterNot(c => c._2 == "white")
          .map(c =>
            s"""<li style="background: ${c._2};">${c._1}: ${c._2}</li>""".stripMargin
          ).mkString("\n")
      }
        |  </ul>
        |</div>
        |""".stripMargin
    else
      ""    
  end printColorLegend 

  protected def readReleaseConfig: ReleaseConfig =
    ReleaseConfig.releaseConfig

end DependencyCreator  

