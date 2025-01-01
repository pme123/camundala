# Postman
We use postman to test the APIs and deploy our projects.

## Test the APIs

In the process of publishing the APIs, we also create a Postman Collection.

You find the Postman Collection in the `projectBaseDir/03-api/PostmanOpenApi.yml` directory of the project.

## Deploy the Projects

This is used when you deploy the projects to the camunda Engine.

You need a folder in a Postman Collection. The folder name is `deploy_manifest`.

This folder will be run from top to bottom. So you need to:
- authenticate to the [Camunda REST API](https://docs.camunda.org/rest/camunda-bpm-platform/7.22/#tag/Deployment)
- configure the deployment for the local environment
- call the Camunda REST API to deploy the project
- test the deployment with another call to the API.

You need to provide the following configuration:

```scala
case class PostmanConfig(
    collectionId: String,
    localDevEnvId: String,
    envApiKey: String = "POSTMAN_API_KEY"
)
```