package camundala
package api

import camundala.bpmn.diagramPath
import sttp.apispec.openapi.Contact

case class ApiConfig(
    // your company name like 'mycompany'
    companyName: String,
    // define tenant if you have one - used for the Postman OpenApi
    tenantId: Option[String] = None,
    // contact email / phone, if there are questions
    contact: Option[Contact] = None,
    // REST endpoint (for testing API)
    endpoint: String = "http://localhost:8080/engine-rest",
    // Base Path of your project (if changed - all doc paths will be adjusted)
    basePath: os.Path = os.pwd,
    // If you work with JIRA, you can add matchers that will create automatically URLs to JIRA Tasks
    jiraUrls: Map[String, String] = Map.empty,
    // Configure your project setup
    projectsConfig: ProjectsConfig = ProjectsConfig(),
    // Configure your template generation
    modelerTemplateConfig: ModelerTemplateConfig = ModelerTemplateConfig(),
    // The URL of your published documentations
    // s"http://myCompany/bpmnDocs"
    docBaseUrl: Option[String] = None,
    // Path, where the Git Projects are cloned - for dependency check.
    // the default is for the structure: dev-myCompany/projects/myProject
    tempGitDir: os.Path = os.pwd / os.up / os.up / "git-temp"
):
  val catalogPath: os.Path = basePath / catalogFileName

  lazy val openApiPath: os.Path            = basePath / ApiConfig.openApiPath
  lazy val postmanOpenApiPath: os.Path     = basePath / ApiConfig.postmanOpenApiPath
  lazy val openApiDocuPath: os.Path        = basePath / ApiConfig.openApiHtmlPath
  lazy val postmanOpenApiDocuPath: os.Path = basePath / ApiConfig.postmanOpenApiHtmlPath

  lazy val projectGroups: Seq[ProjectGroup] = projectsConfig.projectConfigs
    .map(_.group)
    .distinct

  lazy val init: Unit = projectsConfig.init(tempGitDir)
  
  def withTenantId(tenantId: String): ApiConfig =
    copy(tenantId = Some(tenantId))

  def withBasePath(path: os.Path): ApiConfig =
    copy(
      basePath = path
    )

  def withEndpoint(ep: String): ApiConfig =
    copy(endpoint = ep)

  def withPort(port: Int): ApiConfig =
    copy(endpoint = s"http://localhost:$port/engine-rest")

  def withDocBaseUrl(url: String): ApiConfig =
    copy(docBaseUrl = Some(url))

  def withProjectsConfig(gitConfigs: ProjectsConfig): ApiConfig =
    copy(projectsConfig = gitConfigs)

  def withModelerTemplateConfig(modelerTemplateConfig: ModelerTemplateConfig): ApiConfig =
    copy(modelerTemplateConfig = modelerTemplateConfig)

  def addGitConfig(gitConfig: ProjectsPerGitRepoConfig): ApiConfig =
    copy(projectsConfig =
      projectsConfig.copy(perGitRepoConfigs = projectsConfig.perGitRepoConfigs :+ gitConfig)
    )

  def withJiraUrls(urls: (String, String)*): ApiConfig =
    copy(jiraUrls = urls.toMap)

  def addJiraUrl(jiraTag: String, url: String): ApiConfig =
    copy(jiraUrls = jiraUrls + (jiraTag -> url))

  def withContact(contact: Contact): ApiConfig =
    copy(contact = Some(contact))

  def refIdentShort(refIdent: String): String =
    projectsConfig.refIdentShort(refIdent, companyName)

  def refIdentShort(refIdent: String, projectName: String): String =
    projectsConfig.refIdentShort(refIdent, companyName, projectName)

  lazy val projectConfPath: os.Path = basePath / projectsConfig.projectConfPath
end ApiConfig
object ApiConfig:
  lazy val openApiPath: os.RelPath            = os.rel / "03-api" / "OpenApi.yml"
  lazy val postmanOpenApiPath: os.RelPath     = os.rel / "03-api" / "PostmanOpenApi.yml"
  lazy val openApiHtmlPath: os.RelPath        = os.rel / "03-api" / "OpenApi.html"
  lazy val postmanOpenApiHtmlPath: os.RelPath = os.rel / "03-api" / "PostmanOpenApi.html"
end ApiConfig

case class ProjectsConfig(
                           // Path to your ApiProjectConf - default is os.pwd / PROJECT.conf
                           projectConfPath: os.RelPath = defaultProjectConfigPath,
                           // grouped configs per GitRepos - so it is possible to use projects from different Repos
                           perGitRepoConfigs: Seq[ProjectsPerGitRepoConfig] = Seq.empty
):

  lazy val isConfigured: Boolean = perGitRepoConfigs.nonEmpty

  def withProjectConfPath(path: os.RelPath): ProjectsConfig =
    copy(projectConfPath = path)

  def projectCloneUrl(projectName: String): Option[String] =
    perGitRepoConfigs
      .find(_.containsProject(projectName))
      .map(_.cloneBaseUrl)

  def init(tempGitDir: os.Path): Unit =
    perGitRepoConfigs.foreach(_.init(tempGitDir))
  end init

  def initProject(projectName: String, tempGitDir: os.Path): Unit =
    perGitRepoConfigs.foreach(_.initProject(tempGitDir, projectName))
  end initProject

  def projectConfig(projectName: String): Option[ProjectConfig] =
    projectConfigs.find(_.name == projectName)

  lazy val projectConfigs: Seq[ProjectConfig] = perGitRepoConfigs.flatMap(_.projects)

  lazy val colors: Seq[(String, String)] = projectConfigs.map { project =>
    project.name -> project.color
  }

  def hasProjectGroup(
      projectName: String,
      projectGroup: ProjectGroup
  ): Boolean =
    perGitRepoConfigs
      .flatMap(_.projects)
      .find(_.name == projectName)
      .exists(_.group == projectGroup)

  def refIdentShort(refIdent: String, companyId: String, projectName: String): String =
    refIdent
      .replace(s"$companyId-", "") // mycompany-myproject-myprocess -> myproject-myprocess
      .replace(
        s"${projectName.replace(s"$companyId-", "")}-",
        ""
      ) // myproject-myprocess -> myprocess
  end refIdentShort

  // if projectName is not known
  def refIdentShort(refIdent: String, companyId: String): String =
    val projectNames = projectConfigs.map(pc => pc.name)

    projectNames.find(refIdent.startsWith)
      .map(pn =>
        refIdent.replace(s"$pn-", "")  // case myCompany-myProject-myProcess
          .replace(s"$companyId-", "") // case myCompany-myProject > where no myProcess
      )
      .orElse(          // case myProject-myProcess
        projectNames.map(_.replace(s"$companyId-", ""))
          .find(refIdent.startsWith)
          .map(pn =>
            refIdent.replace(s"$pn-", "")
          )).getOrElse( // or any other process
        refIdent)
  end refIdentShort

end ProjectsConfig

case class ProjectsPerGitRepoConfig(
    // Base URL for the Git Repos
    // The pattern must be $cloneBaseUrl/$projectName.git
    cloneBaseUrl: String,
    // Definition of the projects
    projects: Seq[ProjectConfig]
):

  def init(gitDir: os.Path): Unit =
    projects.foreach: project =>
      val gitRepo = s"$cloneBaseUrl/${project.name}.git"
      updateProject(project.absGitPath(gitDir), gitRepo)

  def initProject(gitDir: os.Path, projectName: String): Unit =
    projects.find(_.name == projectName)
      .foreach { project =>
        val gitRepo = s"$cloneBaseUrl/${project.name}.git"
        updateProject(project.absGitPath(gitDir), gitRepo)
      }
  end initProject

  def containsProject(projectName: String): Boolean =
    projects.exists(_.name == projectName)

  private def updateProject(gitProjectDir: os.Path, gitRepo: String): Unit =
    println(s"Git Project Dir: $gitProjectDir")
    println(s"Git Repo: $gitRepo")
    os.makeDir.all(gitProjectDir)
    if !(gitProjectDir / ".gitignore").toIO.exists() then
      os.proc("git", "clone", gitRepo, gitProjectDir)
        .callOnConsole(gitProjectDir)
    else
      os
        .proc("git", "checkout", "develop")
        .callOnConsole(gitProjectDir)
      os.proc("git", "pull", "origin", "develop")
        .callOnConsole(gitProjectDir)
    end if
  end updateProject
end ProjectsPerGitRepoConfig

case class ProjectConfig(
    // Name of the project
    name: String,
    // you can group your projects - for better overview
    group: ProjectGroup,
    // the color of your project - for better overview and visualization in the BPMN diagrams
    color: String = "#fff"
):
  def absGitPath(gitDir: os.Path): os.Path  = gitDir / name
  def absBpmnPath(gitDir: os.Path): os.Path = absGitPath(gitDir) / diagramPath
end ProjectConfig

case class ProjectGroup(
    name: String,
    // line color
    color: String = "purple",
    fill: String = "#ddd"
)

case class ModelerTemplateConfig(
    schemaVersion: String = "0.16.0",
    templateRelativePath: os.RelPath = os.rel / ".camunda" / "element-templates",
    generateGeneralVariables: Boolean = true
):
  lazy val templatePath: os.Path = os.pwd / templateRelativePath

  lazy val schema =
    s"https://unpkg.com/@camunda/element-templates-json-schema@$schemaVersion/resources/schema.json"
end ModelerTemplateConfig
