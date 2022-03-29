# Tweet directly from the Tasklist - Spring Boot packaged
I did the following Adjustments:
* Changed it to _Scala_ to simplify my example.
* Added extensions to the Camundala DSL.
* Added the BPMN implementation in Camundala DSL.

There are 2 application here:
1. **TwitterProcessRunnerApp**
   
   Runs the _Camundala_ Dev Process (from specification BPMN to implementation BPMN).

2. **TwitterServletProcessApplication**

   Runs the Camunda Engine (Spring Boot) with the generated BPMN. 

## How to run it
> **this needs [sbt](https://www.scala-sbt.org)**

`sbt exampleTwitter/run`

Now you can select 1 or 2:
```
Multiple main classes detected. Select one to run:
 [1] camundala.examples.twitter.TwitterServletProcessApplication
 [2] camundala.examples.twitter.bpmn.TwitterProcessRunnerApp
 ```
This is the [original README](https://github.com/camunda/camunda-bpm-examples/tree/master/spring-boot-starter/example-twitter)

I bundled it here! Just run `InvoiceServletProcessApplication`.

This based on this Project: https://github.com/pme123/spring-boot-datakurre-plugins

