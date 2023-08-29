# Technologies

Our goal is to keep the dependencies to a minimum.
We group them to our Modules.
The following Open Source Projects are great and are worth the extra dependency:

## camundala-domain
### Tapir
_With [tapir](https://tapir.softwaremill.com/en/latest/), you can describe HTTP API endpoints as immutable Scala values._ 

We are using this to describe our domain models. 
Tapir allows us to generate the Open API specification from the models.

### Circe
_[circe](https://circe.github.io/circe/) (pronounced SUR-see, or KEER-kee in classical Greek, or CHEER-chay in Ecclesiastical Latin) is a JSON library for Scala (and Scala.js)._

We use Circe to encode our domain models to JSON and decode them back.
This is used by Tapir to generate the specifications and the Simulations to run 
the REST calls.

## camundala-bpmn
Depends on: _camundala-domain_
### OS-Lib

_[OS-Lib](https://github.com/com-lihaoyi/os-lib) is a simple Scala interface to common OS filesystem and subprocess APIs. 
OS-Lib aims to make working with files and processes in Scala as simple as any scripting language, while still providing the safety, 
flexibility and performance you would expect from Scala._

## camundala-api
Depends on: _camundala-bpmn_

### scala-xml
_The standard [Scala XML](https://github.com/scala/scala-xml) library._ 

We use it for the reference resolutions in the BPMNs.

### Typesafe Config
_[Configuration library](https://github.com/lightbend/config) for JVM languages._

For all configurations, that are not directly in Scala, we use this library. 
At the moment this is _PROJECT.conf_ (Project Documentation) and _RELEASE.conf_ (Company Documentation).

## camundala-simulation
Depends on: _camundala-bpmn_

### sttp Client
_[sttp client](https://sttp.softwaremill.com/en/stable/) is an open-source library which provides a clean, programmer-friendly API to describe HTTP requests and how to handle responses._

We use it to call the REST API from Camunda

## camundala-dmn
Depends on: _camundala-bpmn_

### Camunda DMN Tester
_A little [DMN Table tester](https://github.com/camunda-community-hub/camunda-dmn-tester) with the following Goals:_

- _As a developer I want to test the DMNs that I get from the Business, even not knowing the concrete rules._
- _Business people can create their own tests._
- _They can easily adjust the tests to the dynamic nature of DMN Tables._

We generate Dmn Table Tester configurations from a DSL.

### sttp Client
_see camundala-simulation_

To use the DmnTester's REST API.

