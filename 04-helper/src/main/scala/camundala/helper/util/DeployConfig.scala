package camundala.helper.util

case class DeployConfig(
    postmanCollectionId: String,
    postmanLocalDevEnvId: String,
    postmanEnvApiKey: String = "POSTMAN_API_KEY"
)
