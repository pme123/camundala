package camundala.helper.util

case class PublishConfig(
    documentationUrl: String,
    documentationEnvUsername: String = "DOCUMENTATION_USERNAME",
    documentationEnvPassword: String = "DOCUMENTATION_PASSWORD",
    openApiHtmlPath: os.ResourcePath = os.resource / "OpenApi.html",
)
