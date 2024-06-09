package camundala.myCompany

import camundala.api.*

trait MyCompanyApiCreator extends ApiCreator, ApiDsl, CamundaPostmanApiCreator:

  override protected def apiConfig: ApiConfig = myCompanyConfig

end MyCompanyApiCreator
