package camundala.myCompany

import camundala.api.*
import camundala.api.docs.CompanyDocCreator
import camundala.api.docs.ReleaseConfig

/*
Starting point to use Camundala for Company wide documentation
For now only the catalog is created.

*/
object MyCompanyDocCreator extends CompanyDocCreator, App:
  implicit lazy val apiConfig: ApiConfig = myCompanyConfig

  prepareDocs()
end MyCompanyDocCreator
