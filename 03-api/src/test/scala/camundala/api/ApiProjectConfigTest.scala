package camundala.api

import munit.FunSuite

class ApiProjectConfigTest extends FunSuite:
  test("ApiProjectConfig"):
    val apiProjectConfig = ApiProjectConfig(
      os.pwd / "03-api" / "src" / "test" / defaultProjectConfigPath
    )
    assertEquals(
      apiProjectConfig,
      ApiProjectConfig(
        "mycompany-myProject",
        VersionConfig("1.0.0-SNAPSHOT"),
        Seq("subProject1", "subProject2"),
        Seq(
          DependencyConfig("mastercompany-services", "1.2.4"),
          DependencyConfig("mycompany-commons", "1.0.3")
        )
      )
    )

end ApiProjectConfigTest
