# The Twitter example.
Camunda Spring Boot with Scala & ZIO.

Runs on port `9998`.
Camundala Services run on port `8888`

This is a copy from https://github.com/pme123/zio-camunda-spring-boot

The Project uses three apps provided by Camundala:
1. Camunda, a BPMN Engine that is started as a Spring Boot App.
1. The HTTP Server (http4s) to provide Services for the Camunda Modeler (deploy).
1. The Command Line Interface (CLI) - use `--help` in the console.

Run with Mill:

`mill -i examples.twitter.run`

`-i` is needed to run Mill in interactive mode.