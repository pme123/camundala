package camundala.api

/** the idea is use Camundala to create Company's Process documentation.
  *
  * For a Start you can create a Catalog.
  */
trait CompanyDocCreator:
  def apiConfig: ApiConfig

  def createCatalog(): Unit =
    val projectPaths = apiConfig.gitConfigs.init
    val catalogs = s"""{%
                      |// auto generated - do not change!
                      |helium.site.pageNavigation.depth = 1
                      |%}
                      |## Catalog
                      |${projectPaths
      .map { case projectName -> _ =>
        val path = apiConfig.gitConfigs.gitDir / projectName / catalogFileName
        if (os.exists(path))
          os.read(path)
        else
          s"""### $projectName
                      |Sorry there is no $path.
                      |""".stripMargin
      }
      .mkString("\n")}""".stripMargin
    val catalogPath = apiConfig.basePath / "src" / "docs" / catalogFileName
    if (os.exists(catalogPath))
      os.write.over(catalogPath, catalogs)
    else
      os.write(catalogPath, catalogs, createFolders = true)

end CompanyDocCreator
