# sbt

The _sbt_ files are structured as follows:

```bash
myCompany-camundala
            |  project
            |    |  build.properties
            |    |  plugins.sbt
            |    |  ProjectDef.scala
            |    |  Settings.scala
            |  build.sbt

```

## build.properties
Contains _only_ the sbt version. 
(updated automatically)

## plugins.sbt
Defines the plugins used in the project.
(updated automatically)

## ProjectDef.scala
Defines the project naming (_organisation, name, version_).
(updated automatically)

You should **not adjust** this file, as in is used in the development process.

## Settings.scala
Contains the settings for the build project.
(updated automatically)

## build.sbt
Top level of the build definition. Make your adjustments here.

Check it for the _**TODOs**_ and adjust them to your needs.

At the moment you need at least define the _repository_ settings.

