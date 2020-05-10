# Camundala

Doing Camunda with Scala.

# WORK IN PROGRESS / EXPERIMANTAL

# Key Concepts

* **Separate Business Modelling from technical implementation.**
  * The Business Model contains only the business relevant stuff - no mappings etc.
  * The Implementation Model is generated from the Business Model and the technical Implementation - this is the Scala part.

## Buildtool Mill
This Project uses [Mill](https://github.com/lihaoyi/mill), the most fun build tool I know.

In **Visual Studio Code** mill works with the Metals Plugin without any adjustments.

### Update dependencies in Intellij

    mill mill.scalalib.GenIdea/idea
    
# Examples
This project is driven by examples. 
See [./examples/README.m](./examples/README.md)