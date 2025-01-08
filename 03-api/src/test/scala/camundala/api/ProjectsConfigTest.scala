package camundala.api

import munit.FunSuite

class ProjectsConfigTest extends FunSuite:

  test("colorForId should return the correct color for a given refName and ownProjectName"):
    val projectConfig1 = ProjectConfig("project1", ProjectGroup("group1"), "#ff0000")
    val projectConfig2 = ProjectConfig("project2", ProjectGroup("group2"), "#00ff00")
    val projectsConfig = ProjectsConfig(perGitRepoConfigs =
      Seq(
        ProjectsPerGitRepoConfig("https://example.com", Seq(projectConfig1, projectConfig2))
      )
    )

    val result = projectsConfig.colorForId("project1-task", "project2")
    assertEquals(result, Some("project1" -> "#ff0000"))

  test("colorForId should return None if no matching project is found"):
    val projectConfig1 = ProjectConfig("project1", ProjectGroup("group1"), "#ff0000")
    val projectsConfig = ProjectsConfig(perGitRepoConfigs =
      Seq(
        ProjectsPerGitRepoConfig("https://example.com", Seq(projectConfig1))
      )
    )
    val result         = projectsConfig.colorForId("unknown-task", "project1")
    assertEquals(result, None)

  test("colorForId should return None if refName starts with ownProjectName"):
    val projectConfig1 = ProjectConfig("project1", ProjectGroup("group1"), "#ff0000")
    val projectsConfig = ProjectsConfig(perGitRepoConfigs =
      Seq(
        ProjectsPerGitRepoConfig("https://example.com", Seq(projectConfig1))
      )
    )
    val result         = projectsConfig.colorForId("project1-task", "project1")
    assertEquals(result, None)

  // projectNameForRef
  val projectConfig  = ProjectConfig("myProject", ProjectGroup("group1"))
  val projectsConfig = ProjectsConfig(perGitRepoConfigs =
    Seq(ProjectsPerGitRepoConfig("http://example.com", Seq(projectConfig)))
  )
  test(
    "projectNameForRef should return the project name if the processRef starts with a project name"
  ):
    val result = projectsConfig.projectNameForRef("myProject-myProcess")
    assertEquals(result, "myProject")
  test("projectNameForRef should return the processRef if no project name matches"):
    val result = projectsConfig.projectNameForRef("unknownProject-myProcess")
    assertEquals(result, "NO PROJECT FOUND")
end ProjectsConfigTest
