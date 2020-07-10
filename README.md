# Camundala

Doing Camunda with Scala.

# WORK IN PROGRESS / EXPERIMENTAL

# Key Focus

* **Separate Business Modelling from technical implementation.**
  * The Business Model contains only the business relevant stuff - no mappings etc.
  * The Implementation Model is generated from the Business Model and the technical Implementation - this is the Scala part.

* **BPMN Engineer centered.**
  * This is all about productivity.
  
* **Provide a Type-Safe way to model a BPMN Process.**
  * Let the compiler help you to do your BPMN right.
  
* **Use whenever possible Scala as the Type-Safe language.**
  * Technical BPMNs are build with _Scala Case Classes_.
  * Refined Types - see
  * Scripting is done with _Scala Script_.
  
* **100 % Camunda compatible**
  * Deployed Models run on any Camunda Engine - no Extras needed.

# Feature List
## Mapping
See [doc/mapping](doc/mapping.MD)
## CLI
**Feature** | **Descr** | **State**
--- | --- | --- 
deploy create | Deploy a project according to your Deploy definition. It uses the Camunda REST API. | ready
 TODO |
  
# Requirements

## Buildtool Mill
This Project uses [Mill](https://github.com/lihaoyi/mill), the most fun build tool I know.

In **Visual Studio Code** mill works with the Metals Plugin without any adjustments.

### Update dependencies in Intellij

    mill mill.scalalib.GenIdea/idea
    
# Examples
This project is driven by examples. 
See [./examples/README.m](./examples/README.md)
