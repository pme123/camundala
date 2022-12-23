# Project Setup
**_Camundala_** is written in _**Scala**_ and provides _Scala DSLs_.
So far we only use it as a _sbt_ projects. 
Here is a video to get you started with a Scala dev environment: 
[Setting up a dev environment with Coursier](https://www.youtube.com/watch?v=j-H6LSv2z_8&list=PLTx-VKTe8yLxYQfX_eGHCxaTuWvvG28Ml). 

To get started please take the [example project](https://github.com/pme123/camundala-example). 
The example includes a Camunda Spring Boot setup. 

For a Camunda 8 example (no Simulations yet) check this project under examples. Be aware that the
_Camunda 8_ examples need either a _Camunda_ Cloud subscription or a standalone
installation - I use _Camunda_'s docker images.

As these example are in a complexer multi-module project, we show here and explain each part.

## File structure
```javascript
src // uses maven src setup
    / main / scala 
                   / yourproject.package 
                                         / domain.scala // put your domain here
                                         / bpmn.scala   // put your bpmn here
                                         / api.scala    // put your api here
                                         / otherStuff   // e.g. services etc.
    / it / scala
                 / yourproject.package
                                        / InvoiceSimulation.scala // put your simulations here
project 
        / build.properties // version of sbt - see below
build.sbt // dependencies etc. - see below

```

### build.sbt
Here you define the dependencies.
```scala
val camundalaVersion = "0.12.0"
lazy val camundalaDependencies = Seq(
  "io.github.pme123" %% "camundala-api" % camundalaVersion,
  "io.github.pme123" %% "camundala-simulation" % camundalaVersion % IntegrationTest
)
```
Here we want to do API Documentation and Simulations. 

