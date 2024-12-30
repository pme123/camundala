package camundala.helper.util

case class PostmanConfig(
    collectionId: String,
    localDevEnvId: String,
    envApiKey: String = "POSTMAN_API_KEY"
)
