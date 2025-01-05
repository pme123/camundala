{%
helium.site.pageNavigation.enabled = false
%}

# Introduction

A Company Project handles the specific configuration for the company.

@:callout(info)
**Be aware** that this is not tested.

Make sure you have [init the company](../development/initCompany.md).

In this process many files are generated and some are replaced.

Only files with the comment `DO NOT ADJUST` will be replaced.
So if you add custom code, make sure to remove this comment.

We recommend you make a comment what you have changed, 
so you can easily update the file from time to time.
(just add the `DO NOT ADJUST` comment again)

If there is no `DO NOT ADJUST` comment, you need to delete it if you want the newest version of this file.

Normally these files have the comment: `// This file was created with .. - to reset delete it and run the command.`.
@:@

The layout looks similar to a BPMN project:
```bash
myCompany-camundala
            |  00-docs
            |  02-bpmn
            |  03-api
            |  03-dmn
            |  03-simulation
            |  03-worker
            |  04-helper
            |  project
            |  build.sbt
            |  helper.scala

```

To setup the Company Project, follow these steps:

1. Open the `mycompany-camundala` directory with your IDE (I use Intellij).
1. Import the sbt project. The project should compile without errors.
1. **[sbt]**

   The build tool (`project`, `build.sbt`).

1. **[00-docs]**

   The company's documentation.
1. **[02-bpmn]**

   General configurations and code for the _BPMN DSL_.
1. **[03-api]**

   General configurations and code for the _Api DSL_.
1. **[03-dmn]**

   General configurations and code for the _DmnTester DSL_.
1. **[03-simulation]**

   General configurations and code for the _Simulation DSL_.
1. **[03-worker]**

   General configurations and code for the _Worker DSL_.
1. **[04-helper]**

   General configurations and code for the development process of the projects, 
   Including the Company project.

1. **[Development]**

   The `helper.scala` script supports the development process.
