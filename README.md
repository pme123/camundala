```
     _/_/_/                                                      _/            _/
  _/          _/_/_/  _/_/_/  _/_/    _/    _/  _/_/_/      _/_/_/    _/_/_/  _/    _/_/_/
 _/        _/    _/  _/    _/    _/  _/    _/  _/    _/  _/    _/  _/    _/  _/  _/    _/
_/        _/    _/  _/    _/    _/  _/    _/  _/    _/  _/    _/  _/    _/  _/  _/    _/
 _/_/_/    _/_/_/  _/    _/    _/    _/_/_/  _/    _/    _/_/_/    _/_/_/  _/    _/_/_/
```

[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

# Camundala — a new Way to develop Camunda BPMNs.
> Using the BPMN-Power of _**Camunda**_
and the Type safety of _**Scala**_ to develop BPMN Processes with a nice DSL.
## Why Camundala
* Domain Driven
* Typesafe
* Code Completion
* Composable on any Granularity
* Automatic Testing included
* 100% BPMN / Camunda compatible

I gave a talk at Camunda Camunda Summit 2022, check it out:
[Domain Driven Process Development](https://page.camunda.com/ccs2022-domaindrivenprocessdevelopment?hsLang=en)

## State of the Project
This project is now divided in two Github Project:
- https://github.com/pme123/camundala (this Repo)
  - Some features that are ready to be used in your Project.
- https://github.com/pme123/camundala-dsl
  - Testing new Features and Ideas.

So the first release is all about:
## Domain Driven Process Development
More infos will follow.

## Examples
In this project you find two examples that uses the Features.
- [Twitter Example](05-examples/twitter/README.md)
- [Invoice Example](05-examples/invoice/README.md)


# Development

## Update Dependencies

Use https://github.com/kitlangton/given

## Releasing
Just run `amm ./publish-release.sc VERSION`.

## Local publish

   `sbt publishLocal`

## Documentation
We use [mdoc](https://scalameta.org/mdoc/) to verify the documentation 
and [laika](https://typelevel.org/Laika/) to generate the htmls.

Run mdoc:   `sbt "docs/mdoc --watch"`
And laika:  `sbt "~docs/laikaPreview"`
Check the result in http://localhost:4242