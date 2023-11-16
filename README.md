```
     _/_/_/                                                      _/            _/
  _/          _/_/_/  _/_/_/  _/_/    _/    _/  _/_/_/      _/_/_/    _/_/_/  _/    _/_/_/
 _/        _/    _/  _/    _/    _/  _/    _/  _/    _/  _/    _/  _/    _/  _/  _/    _/
_/        _/    _/  _/    _/    _/  _/    _/  _/    _/  _/    _/  _/    _/  _/  _/    _/
 _/_/_/    _/_/_/  _/    _/    _/    _/_/_/  _/    _/    _/_/_/    _/_/_/  _/    _/_/_/
```
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

## Releasing
Just run `amm ./publish-release.sc VERSION`.

## Local publish

   `sbt publishLocal`

