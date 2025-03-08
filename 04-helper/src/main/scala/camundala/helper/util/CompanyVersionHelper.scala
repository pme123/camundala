package camundala.helper.util

import camundala.api.{ApiProjectConfig, DependencyConfig, VersionHelper}

import scala.jdk.CollectionConverters.*

case class CompanyVersionHelper(
    companyName: String
):

  lazy val companyCamundalaVersion: String =
    VersionHelper.repoSearch(s"$companyName-camundala-bpmn_3", companyName)

end CompanyVersionHelper
