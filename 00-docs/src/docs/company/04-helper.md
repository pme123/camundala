# 04-helper

### CompanyDevHelper
With the `CompanyDevHelper` you can customize  _Camundala_ for your Company:

```scala mdoc
import camundala.api.*
import camundala.helper.dev.DevHelper
import camundala.helper.util.*

case class CompanyDevHelper(projectName: String, subProjects: Seq[String] = Seq.empty)
    extends DevHelper:

  lazy val apiConfig: ApiConfig = ApiConfig("mycompany")//.withTenantId("mycompany")...

  lazy val deployConfig: Option[DeployConfig] = ???
  lazy val devConfig: DevConfig = ???
  lazy val dockerConfig: DockerConfig = ???
  lazy val publishConfig: Option[PublishConfig] = ???

```
