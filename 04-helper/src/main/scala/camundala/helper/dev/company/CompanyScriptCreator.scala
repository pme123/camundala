package camundala.helper.dev.company

import camundala.api.VersionHelper
import camundala.helper.util.DevConfig

case class CompanyScriptCreator()(using config: DevConfig):

  lazy val companyHelper =
    s"""#!/usr/bin/env -S scala shebang
       |$helperCompanyDoNotAdjustText
       |
       |//> using toolkit 0.7.0
       |//> using dep io.github.pme123::camundala-helper:${VersionHelper.camundalaVersion}
       |
       |import camundala.helper.dev.DevCompanyHelper
       |
       |   @main
       |   def run(command: String, arguments: String*): Unit =
       |     DevCompanyHelper.run(command, arguments*)
       |""".stripMargin

  lazy val companyCamundalaHelper =
    s"""#!/usr/bin/env -S scala shebang
       |$helperCompanyDoNotAdjustText
       |
       |//> using dep ${config.companyName}::${config.companyName}-camundala-helper:${config.versionConfig.companyCamundalaVersion}
       |
       |import ${config.companyName}.camundala.helper.*
       |
       |@main
       |def run(command: String, args: String*): Unit =
       |  CompanyCamundalaDevHelper.runForCompany(command, args*)
       |""".stripMargin

end CompanyScriptCreator
