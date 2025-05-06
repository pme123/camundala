package camundala.examples.demos.newWorker

import camundala.worker.c7zio.OAuth2Client

object CompanyOAuth2Client extends OAuth2Client:

  lazy val fssoRealm: String = sys.env.getOrElse("FSSO_REALM", "0949")
  lazy val fssoBaseUrl       = sys.env.getOrElse("FSSO_BASE_URL", s"http://host.lima.internal:8090/auth")

  override lazy val client_id     = sys.env.getOrElse("FSSO_CLIENT_NAME", "bpf")
  override lazy val client_secret =
    sys.env.getOrElse("FSSO_CLIENT_SECRET", "6ec0e8ce-eff1-456f-bc2f-907b6fcb5157")
  override lazy val scope         = sys.env.getOrElse("FSSO_SCOPE", "bpf fcs")
  override lazy val username      = sys.env.getOrElse("FSSO_TECHUSER_NAME", "admin")
  override lazy val password      = sys.env.getOrElse("FSSO_TECHUSER_PASSWORD", "admin")

end CompanyOAuth2Client
