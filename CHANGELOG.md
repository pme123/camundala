# Changelog

All notable changes to this project will be documented in this file.

* Types of Changes (L3):
  * Added: new features
  * Changed: changes in existing functionality
  * Deprecated: soon-to-be-removed features
  * Removed: now removed features
  * Fixed: any bug fixes
  * Security: in case of vulnerabilities


The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## 1.30.31 - 2025-01-20
### Changed 
- Made the taskDefinitionKey of a UserTask required to be the same as in the BPMN - new in API doc and Simulation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/dfd801def32646c3a748464c309594c1cfe439ed)
- Updated Versions. - see [Commit](https://pme123@github.com/pme123/camundala/commit/c85dd2ba69698edd4c4360f9d2537b7553a07414)
- Working Worker C7 example. - see [Commit](https://pme123@github.com/pme123/camundala/commit/4584d6868a5861780d2ec3f605b7e6398dc9a5cb)
- Working Worker C8 example. - see [Commit](https://pme123@github.com/pme123/camundala/commit/3af0336ded2ae41fdf807d35da54d5b2e46cd68b)
- Worker C8 state of work. - see [Commit](https://pme123@github.com/pme123/camundala/commit/0ef0d755e5137e564487d1cbe2a3d715f38bcf2d)
- Fixed check previous Version check - too many changelog entries. - see [Commit](https://pme123@github.com/pme123/camundala/commit/751432cf6694f5a950edb77203a3ea0dc298308a)
- Fixed missing dependencies in the dependency table. - see [Commit](https://pme123@github.com/pme123/camundala/commit/1be2d62cf12f141330194772f3451d7f6a38bf43)
- Adjustments file generation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/a4781a792971893b6f478ec876b3a2f9cd28ca98)
- Added codec for IntOrBoolean. - see [Commit](https://pme123@github.com/pme123/camundala/commit/f8051a16f0d14cf4ffc4aeef15e706e8dbf995a3)
- First version with Worker for Camunda8. - see [Commit](https://pme123@github.com/pme123/camundala/commit/18b73324573eff2e42d755f417ec0d45a57c3036)
- Added Circe Classes to exports.scala. - see [Commit](https://pme123@github.com/pme123/camundala/commit/ebecd6242e4691704d3a9609609f4b874fa57a11)
- Fixes in project update / added CompanyValidationWorker. - see [Commit](https://pme123@github.com/pme123/camundala/commit/f38eeb69f1944aec84839b1df2012290a5ba1359)
- Fixing only.badScenarios / only.incidentScenarios. - see [Commit](https://pme123@github.com/pme123/camundala/commit/1899a565231b3f52463f67b3284001a98861f2b4)

## 1.30.30 - 2025-01-08
### Changed 
- Try fixing CI: sbt: command not found - see [Commit](https://pme123@github.com/pme123/camundala/commit/5e5b9b787eb4f74a08cd6efd46a3ae0055da1163)
- Try fixing CI: sbt: command not found - see [Commit](https://pme123@github.com/pme123/camundala/commit/05fd213802b1ba0b7c53a6dfbcf36c245944316c)
- Fixed bad links in Usages Api Docs. - see [Commit](https://pme123@github.com/pme123/camundala/commit/b2db292ca0043efa4cbee20db6ef377cb38cf6fb)
- Adjusted names of elements in templates. - see [Commit](https://pme123@github.com/pme123/camundala/commit/24c348ae191140aaeca8495056d6def526583581)
- Fixed Colorizing BPMNs. - see [Commit](https://pme123@github.com/pme123/camundala/commit/2ba02061f11823c9a24de350c86806bafa384f2f)

## 1.30.29 - 2025-01-05
### Changed 
- Updated Versions. - see [Commit](https://pme123@github.com/pme123/camundala/commit/69a7f565c0a9d8ed2fce96535b4f8f9c8903ef98)
- Removed version from PROJECT.conf - see [Commit](https://pme123@github.com/pme123/camundala/commit/4292ec47edd043a24029006b3f612c3bb457bda6)
- Adjusted Documentation for changes. - see [Commit](https://pme123@github.com/pme123/camundala/commit/9e68f34dad449cf5fae85219386149ad7e762b01)
- Changed configuration for Project to PROJECT.conf - see [Commit](https://pme123@github.com/pme123/camundala/commit/ea26b5caf881c3620b88cdaf68b371bcf01a979a)
- Merge branch 'master' into develop - see [Commit](https://pme123@github.com/pme123/camundala/commit/ae70c4a3df3b2840180738458d47e6eb5f508761)
- Merge pull request #129 from scala-steward/update/laika-sbt-1.3.0 - see [Commit](https://pme123@github.com/pme123/camundala/commit/791f39b48bbd2488f7580d7437d2a7fbff1fbf3d)
- Update laika-sbt to 1.3.0 - see [Commit](https://pme123@github.com/pme123/camundala/commit/d0d6572dcefb050f0bf93c0e54098157cda7e204)
- Fixed @SpringConfiguration. - see [Commit](https://pme123@github.com/pme123/camundala/commit/af82b22143d996e234930a76d36519248529ee41)
- Fixed error in ApiConfig.init for templates. - see [Commit](https://pme123@github.com/pme123/camundala/commit/31bdebc3f833a5da47ce913ba982dc209745aa0e)

## 1.30.28 - 2024-12-31
### Changed 
- Adjusted the spring dependencies due to problems in test worker. - see [Commit](https://pme123@github.com/pme123/camundala/commit/1896166353ee31f7f66e1aab70f32a566df08ded)

## 1.30.27 - 2024-12-30
### Changed 
- Added Company development documentation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/5e668742e60236054ec1804ea34db91a52f8ac42)
- Added Helper documentation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/0ef49dadcea58985fb8bc55367c7d8ea2a260e13)
- Added Worker documentation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/16ca22337ce552e60356f45b0a43371b1ca28cee)
- Added example Worker implementation to 04-worker-c8zio. - see [Commit](https://pme123@github.com/pme123/camundala/commit/c176892b7f2108062b3e2075e0bfdd52c60164ad)
- Added Worker implementation to 04-worker-c7spring. - see [Commit](https://pme123@github.com/pme123/camundala/commit/1c7b7e5526d9cb9635910ab8a110365c50b16a5b)
- Added Simulation documentation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/2de43068083c0d1b6a5f77e7e10b6ebb455618f8)
- Added DmnTester documentation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/f948cbe8880d94a850d6c45f8d06de3ec2508711)
- Merge branch 'master' into develop - see [Commit](https://pme123@github.com/pme123/camundala/commit/7f32346a993dd2e3cc6833fe12fddbbbf9dfee3c)
- Fixed Dependencies. - see [Commit](https://pme123@github.com/pme123/camundala/commit/8182f6c44e43fd6382712428b9233f98a9554aac)
- Merge branch 'master' into develop - see [Commit](https://pme123@github.com/pme123/camundala/commit/a9d3e6fba7b5c1cb260b705fa25396e75e2c5766)
- Documented company/03-api. - see [Commit](https://pme123@github.com/pme123/camundala/commit/6780fe26284cb0b050931287af82cfc1fe6f7ee5)
- Merge pull request #122 from scala-steward/update/tapir-iron-1.11.11 - see [Commit](https://pme123@github.com/pme123/camundala/commit/3358e8151e52f62797c13b6cc5e30ee1c56d56b4)
- Merge pull request #123 from scala-steward/update/sbt-1.10.7 - see [Commit](https://pme123@github.com/pme123/camundala/commit/4bb71adb6772208b0a313f8b489829015f6d84cc)
- Update sbt, sbt-dependency-tree, ... to 1.10.7 - see [Commit](https://pme123@github.com/pme123/camundala/commit/f66e1eb97fb919aa6431f77eda9ba949036ca8c0)
- Update tapir-iron, tapir-json-circe, ... to 1.11.11 - see [Commit](https://pme123@github.com/pme123/camundala/commit/9232fd4285161b3b42d687988f090c483e7623e5)
- Documented company/03-api. - see [Commit](https://pme123@github.com/pme123/camundala/commit/52ca9f0a1287ccb9e8b502fdcadefe60c5e37cac)
- Fixing shortenName for OldName1 mycompany-myproject-myworker.post. - see [Commit](https://pme123@github.com/pme123/camundala/commit/1f02a621e56eb75e849b0656043db802ef2e1e11)
- Fixing bad dependency links. - see [Commit](https://pme123@github.com/pme123/camundala/commit/af29067f95337aa3f6773981d4ddf6fe40e454f8)

## 1.30.26 - 2024-12-19
### Changed 
- Fixing missing OpenApi.yml / adding generic documentation in ApiCreator. - see [Commit](https://pme123@github.com/pme123/camundala/commit/aba43a7e53d6aedca80917ed02a1bf0bb0519930)
- Added tags description in ApiCreator. - see [Commit](https://pme123@github.com/pme123/camundala/commit/866cfad7d1ebdb936ae21a4b9cbd6ef9dacdd577)
- Updating Versions - see [Commit](https://pme123@github.com/pme123/camundala/commit/d1473c30267078397195dcaa97c69cf8007dd304)
- Documenting company bpmn / api. - see [Commit](https://pme123@github.com/pme123/camundala/commit/622339dc44d641941acd93ddbfc993dc8add4f7f)
- Merge remote-tracking branch 'origin/master' into develop - see [Commit](https://pme123@github.com/pme123/camundala/commit/eb5b8709c44ee28b3039746d69fd401b8b12e98d)
- Merge pull request #117 from scala-steward/update/mdoc-2.6.2 - see [Commit](https://pme123@github.com/pme123/camundala/commit/eff93a8865662224d382de45453f530bf6d9ef44)
- Merge pull request #113 from scala-steward/update/munit-1.0.3 - see [Commit](https://pme123@github.com/pme123/camundala/commit/bc9f94594f2b312cce76bdee911b0f57704b0122)
- Update munit to 1.0.3 - see [Commit](https://pme123@github.com/pme123/camundala/commit/76a0578bc64f9e64a56f15fbcd0644e6c95239e4)
- Update mdoc, sbt-mdoc to 2.6.2 - see [Commit](https://pme123@github.com/pme123/camundala/commit/0e82da1c0a7eaec1e9a90a15623fff4e007f3d33)
- Documenting company bpmn / api. - see [Commit](https://pme123@github.com/pme123/camundala/commit/0ca11498125f474a0a76bf582f93049ec0b544f7)
- Generated documentation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/47f17802ba079f095788129ca2468ceb79f08154)
- Merge branch 'develop' - see [Commit](https://pme123@github.com/pme123/camundala/commit/7d510c2d2ccd6b41f098d7dd5ab7cad87d52a4e3)

## 1.30.25 - 2024-12-16
### Changed 
- Started documenting the Company level. - see [Commit](https://pme123@github.com/pme123/camundala/commit/2f4159cc90b2831b792e363237c0497f24d4cc36)
- Added 00-docs CONFIG/VERSIONS files in CompanyDocsGenerator. - see [Commit](https://pme123@github.com/pme123/camundala/commit/e4cc15b935ee44b50e65b632f260f2be2deb21e1)
- Added 00-docs generator. - see [Commit](https://pme123@github.com/pme123/camundala/commit/9aaec9430785fa22bf209110d62e129229694306)
- Updating documentation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/c3d9d3d2d158f5e078ad87bd63d50874c503db8f)
- Fixing prepare-/publishDocs. - see [Commit](https://pme123@github.com/pme123/camundala/commit/9740b77bdc177686afdf11561cae8753dfc1e8ed)
- Moved documentation to 00-docs / integrated DevCompanyCamundalaHelper. - see [Commit](https://pme123@github.com/pme123/camundala/commit/dcff2bd839b1750d74f2b5e525ca9e33f1136bfb)
- Added publish for DevCompanyCamundalaRunner. - see [Commit](https://pme123@github.com/pme123/camundala/commit/e3260d645c13e2b272436dec608971f1e7ef1d8b)
- Cleanup company docs. - see [Commit](https://pme123@github.com/pme123/camundala/commit/928c2b58bd1481f1896f45412ffa64954c3a9261)
- Migrated docker.. to camundala. - see [Commit](https://pme123@github.com/pme123/camundala/commit/76185cf5a8909dfb90b5eddcbd10e8f762242171)
- Migrated deploy to camundala. - see [Commit](https://pme123@github.com/pme123/camundala/commit/d541363d3b6d9345fef10a584f108917c867a543)
- Migrated publish to camundala. - see [Commit](https://pme123@github.com/pme123/camundala/commit/4d9b9f9070fe4e975d62342cd4f8daa440c633b4)
- Documented generate helper. - see [Commit](https://pme123@github.com/pme123/camundala/commit/c710de3cc3de2240817acc719a1abc110bd2a357)
- Added Company Wrappers for all modules in CompanyWrapperGenerator / added mdoc for Version resolving in code. - see [Commit](https://pme123@github.com/pme123/camundala/commit/016c7ab5bfc520e2cdaf76da6097584f2ac8eefd)
- Restructured Helper - Added DevCompanyHelper.createProject. - see [Commit](https://pme123@github.com/pme123/camundala/commit/946b54a31f74fa0a877da4f8a15e4055c47d3222)
- Restructured Helper - Added DevCompanyHelper.initCompany. - see [Commit](https://pme123@github.com/pme123/camundala/commit/c450c2859329b35f18bd4295a5750d708d18edb7)
- redone TimerEvent. - see [Commit](https://pme123@github.com/pme123/camundala/commit/90e6788e02d335667e795748cbe4796c85d46422)
- run documentation/laikaSite. - see [Commit](https://pme123@github.com/pme123/camundala/commit/fedfbb1770215571957aca8078d95a1d5c2cc00d)
- run documentation/laikaSite. - see [Commit](https://pme123@github.com/pme123/camundala/commit/82b77f1fe4a27f7b6ff8bce1e6ac9a754a0cab89)
- Fixing compile errors. - see [Commit](https://pme123@github.com/pme123/camundala/commit/2c966d67dfeef3208af648bc3b69b5bfc341e830)
- Documenting bpmnDsl and worker. - see [Commit](https://pme123@github.com/pme123/camundala/commit/1d77e010a9744dd1f291175bdfc406938639dfe0)
- Updated process to bpmnDsl. - see [Commit](https://pme123@github.com/pme123/camundala/commit/a7eecd5ca066f02f5cece96dc158d84fcecbfa04)
- Working at the documentation. - see [Commit](https://pme123@github.com/pme123/camundala/commit/4a2b1ea8c00dd3a90864ef3be6347e8963e84c34)
- Added align.preset = most to GenericFileGenerator. - see [Commit](https://pme123@github.com/pme123/camundala/commit/711af08d8279f6904d86a239f749cc4184327d6a)

## 1.30.24 - 2024-11-27
### Changed 
- Removed TimeToLive in BpmnProcessGenerator. - see [Commit](https://pme123@github.com/pme123/camundala/commit/195d735f7a38d02ed94d6681ce1ff5d38b503910)
- Improved result comparison of multiline strings. - see [Commit](https://pme123@github.com/pme123/camundala/commit/b14cdb0192227571043872d60448a6a11c111f9b)
- Added variables errorCode and errorMsg to C7WorkerHandler. - see [Commit](https://pme123@github.com/pme123/camundala/commit/a4cadccf2aecf785c8d51bc332342ec8c469e53a)
- Wrapped execution in a Future to handle workers in parallel. - see [Commit](https://pme123@github.com/pme123/camundala/commit/b7a83be64d04f83ec5ddc429f7c2c6067925f771)
- Added deriveUnionApiSchema convenience function. - see [Commit](https://pme123@github.com/pme123/camundala/commit/f80e4955fc7b70a45492a842109a6477a42b5625)
- Migrated to scala-cli for helper script. - see [Commit](https://pme123@github.com/pme123/camundala/commit/b7fe4287932aafe829de2e3634f0a266e2c3bcaa)

## 1.30.23 - 2024-11-14
### Changed 
- Removed jaxb and springboot in SbtSettingsGenerator. - see [Commit](https://pme123@github.com/pme123/camundala/commit/3c7e349796eeed9fe908ad93b203734d4db158c7)

## 1.30.22 - 2024-11-14
### Changed 
- Adjustments in Generation application.yaml for Workers. Fixed bad dependencies. - see [Commit](https://pme123@github.com/pme123/camundala/commit/9c01a75f9232c8c937dbdaf3412b14286f10b74c)

## 1.30.21 - 2024-11-13
### Changed 
- Updated versions. - see [Commit](https://pme123@github.com/pme123/camundala/commit/00ea9117933deb68f555580f79c97300ca68bf42)
- Cleanup repo. - see [Commit](https://pme123@github.com/pme123/camundala/commit/714d37e0f36ebb4e7bf7501d333b9fd206e69c17)

## 1.30.18 - 2024-11-13
### Changed 
- Adjustments in Generation application.yaml for Workers. - see [Commit](https://github.com/pme123/camundala/commit/091419d3ba395f3cec0e260e39c02826bfe1f8d0)

## 1.30.16 - 2024-11-12
### Changed 
- Adjustments in Worker Template generation. - see [Commit](https://github.com/pme123/camundala/commit/ebd970b99abc7acc37e26ae7b3d27d0110da5327)
- Adjusted BpmnProcessGenerator to newest basic pattern. - see [Commit](https://github.com/pme123/camundala/commit/2921a79c04c3723ebf71a32a7146a9dfbc0902df)
- Adjusted mocking, that it returns the filteredOut for mocking. - see [Commit](https://github.com/pme123/camundala/commit/03bf8138a2de02bbc3c7685186c9c8012dd617b9)

## 1.30.15 - 2024-11-06
### Changed 
- Added dynamic mocking / documented . - see [Commit](https://github.com/pme123/camundala/commit/123b890775b3e2d08f7708ba2f415338f867b6d7)
- Removed duplicate xmlns:color definition in BpmnProcessGenerator. - see [Commit](https://github.com/pme123/camundala/commit/7a180c23c8d7425931626a5dd47cc5e2ccfd77c2)
- Updated Versions. - see [Commit](https://github.com/pme123/camundala/commit/0c5904ee82887312e0267689a4a76e6ffb841180)
- Moved logic to ConfVersion / made versionPrevious required. - see [Commit](https://github.com/pme123/camundala/commit/773a4030c49e548a5cb97cb407da28d6954c97e2)
- Fixes in Dependency generators - filter worker dependencies. - see [Commit](https://github.com/pme123/camundala/commit/b2fa224eab7adaef92bf40bd709d1919e537bf91)
- Added worker versions to the dependency Tables. - see [Commit](https://github.com/pme123/camundala/commit/85afd1617108043cabcdb44a74c5c558c4102895)
- Added VERSIONS_PREVIOUS to filter all needed Change Log entries. - see [Commit](https://github.com/pme123/camundala/commit/6a94b9239c9c0a82c08c12b45d204f92da3342b7)
- Added patched to the CompanyDocCreator. - see [Commit](https://github.com/pme123/camundala/commit/767b217a6609bb4e56158c435b8e5a602f14121c)

## 1.30.13 - 2024-10-08
### Changed 
- Fixed bad Regex for multiple lines. - see [Commit](https://github.com/pme123/camundala/commit/17e22bad3b1750b1d41f5b3048b8df595a468871)

## 1.30.12 - 2024-09-13
### Changed 
- Only register Worker from the projects package in C7WorkerHandler, so no Worker from dependency is registered (that is used directly in a Worker). - see [Commit](https://github.com/pme123/camundala/commit/e93ac1b9347f09d2ae6370f045a7ff40627d6f80)
- Fixed bad Seq/Set type generation in CreatorHelper. - see [Commit](https://github.com/pme123/camundala/commit/619b7080272217f354bf783fa0aa36d343532bbc)

## 1.30.11 - 2024-09-05
### Changed 
- Updated Versions - see [Commit](https://github.com/pme123/camundala/commit/ad9b3f79bef8d8d375cd76fc4e774bef4d78d3da)
- Added Worker to DevStatistics. - see [Commit](https://github.com/pme123/camundala/commit/13cf63f64dcaded909cb6851174dae49570c1ba8)
- Support of dynamic signals with processInstanceId / fixed waitFor with String value. - see [Commit](https://github.com/pme123/camundala/commit/76a9e080fbb89e7c0ef5ab822e2d556342489de4)

## 1.30.10 - 2024-08-29
### Changed 
- Migrated from Ammonite to scala-cli. - see [Commit](https://github.com/pme123/camundala/commit/ee2e81ab2d8577f8f318cc5ef70dd1919943886e)

## 1.30.9 - 2024-08-27
### Changed 
- Fixed bad mocking handling in case of success. - see [Commit](https://github.com/pme123/camundala/commit/f05cec8831a8498a0b1342263ae9e929cb3ae091)
- Updated to Scala 3.5.0. - see [Commit](https://github.com/pme123/camundala/commit/110390600a2fdfadc3a0d1cb9c9ec3dce8b8c56a)
- Moved chimney dependency to bpmn. - see [Commit](https://github.com/pme123/camundala/commit/765a2444a7966f73dbfb89642515dacc007ce812)

## 1.30.7 - 2024-08-16
### Changed 
- Added possibility to add CatchAll to handledErrors (general variable) - see [Commit](https://github.com/pme123/camundala/commit/b3ca366f30738d8c01523374fd23c40c30f7c4e6)
- Adjusting naming for API docs. - see [Commit](https://github.com/pme123/camundala/commit/b32feb75a86db3a0dc44af917562246ce3c6c31a)
- Added color to bpmn schema BpmnProcessGenerator. - see [Commit](https://github.com/pme123/camundala/commit/2c9338b6eea79d64a28cac3ae57866702b98abdf)

## 1.30.6 - 2024-08-15
### Changed 
- Added possibility to ignore response body with NoOutput. - see [Commit](https://github.com/pme123/camundala/commit/c41fc22c16690b5504b47c72494ac0b028f02caf)
- Added chimney mapping dependency to worker project. - see [Commit](https://github.com/pme123/camundala/commit/592538a7415f9814d2643feaa82d931782098114)

## 1.30.5 - 2024-08-12
### Changed 
- Added tests null value serialisation / fixed tests - see [Commit](https://github.com/pme123/camundala/commit/ac2009c54160b22cece8a1f5e5c0c0e322696d93)
- Keep null values in json for Camunda variables. - see [Commit](https://github.com/pme123/camundala/commit/f7e5dbd324dd6f9a79b5dec999d6fdc17663ee3e)

## 1.30.4 - 2024-08-12
### Changed 
- Fixes in shortenName. - see [Commit](https://github.com/pme123/camundala/commit/8b3694307ffd3f4b74c37199c319ff816463e430)
- Fixed older naming in shortenName. - see [Commit](https://github.com/pme123/camundala/commit/18646117382ea715222ccc5d8043f971b5059678)

## 1.30.3 - 2024-08-11
### Changed 
- Small adjustments to InitWorker api endpoint. - see [Commit](https://github.com/pme123/camundala/commit/cad1ccc0e9d07e3e2ae1f05b7d72e3fbb68a6d0b)
- Changed default to NoInput in InitWorker customInit. - see [Commit](https://github.com/pme123/camundala/commit/e7c92bf7e27f30b8b4e84d0ad15bd9bb3a9be86b)

## 1.30.2 - 2024-08-11
### Changed 
- Updated missing dependencies. - see [Commit](https://github.com/pme123/camundala/commit/8fb12016fdf4d2e3695f466a7fddb499a7022ed1)
- Updated Versions. - see [Commit](https://github.com/pme123/camundala/commit/731b5d0f5546c8c70aefc0df4a1358eb28f58e04)
- adjusted jvm to 21 to be save in github action. - see [Commit](https://github.com/pme123/camundala/commit/4ae06c47e60f1492ab853b5837858b782880afc3)
- fixing jvm version in Github action - see [Commit](https://github.com/pme123/camundala/commit/2a75444d7cb24f6ea106052c0a9c9cb9b7697a66)
- Fixed Test failure and Github action warnings. - see [Commit](https://github.com/pme123/camundala/commit/a8645387515517749c243df5fc489a2379d05736)

## 1.30.1 - 2024-08-10
### Changed 
- Adjusted References to new naming in ProcessReferenceCreator. - see [Commit](https://github.com/pme123/camundala/commit/7d141e22e31ee7b624778cb829b2d08fe2c40f4d)
- Fixed BPMN/DMN names for API Documentation - see [Commit](https://github.com/pme123/camundala/commit/166989553c38fe3adbee0e92dd4f53015c26445f)
- Merge branch 'MAP-8900-InitIn-Type' into develop - see [Commit](https://github.com/pme123/camundala/commit/7cc07cd4c4043a430c7ffeab831ba0a849ac2a92)

## 1.30.0 - 2024-08-08
### Changed 
- Added InitWorker to Api Config. - see [Commit](https://github.com/pme123/camundala/commit/11f10c095b66fe51f91c278c6da737d0d4e07dc3)
- Adjusted BpmnGenerator and WorkerGenerator for InitIn. - see [Commit](https://github.com/pme123/camundala/commit/d06ad88e8048574c668ffd8d63b1028058d463fa)
- Added InitIn Type - no compile errors. - see [Commit](https://github.com/pme123/camundala/commit/040b87de98e5f1bbb88f70a9626bde25d81c379e)
- MAP-8900: Added InitIn Type to Process Class. - see [Commit](https://github.com/pme123/camundala/commit/31ee4c40d4cecb74bbc6eb479257531ee1cbfdb6)
- fixed getTypes in SeviceClassesCreator. - see [Commit](https://github.com/pme123/camundala/commit/c27a8f002c92cdcd430963708271e95ccce44f16)
- Adjusted shortName for GenericeServiceIn. - see [Commit](https://github.com/pme123/camundala/commit/8a7bbea4e4552d8a562d055d18d895a57601e766)
- Adjustments in TopicName for API generation. - see [Commit](https://github.com/pme123/camundala/commit/c768e316880e0d0e8480a1365cb3b3d79890528b)

## 1.29.29 - 2024-07-16
### Changed 
- General ProcessStatus modified / some adjustments in Generators. - see [Commit](https://github.com/pme123/camundala/commit/c8a2da1983a4fa21ae69a0d18ac41161508af924)
- Fixed mockedWorker to mockedWorkers in docs. - see [Commit](https://github.com/pme123/camundala/commit/c911c3b96204fc92437ed304a635cca73f3b4876)
- Adjusted Topic naming in API generation - see [Commit](https://github.com/pme123/camundala/commit/b29d88c697cfb14ffebb309d63275cbd59e6459f)

## 1.29.28 - 2024-07-12
### Changed 
- Added also Option as valid response type for no response body. - see [Commit](https://github.com/pme123/camundala/commit/762bb497e39e396084e55112f51398a7bfab3802)
- Added tests for NoOutput Check. - see [Commit](https://github.com/pme123/camundala/commit/0e1fcaa240c64f3003993efa92634b60d8383480)

## 1.29.27 - 2024-07-11
### Changed 
- Fixed test:compile problem - see [Commit](https://github.com/pme123/camundala/commit/81f9677b33646d6537754c051afc1dd32adbbbd4)

## 1.29.26 - 2024-07-11
### Changed 
- Adjusted naming for deriveEnumValue - see [Commit](https://github.com/pme123/camundala/commit/18bad4713d4caa66b3effb903171ce5bce291a0a)
- Added toJson to CamundaVariable that translates the result of the Camunda 7 Service to a json object. - see [Commit](https://github.com/pme123/camundala/commit/b067910a188e4189b487bd224ffd2b270f3994f7)
- Added ClassTag to RestApiClient - to get rid of warning. - see [Commit](https://github.com/pme123/camundala/commit/0f66e1df4724a8e2fa9b8d7f1071d6a73fa329fb)
- Merge commit '698338f79914b7bb7bb733dce853aab87e8f0a95' into develop - see [Commit](https://github.com/pme123/camundala/commit/79f310caa9125aee624ac2f0746f2c99f774ed3c)
- Merge pull request #41 from scala-steward/update/sbt-1.10.1 - see [Commit](https://github.com/pme123/camundala/commit/698338f79914b7bb7bb733dce853aab87e8f0a95)
- Update sbt, sbt-dependency-tree to 1.10.1 - see [Commit](https://github.com/pme123/camundala/commit/d2ea71fb00ae3fb33ffb5f3d4d85fa307c309d65)

## 1.29.25 - 2024-07-09
### Changed 
- Added LocalDate to CamundaVariable.valueToCamunda. - see [Commit](https://github.com/pme123/camundala/commit/34a5816fc0e1dcd5010eca8e9d9544cf6c6407d6)

## 1.29.24 - 2024-07-09
### Changed 
- Fixed bad inConfig initialization if the inConfig was defined in In object. - see [Commit](https://github.com/pme123/camundala/commit/9d319a43d5c8fe4b32d7a76696b51a39719e0f83)

## 1.29.23 - 2024-07-06
### Changed 
- Updated Versions - see [Commit](https://github.com/pme123/camundala/commit/9835c82254deff34737b4583b99f032c5774d127)
- Take input variables to override InConfig variables. - see [Commit](https://github.com/pme123/camundala/commit/56308e9e67968395f37e7bff307b62c3ff228204)
- Makedir added for app runner. - see [Commit](https://github.com/pme123/camundala/commit/f9c43b03c153b561a6ce21239123a56176c27bfa)

## 1.29.22 - 2024-06-21
### Changed 
- Added Generator for WorkerTestApp runner for intellij and vscode. - see [Commit](https://github.com/pme123/camundala/commit/ed4883b951cbea6a2cae8516497d46f9e95d4627)
- Fixed compile errors after merging stewards version updates. - see [Commit](https://github.com/pme123/camundala/commit/a1b5191e90b8f722be93d5ba5051ca8a48ec9778)


## 1.29.21 - 2024-06-10
### Changed 
- Redone Scala Version. - see [Commit](https://github.com/pme123/camundala/commit/4a1f091965ef9ce4e2b67c88b2257907d6ef7f54)

## 1.29.20 - 2024-06-10
### Changed 
- Redone charset.name - see [Commit](https://github.com/pme123/camundala/commit/81820b91ded96c4f36b813ba83422c701be4c3e2)
- Changed back to version 17 - see [Commit](https://github.com/pme123/camundala/commit/b390597fb1cd0b075a0e159e18c014fafb1f6ee0)
- Removed java version - see [Commit](https://github.com/pme123/camundala/commit/44ddc0330d734eccff468a487e60e01c4b492acb)
- Using name of charset. - see [Commit](https://github.com/pme123/camundala/commit/86631627109aa31e219e9fd2b88d855c1005de79)
- Testing java-versions in github actions. - see [Commit](https://github.com/pme123/camundala/commit/57a51fd9d195ce23d3828f8c27c199a0dd17464e)
- Adjusted java-version in github actions. - see [Commit](https://github.com/pme123/camundala/commit/7c185764b7ff790209da38fe64ef8f6f66837836)
- Tested updated versions by Scala Steward. - see [Commit](https://github.com/pme123/camundala/commit/3a178dca020adad7877506373d348b9ce2eec187)
- Added Better Exception Handling. - see [Commit](https://github.com/pme123/camundala/commit/683bf04476e456983bbee9f3d90faf295319f6fa)
- Merge branch 'refs/heads/master' into develop - see [Commit](https://github.com/pme123/camundala/commit/e0363b3d1c166efdc19e45a38531372d0fc52230)
- Merge remote-tracking branch 'refs/remotes/origin/master' into develop - see [Commit](https://github.com/pme123/camundala/commit/442fa5b9f7b1188aa634f4bfccf699327efd970a)
- Merge pull request #1 from scala-steward/update/sbt-buildinfo-0.12.0 - see [Commit](https://github.com/pme123/camundala/commit/d398dce03afacec119886bff4874682309d4acc1)
- Merge pull request #2 from scala-steward/update/jackson-module-scala-2.14.3 - see [Commit](https://github.com/pme123/camundala/commit/59c9fe23b7d6ecf80b32753fc5ef3f22f4c993bc)
- Merge branch 'master' into update/jackson-module-scala-2.14.3 - see [Commit](https://github.com/pme123/camundala/commit/f094989fe3d11386b55b6a33a98e433ad856d984)
- Merge pull request #3 from scala-steward/update/sbt-native-packager-1.10.0 - see [Commit](https://github.com/pme123/camundala/commit/7ac46b0608dc36b35615dac6ee053d7774809e4b)
- Merge pull request #4 from scala-steward/update/h2-2.2.224 - see [Commit](https://github.com/pme123/camundala/commit/169f2ba86fa5b5d3689c75489f327eef9daedcff)
- Merge branch 'master' into update/h2-2.2.224 - see [Commit](https://github.com/pme123/camundala/commit/93541bce9ff05cf27e147fe5951f5cdaa33634a2)
- Merge pull request #5 from scala-steward/update/os-lib-0.10.2 - see [Commit](https://github.com/pme123/camundala/commit/2c362631f580d4c0b8c5afb2a088b7ed8e24bf68)
- Merge branch 'master' into update/os-lib-0.10.2 - see [Commit](https://github.com/pme123/camundala/commit/56c1ed73e1bee73219369b489d51523ad5871bd2)
- Merge pull request #6 from scala-steward/update/openapi-circe-yaml-0.7.4 - see [Commit](https://github.com/pme123/camundala/commit/94c29632e015a7067a4460cb6050e20c049b434b)
- Merge branch 'master' into update/openapi-circe-yaml-0.7.4 - see [Commit](https://github.com/pme123/camundala/commit/5c0039ee89fff37238e9dedab71958858e3e5626)
- Merge pull request #7 from scala-steward/update/circe-3.8.16 - see [Commit](https://github.com/pme123/camundala/commit/c5499a7054f4589f5765360b20f40e822b83b31a)
- Merge branch 'master' into update/circe-3.8.16 - see [Commit](https://github.com/pme123/camundala/commit/ddea908e778a99181b07d3a39b591da3343b3bbb)
- Merge pull request #8 from scala-steward/update/tapir-iron-1.10.8 - see [Commit](https://github.com/pme123/camundala/commit/45143f39ed602c19f8d2ddf66970ed9ba9fb59f2)
- Merge branch 'master' into update/tapir-iron-1.10.8 - see [Commit](https://github.com/pme123/camundala/commit/e350937f356ee13b038958a44012599c9e500b49)
- Merge pull request #9 from scala-steward/update/typesafe-1.4.3 - see [Commit](https://github.com/pme123/camundala/commit/00aaa83d6b42ffe89617bcb34b0fc2953d08d4df)
- Merge branch 'master' into update/typesafe-1.4.3 - see [Commit](https://github.com/pme123/camundala/commit/94112ead5123977c75e88f90b6396bfa3890e041)
- Merge pull request #10 from scala-steward/update/spring-boot-starter-camunda-8.5.4 - see [Commit](https://github.com/pme123/camundala/commit/b87d2bfa1266222829ee603a1ec56463445fd0db)
- Merge branch 'master' into update/spring-boot-starter-camunda-8.5.4 - see [Commit](https://github.com/pme123/camundala/commit/5c69bd4217bcf7d6c70e20fd9e898fb7e770ecc7)
- Merge pull request #11 from scala-steward/update/iron-circe-2.5.0 - see [Commit](https://github.com/pme123/camundala/commit/2fb825c2153fd146ab617a11296190afd9545206)
- Merge pull request #12 from scala-steward/update/netty-all-4.1.110.Final - see [Commit](https://github.com/pme123/camundala/commit/7d13eba4e77a28057de63144361829b104ae2453)
- Merge branch 'master' into update/iron-circe-2.5.0 - see [Commit](https://github.com/pme123/camundala/commit/dd42314e64fd1cbb77ae2ec18e6a44662eb0d991)
- Merge pull request #17 from scala-steward/update/groovy-jsr223-3.0.21 - see [Commit](https://github.com/pme123/camundala/commit/b061fcb6f2218f6457fd01b79c45f4a7e6234d7f)
- Merge branch 'master' into update/netty-all-4.1.110.Final - see [Commit](https://github.com/pme123/camundala/commit/a2f548815b3d060567593e9a76487bbd07b149e9)
- Merge pull request #13 from scala-steward/update/swagger-parser-2.1.22 - see [Commit](https://github.com/pme123/camundala/commit/eb725e131244c5c8f54bbf3d7606a79d8afa331f)
- Merge pull request #14 from scala-steward/update/camunda-engine-plugin-spin-7.21.0 - see [Commit](https://github.com/pme123/camundala/commit/d8efcfb7f4282c9f20c0b09b15541a29f15ed44d)
- Merge pull request #15 from scala-steward/update/camunda-bpm-spring-boot-starter-external-task-client-7.21.0 - see [Commit](https://github.com/pme123/camundala/commit/b4585c7adedaf2ee35d8e0577d46df364fb9b24b)
- Merge pull request #16 from scala-steward/update/camunda-spin-dataformat-json-jackson-1.18.5 - see [Commit](https://github.com/pme123/camundala/commit/10c934ab1f9c216bfea99e6decb47234be7358e0)
- Merge pull request #21 from scala-steward/update/scalafmt-core-3.7.17 - see [Commit](https://github.com/pme123/camundala/commit/4d21ffcd685eed43abed3615680ad18cedad9e66)
- Merge branch 'master' into update/groovy-jsr223-3.0.21 - see [Commit](https://github.com/pme123/camundala/commit/19aa5d3a530f34fe2d7d483bb559046ec91b08f3)
- Merge pull request #18 from scala-steward/update/scala-xml-2.3.0 - see [Commit](https://github.com/pme123/camundala/commit/ba779abefb771655d5574b65a46167903e483c3b)
- Merge pull request #19 from scala-steward/update/sbt-1.9.9 - see [Commit](https://github.com/pme123/camundala/commit/c778d4b53d7fa5949e86e164d4c1d304211c1a9b)
- Merge pull request #20 from scala-steward/update/munit-1.0.0 - see [Commit](https://github.com/pme123/camundala/commit/25b86a99da6b852ec28e053ec59efa9a1bedccfb)
- Merge pull request #22 from scala-steward/update/spring-boot-starter-2.7.18 - see [Commit](https://github.com/pme123/camundala/commit/e1e476496222ff295059730a3149bcae5d0a9f97)
- Merge pull request #23 from scala-steward/update/laika-sbt-1.0.1 - see [Commit](https://github.com/pme123/camundala/commit/36466251af9948a1c68a92f56c9461a330214727)
- Update laika-sbt to 1.0.1 - see [Commit](https://github.com/pme123/camundala/commit/118dbffc32e08af047e019c96f650405aba86bf1)
- Update spring-boot-starter, ... to 2.7.18 - see [Commit](https://github.com/pme123/camundala/commit/733d0db6c52071a99fc7060e8fc1de3740ea36cd)
- Add 'Reformat with scalafmt 3.7.17' to .git-blame-ignore-revs - see [Commit](https://github.com/pme123/camundala/commit/6f4a8e5744b6e0a2f62ef3dc629f57f265a147ed)
- Reformat with scalafmt 3.7.17 - see [Commit](https://github.com/pme123/camundala/commit/9623b26118320234e1b09a6fe4c89bcbaf25898b)
- Update scalafmt-core to 3.7.17 - see [Commit](https://github.com/pme123/camundala/commit/36007d2ce78666beee8036b035f0837ef6591ba2)
- Update munit to 1.0.0 - see [Commit](https://github.com/pme123/camundala/commit/88a519fa3fdacec9705b747b7dbfe0ed1ea7cead)
- Update sbt, sbt-dependency-tree to 1.9.9 - see [Commit](https://github.com/pme123/camundala/commit/f16bf06b264e199f1d0d75f0efd876827f4ac289)
- Update scala-xml to 2.3.0 - see [Commit](https://github.com/pme123/camundala/commit/dc688b1061efb49de8d6320c3f4e5730c5e1511b)
- Update groovy-jsr223 to 3.0.21 - see [Commit](https://github.com/pme123/camundala/commit/59b687c3353995054fbc55b96f54f48b1762a3fe)
- Update camunda-spin-dataformat-json-jackson to 1.18.5 - see [Commit](https://github.com/pme123/camundala/commit/72b626cd303d2a32808be846421b4af9066dc171)
- Update camunda-bpm-spring-boot-starter-external-task-client, ... to 7.21.0 - see [Commit](https://github.com/pme123/camundala/commit/f13269aff9e52efabe46cf64a78675f60c613fe6)
- Update camunda-engine-plugin-spin, ... to 7.21.0 - see [Commit](https://github.com/pme123/camundala/commit/f4f2bee403bec03033724584d5ad9fded212bf84)
- Update swagger-parser to 2.1.22 - see [Commit](https://github.com/pme123/camundala/commit/c3439184b78577f002384bf055c51b4552a850af)
- Update netty-all to 4.1.110.Final - see [Commit](https://github.com/pme123/camundala/commit/7cdd46a910a892cb9fd9995c5f0884ece5711c41)
- Update iron-circe to 2.5.0 - see [Commit](https://github.com/pme123/camundala/commit/bb6020d235fe7ba40f448cc0060df55a191c8691)
- Update spring-boot-starter-camunda to 8.5.4 - see [Commit](https://github.com/pme123/camundala/commit/b1d6b6a6724a9a87c4d59289512cd7abc4962a93)
- Update typesafe:config to 1.4.3 - see [Commit](https://github.com/pme123/camundala/commit/d78c5f403f57a6b0d942fc296e8eca0a7d626c07)
- Update tapir-iron, tapir-json-circe, ... to 1.10.8 - see [Commit](https://github.com/pme123/camundala/commit/0061ab18f906f0b1916e27dca5f099ccceed9acb)
- Update circe to 3.8.16 - see [Commit](https://github.com/pme123/camundala/commit/1c0f49d86c23e6d9b12748db5e0a3fee68ce0a9b)
- Update openapi-circe-yaml to 0.7.4 - see [Commit](https://github.com/pme123/camundala/commit/ab1a7c595a0fc216be288b046e4ad4b11c05ea82)
- Update os-lib to 0.10.2 - see [Commit](https://github.com/pme123/camundala/commit/5dd65af68d414bb43d224fca4046317aa4a182b2)
- Update h2 to 2.2.224 - see [Commit](https://github.com/pme123/camundala/commit/c8d3867e5f43013dd8c8d5803bf39ad6649ff987)
- Update sbt-native-packager to 1.10.0 - see [Commit](https://github.com/pme123/camundala/commit/6e13e0f591d8f580fbb21cde6ecb790dc046b134)
- Update jackson-module-scala to 2.14.3 - see [Commit](https://github.com/pme123/camundala/commit/17cc116c045cb0a5d4fe2eba73bf41cf7951526f)
- Update sbt-buildinfo to 0.12.0 - see [Commit](https://github.com/pme123/camundala/commit/04ca31941498faeeb53df38c590b2239707765d4)
- update Generator dockerUp added imageVersion. - see [Commit](https://github.com/pme123/camundala/commit/54feb13476a1a0b2b7ca04b57aa34be42c64ea0b)
- Merge branch 'develop' - see [Commit](https://github.com/pme123/camundala/commit/82a5ff411ee8851331ae9bf84925e63b333b035f)

## 1.29.19 - 2024-06-06
### Changed 
- Back to Scala 3.3 / Scala Stuwart batch added - see [Commit](https://github.com/pme123/camundala/commit/4c346c27a65292cf8dc14f8bf946c705927953a6)
- Updated to Scala 3.4.2 / fixed shortName for old pattern. - see [Commit](https://github.com/pme123/camundala/commit/97366e66b77f6b66a4d5b16d71024584d2e46ae8)
- Removed deepDropNullValues for Config init. - see [Commit](https://github.com/pme123/camundala/commit/980f28b4fcb3b60e9596f0f050acad5e8a27d2a8)

## 1.29.18 - 2024-05-25
### Changed 
- Shortened names in Api documentation (endpointName - navigation link and title). - see [Commit](https://github.com/pme123/camundala/commit/37f0412660a29ed14e2329af9ca82a7fa6a37e40)
- Try to get rid of null values in request bodies. - see [Commit](https://github.com/pme123/camundala/commit/a9abd9fcb5727f1709bab5b4f7a7fdf2d8923068)

## 1.29.16 - 2024-05-24
### Changed 
- Fixed response of no body at all Option[NoOutput] - see [Commit](https://github.com/pme123/camundala/commit/b31bb293bbfdf5470a35d529d734357b1108c6c8)
- Fixed Postcode to PostcodeStr. - see [Commit](https://github.com/pme123/camundala/commit/27bb356d1205f6e97b198f81fa91bf02a04f6f56)

## 1.29.15 - 2024-05-23
### Changed 
- Added Graviton Schema for Testing ApiGeneration. - see [Commit](https://github.com/pme123/camundala/commit/d40b7bf257ac1582e7c7f26f1ce7aaac403ecc62)
- Added color reference to generated BPMN. - see [Commit](https://github.com/pme123/camundala/commit/8ae40efacdfc80ffd2bf216f8a6127dbc9aea0aa)
- Added deepDropNullValues to asJson. - see [Commit](https://github.com/pme123/camundala/commit/5aa975aaa3b977e37690c5580ec08fe9aba4ff86)
- Fixed bad identifiers in BpmnProcessGenerator. - see [Commit](https://github.com/pme123/camundala/commit/bd37a8e31bc276c6b82601c86f1368a088fc8ba0)

## 1.29.14 - 2024-05-20
### Changed 
- Added createIfNotExists to ApiGenerator. - see [Commit](https://github.com/pme123/camundala/commit/1e7e63564b358bf235407bc6e2ac9d1b2f133d75)
- Added ValueSimple for simple json values. - see [Commit](https://github.com/pme123/camundala/commit/997b28b942fddec4d32eeeacb04154681cb7f355)

## 1.29.13 - 2024-05-18
### Changed 
- Added Process in subProcess in OpenAPI Generation. - see [Commit](https://github.com/pme123/camundala/commit/d3d685dcc0d9120060ccde5819a6c3dd12f7be12)
- Improvements in OpenAPI Generation. - see [Commit](https://github.com/pme123/camundala/commit/c8aef1aa311c413a71f2810dfd65a2e04be05513)
- Added macro to extract the fieldnames from Product or Enum - not working in runtime on generic Types. - see [Commit](https://github.com/pme123/camundala/commit/4aa15f54f9e2295d719ee199e8b234e9e862ff4e)
- Added withEnumInExamples to BPMN Types. - see [Commit](https://github.com/pme123/camundala/commit/cee26b9aea0b6f4d2711441c06f2fa4c0560a53f)
- Improvements and fixes in code generators. - see [Commit](https://github.com/pme123/camundala/commit/d28d968e99b2d133dd54f56cf3cbcc99ae4acbcd)
- Added toServiceResponse to MockedServiceResponse for simpler testing. - see [Commit](https://github.com/pme123/camundala/commit/ff87139b689e3c315c0f94259d6f55d636583df3)
- Added App to DmnTesterStarter. - see [Commit](https://github.com/pme123/camundala/commit/021335d54c1b055aeac28e495a7226f722239eb2)
- Adjusted generating testSettings. - see [Commit](https://github.com/pme123/camundala/commit/8d48d1274f2bbdaba277c048362d0931d927634a)

## 1.29.12 - 2024-05-14
### Changed 
- Included Type in refined types, added percentage/ tests for refined types. - see [Commit](https://github.com/pme123/camundala/commit/8a408baf8dbdf4461309d1e814015ab62e9a4ec7)
- Removed version from banner. - see [Commit](https://github.com/pme123/camundala/commit/a451cf15842cdd1f6f8be93cb830debd9b0f72ab)
- Fixed timing test and adjusted sbt tasks in github action. - see [Commit](https://github.com/pme123/camundala/commit/b9e7ac87edbdd638c04d30a0e0c7dfcf45f739a8)
- Added customDecodeAccumulating for custom decoders. - see [Commit](https://github.com/pme123/camundala/commit/ff083210cf3f1aaa00213bc33d2d15f407d808ee)

## 1.29.11 - 2024-05-07
### Changed 
- Extended LocalDateTimeDecoder with ISO Datetime format. - see [Commit](https://github.com/pme123/camundala/commit/a1cf42126ac848cf4ad0e382f8d3db27fed6267d)

## 1.29.10 - 2024-05-06
### Changed 
- Removed type constraint Product from ServiceIn. - see [Commit](https://github.com/pme123/camundala/commit/87b352608e9a1c5e9b18b43eb480c0469febfb4e)
- Adjustments in Generators. - see [Commit](https://github.com/pme123/camundala/commit/cce1cafe57523eb2e9e298e122a9b8dbe88e5be7)
- Adjustments in Generators. - see [Commit](https://github.com/pme123/camundala/commit/ee01c168f4954d00481e3843e4a7d0e5e583842d)

## 1.29.9 - 2024-04-30
### Changed 
- Adjusted Event name with version in BpmnGenerator. - see [Commit](https://github.com/pme123/camundala/commit/c3c5c53b41f44da93d19a9eaade3bcfa119c4d48)
- Limited Decoding Error Message to 3500 - to avoid Camunda Error. - see [Commit](https://github.com/pme123/camundala/commit/4b28160b8e10edb7cdb55777254a7e047e07fb0c)
- Added cancel to NotValidStatus. - see [Commit](https://github.com/pme123/camundala/commit/c9a20c1d52818674b3045b5ba197fcfaac638f3c)
- Adjusted download name in OpenApi.html generator. - see [Commit](https://github.com/pme123/camundala/commit/0d3c7511be39b49e535543b11d4aeefa0acfaf24)
- Fixed bug in only if there are process steps. - see [Commit](https://github.com/pme123/camundala/commit/166ad52156ca22ae0db2474189c17b42cdb51be5)
- Fixed bug in only if there are process steps. - see [Commit](https://github.com/pme123/camundala/commit/c0e6fe2feeed50f369c99dce23e87b86c59679dd)
- Added CHANGELOG to SetupGenerator. - see [Commit](https://github.com/pme123/camundala/commit/048b2eabf85cbc261fc415280aac83874b77168a)
- Adjusted logging files not updated. - see [Commit](https://github.com/pme123/camundala/commit/f91267ab1d66b0b357395bc26f1d11b442dc6bf3)

## 1.29.8 - 2024-04-14
### Changed 
- Type Fix for descr. - see [Commit](https://github.com/pme123/camundala/commit/b2b8f51228db0b5f50faa9d1a77e6c64c7a81c02)
- Added ApiGenerator HTMLs and download button for SVG BPMNs. - see [Commit](https://github.com/pme123/camundala/commit/bf24f4e3318832c5a1e6b542a1e755f8e3147cb7)
- Merge remote-tracking branch 'origin/develop' into develop - see [Commit](https://github.com/pme123/camundala/commit/61e92d35cde0220847ae6d32cfcbfe95ba06a490)
- Added ApiGenerator / adjusted description of ApiProjectGenerator - see [Commit](https://github.com/pme123/camundala/commit/65ffbf986c5ced7ede2b698ba02833a1bc2ebe60)
- Added ApiGenerator / adjusted description of ApiProjectGenerator - see [Commit](https://github.com/pme123/camundala/commit/dbc40274fb0b22f7c2da689fd1852cf93816fa85)
- Setup default Process Generation. - see [Commit](https://github.com/pme123/camundala/commit/863d1313660a83ea96ca8c80bb3a096f8c613d7d)
- Added application.yaml and banner.txt for Worker generation. - see [Commit](https://github.com/pme123/camundala/commit/7e9e676369bc904446e89f2006bcf14ad7990a0c)
- Fixed problem CamundaVariable toCamunda Long -> Int. - see [Commit](https://github.com/pme123/camundala/commit/40a915316c54a44e69bc922c795945ec0dd313fb)

## 1.29.7 - 2024-04-08
### Changed 
- Changed worker project dependency to compile from test (to run workers within workers). - see [Commit](https://github.com/pme123/camundala/commit/d69d7af23de9535ebf75931c5558edb8939d23d6)
- Added function to run worker from worker. - see [Commit](https://github.com/pme123/camundala/commit/f740d381a1a4ecd242d3564b4391b966d9fde381)
- Adjusted ComposedWorker Example. - see [Commit](https://github.com/pme123/camundala/commit/9d2dd10e5edc8b32e14e7a9b6d8d183fc257dba9)
- Added ComposedWorker. - see [Commit](https://github.com/pme123/camundala/commit/4271ba71f083f2ede7701c6c3ac94ef62320256f)
- Implemented only Scenario. - see [Commit](https://github.com/pme123/camundala/commit/ea9bf6c9137939ce9fa15853db9d3423e7dc5022)

## 1.29.6 - 2024-04-04
### Changed 
- Added Decision to Code Generation. - see [Commit](https://github.com/pme123/camundala/commit/e26712d589dcf344a813b1993e87e112c532c1df)
- Added withInOutExample to ApiBaseDsl. - see [Commit](https://github.com/pme123/camundala/commit/336e4634747f7bbacc4746def18fa5810d26b082)

## 1.29.5 - 2024-03-27
### Changed 
- Fixed Decoder for MockedServiceResponse. - see [Commit](https://github.com/pme123/camundala/commit/86f816bfcb2639a296d8f3bcc7d7bfd4d4b4a8e9)
- Added InConfig to Process Generation. - see [Commit](https://github.com/pme123/camundala/commit/61170021ec93a44f6a15a28eee98de954a140db5)
- Added missing Spring dependency. - see [Commit](https://github.com/pme123/camundala/commit/8f8b8982804e7f68ea5237b0cd9cd8eb5a09f775)
- Business Key only for CallActivities in ModelerTemplGenerator. - see [Commit](https://github.com/pme123/camundala/commit/b8c7a1c5f0648d2d72d08fb3256566ddc89e539c)
- Adjustment in Bpmn ServiceTask trait. - see [Commit](https://github.com/pme123/camundala/commit/ede49fc03db9e46dc9dd098b20a3b9c75821c588)
- Adjustment in Bpmn ServiceTask trait. - see [Commit](https://github.com/pme123/camundala/commit/9192af095bb49511798cf8d67e71e3ba2428a590)

## 1.29.4 - 2024-03-25
### Changed 
- Fixes and adjustments in code generators. - see [Commit](https://github.com/pme123/camundala/commit/976ecd078e814a2d8b0570b902cf9033541d1005)

## 1.29.3 - 2024-03-23
### Changed 
- Renamed BpmnWorkerDsl to BpmnExternalTaskDsl / added generators for process steps. - see [Commit](https://github.com/pme123/camundala/commit/6afca149d688263267b51c9eca89a91c06e3df39)
- Fixes in Template Generation. - see [Commit](https://github.com/pme123/camundala/commit/0e84d3acbcea580d3c28ca00df41f5362f0eb18b)

## 1.29.2 - 2024-03-19
### Changed 
- Adjustments in DSLs for BPMN Process Steps. - see [Commit](https://github.com/pme123/camundala/commit/430feac7eef951b19abc27daf932a59b2947b898)
- Added DSLs for BPMN Process Steps. - see [Commit](https://github.com/pme123/camundala/commit/5e8cdd0379f30f8e38959e5f86ad2375fb9d4e2d)
- Fixed test:compile errors / fixed SimulationGenerator. - see [Commit](https://github.com/pme123/camundala/commit/61ed82030668f0a6750d706be6e0709e63438fd4)
- Added process generated. - see [Commit](https://github.com/pme123/camundala/commit/92c0e552e8ded2e5dfc1c6c3ae61f957a9257063)
- Added process generated. - see [Commit](https://github.com/pme123/camundala/commit/96940d536c945e569de6d8361a7c1e01093e9dfd)
- Added customWorker generated. - see [Commit](https://github.com/pme123/camundala/commit/f60830da195378d443e2dd5045e9ada3fb020790)
- Cleanup DevHelper.update. - see [Commit](https://github.com/pme123/camundala/commit/3edb681978a4aa6c196853e58ec0fbb5de124aec)

## 1.29.1 - 2024-03-14
### Changed 
- fixed Compile error. - see [Commit](https://github.com/pme123/camundala/commit/d7bef7451fa7cc948a1c42d1eebf524a4eb19fe9)
- Cosmetics / Fix in WorkerGenerator and C7WorkerHandler. - see [Commit](https://github.com/pme123/camundala/commit/1bf74aa33c5d5ff84eec62b940093d886f29d8ab)
- Updated Scala / Added LocalDate to Camunda serialization. - see [Commit](https://github.com/pme123/camundala/commit/45af659a5077f3d2011c7ae17d3ee7ec988f7a2c)

## 1.29.0 - 2024-03-12
### Changed 
- Some Improvements. - see [Commit](https://github.com/pme123/camundala/commit/cfdfde2a9aa0d81fc40034818d0a6c5b1f63cab4)
- Added APi Examples with otherEnum Examples. - see [Commit](https://github.com/pme123/camundala/commit/fa4b756241cc95ce030e1661df90225f8e041ce2)
- Added otherEnum Examples - to support In Enums in Workers (variable generation). - see [Commit](https://github.com/pme123/camundala/commit/eb2092a6fd5c3460c7c3257424accb38db438fb6)
- Naming cleanup / Company-. - see [Commit](https://github.com/pme123/camundala/commit/6c3331986a17e676846c25cac9d02e4fcaf0b97f)
- Added HelperGenerator. - see [Commit](https://github.com/pme123/camundala/commit/7fa3fa68412bfec2b43a39596654fc55e7e70240)
- Added WorkerGenerator. - see [Commit](https://github.com/pme123/camundala/commit/a098511897000c00e070e71baf60a58b5384626e)
- Some fixes in Project generation. - see [Commit](https://github.com/pme123/camundala/commit/3cf3b69d98f410d4feb0b56470d67df789a04862)
- Working helperProject with Artifactory configuration in initHelperCompany.sc. - see [Commit](https://github.com/pme123/camundala/commit/21dd6f6164ce4d52fd0beebf064bbbcf63ba28eb)
- Added Company Structure Generator to helper. - see [Commit](https://github.com/pme123/camundala/commit/2222cca70f8a7e7904f9ea7a5cbb4af03bede0a8)
- Added OpenAPI Generator to helper. - see [Commit](https://github.com/pme123/camundala/commit/b5ccc55d8e693852745475f5733fd83da4b0cfc6)
- Added BpmnWorkerDsl. - see [Commit](https://github.com/pme123/camundala/commit/65a5a2153c67a3502e758cddf2e932096275940c)

## 1.28.8 - 2024-02-24
### Changed 
- Improved default values in mapping for ModelerTemplGenerator. - see [Commit](https://github.com/pme123/camundala/commit/aa490c49984adba356bfcbfdceffc50acd5abf25)

## 1.28.7 - 2024-02-20
### Changed 
- Some adjustments in ModelerTemplGenerator. - see [Commit](https://github.com/pme123/camundala/commit/cea1cebc68f70291aed4360ece15549d09bd9da7)
- Moved dependencies in ModelerTemplUpdater to dependency folder. - see [Commit](https://github.com/pme123/camundala/commit/d864100366e03f0eb20d98d9d4230922a919830b)
- Added fix to get only the Variable from the process in simulation. - see [Commit](https://github.com/pme123/camundala/commit/a494edac929b8eb506655238246f04bd322b8966)
- Fixed ProjectApiCreator of invoice example. - see [Commit](https://github.com/pme123/camundala/commit/c3a47557f336933c9940c49274a451eff435083a)

## 1.28.6 - 2024-02-04
### Changed 
- Adding Color Adjustment to Camunda Modeler Template Updater. - see [Commit](https://github.com/pme123/camundala/commit/7041cdb22ce0f4c65eed3432a9e94664b060913a)
- Adding Camunda Modeler Template Updater. - see [Commit](https://github.com/pme123/camundala/commit/1465c0d3a1f84dec070b4053aaa4479ea520eb15)

## 1.28.5 - 2024-02-02
### Changed 
- Fixing link in catalog generation. - see [Commit](https://github.com/pme123/camundala/commit/0f9568561c85c935c3e38fe8b42a09becd12b246)
- Fixing and simplifying catalog generation. - see [Commit](https://github.com/pme123/camundala/commit/8a46f059a75ebc6a6f8e426d79d2b9d280f08dbf)
- First Version of ModelerTemplates. - see [Commit](https://github.com/pme123/camundala/commit/d04000e2c02fc9c6af5faa2a0f1f0ad9fbf0543f)
- First Version of ModelerTemplates. - see [Commit](https://github.com/pme123/camundala/commit/4530a5e38c483a09f6808a4b374a126cadf74d44)

## 1.28.4 - 2024-01-26
### Changed 
- Fixed compile errors - see [Commit](https://github.com/pme123/camundala/commit/03b8df36a49ac301c7bb7e90b2aea6ddb54fa0b8)
- Cleanup  Reworked References / Tags and left navigation. - see [Commit](https://github.com/pme123/camundala/commit/dbd07ed2e7756815a09b9c49d37df8e80ad4e9ab)

## 1.28.3 - 2024-01-25
### Changed 
- Fixed Compile errors. - see [Commit](https://github.com/pme123/camundala/commit/dbfabdd2e443453859501d5310d0f1824ae37713)
- Removed Cawemo / added initProject before Reference Creation. - see [Commit](https://github.com/pme123/camundala/commit/6e862c2d04cc1a3ac6c7aa771602fea6361c7779)
- Introduced InConfig as pattern. - see [Commit](https://github.com/pme123/camundala/commit/9fa1008982e946cb4bae8953587c0d545a17958b)
- Working InvoiceSimulation with InConfig. - see [Commit](https://github.com/pme123/camundala/commit/df0b97718e4ab6ec2d52ea87d271e73d8370a49e)
- With extra config object. - see [Commit](https://github.com/pme123/camundala/commit/483f1056df4f910aa0cec4a2f8628fef7b5effe2)
- Merge branch 'develop' into inConfig - see [Commit](https://github.com/pme123/camundala/commit/9257657ba1c3a2622b10047082165c322f13408a)
- Added Doc files. - see [Commit](https://github.com/pme123/camundala/commit/d62a89fb9385fffb6712a678df85dff92799e19b)

## 1.28.2 - 2024-01-22
### Changed 
- Added manualOutMapping / documented GeneralVariables. - see [Commit](https://github.com/pme123/camundala/commit/30e367e6981bdf23d7253a876309c899dcfb81bf)
- Updated Datakurre Plugins. - see [Commit](https://github.com/pme123/camundala/commit/c55c734d292c6c5cea0b346d7ae50e5f6ee3a9d9)

## 1.28.1 - 2024-01-18
### Changed 
- Adjusted Naming and References in ApiCreator. - see [Commit](https://github.com/pme123/camundala/commit/f2f06dc8e6612ca4358cc3621167ede29b66765e)

## 1.28.0 - 2024-01-15
### Changed 
- Handling empty Body in RestApiClient. - see [Commit](https://github.com/pme123/camundala/commit/b4a72209f1093dab50356dbfb1e5fc42f478208f)
- Fixed compilation errors. - see [Commit](https://github.com/pme123/camundala/commit/df2a8258b9036f4bf715bad13c8ec5aafc408196)
- Error Handling for inputs in validation. - see [Commit](https://github.com/pme123/camundala/commit/56b5849ca7ebdcc0e3100faa6e785dbd67b2bce5)
- Added Error Handling to apiUri and inputMapper. - see [Commit](https://github.com/pme123/camundala/commit/4cdd582e7b94db885b0548f85fe5964d8fa6b472)
- Added Test with ServiceOutput List. - see [Commit](https://github.com/pme123/camundala/commit/d0d65cc64f2294e738ac9805b71afa2fbfc87dc8)

## 1.27.1 - 2024-01-08
### Changed 
- Added optional release ResponsiblePerson. - see [Commit](https://github.com/pme123/camundala/commit/37e40f3bbc66082b3aa8d7297dcc331147ec7d86)
- Replaced Encoder/Decoder with InOutEncoder/InOutDecoder. - see [Commit](https://github.com/pme123/camundala/commit/cfedc4f414770370cec267a6545ee47bf926b6d5)
- Replaced deriveEncoder/deriveDecoder with deriveInOutCodec. - see [Commit](https://github.com/pme123/camundala/commit/0314b73bee92c1f536f42a03400cb6127023a0e7)
- Changed derives to deriveInOutCodec. - see [Commit](https://github.com/pme123/camundala/commit/160d4016d05db6a7ba60d9ca1c78fc8d263e48e7)

## 1.26.2 - 2023-12-15
### Changed 
- Changed to MUnit from JUnit. - see [Commit](https://github.com/pme123/camundala/commit/64dc2f8f9f7e240e4e1c9d8869235897cfa62c73)

## 1.26.1 - 2023-12-14
### Changed 
- Fixed StarWarsPeopleDetailWorker. - see [Commit](https://github.com/pme123/camundala/commit/784a94683fe645cc42c2fdf11a60496fbb35ceba)

## 1.26.0 - 2023-12-14
### Changed 
- Added values to the Query Parameters in ServiceWorker. - see [Commit](https://github.com/pme123/camundala/commit/fea0cbb169d388795d9617c8ae80fe9c2ef6b63f)
- Migrating to sbt 1.9 / Diamond architecture > demos example. - see [Commit](https://github.com/pme123/camundala/commit/203ff814a55b4e82515a47c80514ab1413e1611c)
- Migrating to sbt 1.9 / Diamond architecture > twitter example. - see [Commit](https://github.com/pme123/camundala/commit/9c212d482240767d7c0a8a79710f1199f5566493)
- Added READMEs for not maintained modules. - see [Commit](https://github.com/pme123/camundala/commit/aaa72e4754b08ba7e038206d7e23e619934582dc)
- Migrating to sbt 1.9 / Diamond architecture > invioce example. - see [Commit](https://github.com/pme123/camundala/commit/33202b31cd78f0126aaa90474cd82afad28abc3b)
- Fix in camundala-externalTask-generic - handledErrors. - see [Commit](https://github.com/pme123/camundala/commit/b1b659230ed9fc08f2c2fd018b258c45105dac1e)

## 1.25.5 - 2023-12-06
### Changed 
- Adjusted java version to 17 for springboot. - see [Commit](https://github.com/pme123/camundala/commit/82021562f457f53732eb46313e9b3b1a4db7b6d3)
- Added EngineRunContext to RestApiClient. - see [Commit](https://github.com/pme123/camundala/commit/aaa7114fec6c58e9074dc3007be052917d15440f)
- Fixing some Logging in Worker. - see [Commit](https://github.com/pme123/camundala/commit/3da1b1160a96c7b21fae79fbd26f1af224a97347)
- Removed Database from Worker. - see [Commit](https://github.com/pme123/camundala/commit/0cfcafbae045e8e3da351bafe61f36b10af0d493)
- Updated to Camunda 7.20. - see [Commit](https://github.com/pme123/camundala/commit/b3166ed49999369c44d3612548ec646ecfa94ec7)
- Merge remote-tracking branch 'origin/develop' into develop - see [Commit](https://github.com/pme123/camundala/commit/7097099c56aa9d3c71f77e36bded6aee8b7f13e2)
- Switched to only on versioned page for releases in CompanyDocCreator. - see [Commit](https://github.com/pme123/camundala/commit/fc7a5723e00808de7cc09eb6cad76cfc984cdced)
- Switched to only on versioned page for releases in CompanyDocCreator. - see [Commit](https://github.com/pme123/camundala/commit/4c1e10e3a208520a0ae4798efa062439a03cea91)

## 1.25.4 - 2023-12-01
### Changed 
- Added Helper functions to init Config and Mocks in InitWorker. - see [Commit](https://github.com/pme123/camundala/commit/6cb361679ae8ef9af06e62051329b7f45c3d287a)
- Moved toEngineObject to EngineContext. - see [Commit](https://github.com/pme123/camundala/commit/f09fd2baa5167a28c62c4be2e992b063ae9dcb53)
- Removed deprecated mocking descriptions. - see [Commit](https://github.com/pme123/camundala/commit/942d79e408163c337ef4c5517e9d49fc755bddbb)

## 1.25.3 - 2023-11-29
### Changed 
- Adjusted Scala- and Github Actions Versions - see [Commit](https://github.com/pme123/camundala/commit/3f33b08df8f265e7072323fdb69d685675207bff)

## 1.25.2 - 2023-11-29
### Changed 
- Adding types for indepent Json En-/ Decoding. - see [Commit](https://github.com/pme123/camundala/commit/81ea9428310deefb446bb07c78ff696eff4ae1c9)
- Revert "Reduce dependencies to Circe." - see [Commit](https://github.com/pme123/camundala/commit/524ae7bb93b8a5cf2822c79deece203b161df1ab)
- Reduce dependencies to Circe. - see [Commit](https://github.com/pme123/camundala/commit/cde430e60f1a3cf0abb45e5d523a73c1df6551e1)
- Merge branch 'experiments-json-codec' into develop - see [Commit](https://github.com/pme123/camundala/commit/ecb22e61e94b8c9199d3fed3711a51977aa44dcc)
- Fixed examples path. - see [Commit](https://github.com/pme123/camundala/commit/682e8b44b3faedbde5596bc43dfd32c2ee54efb0)
- Experiments JSON codecs - problems in projects. - see [Commit](https://github.com/pme123/camundala/commit/c01cec0d39f9432a0b74ed74cb97c3af217f5129)
- Fixed DMN Tester Example Paths. - see [Commit](https://github.com/pme123/camundala/commit/d8c5a6e27493f02e282094d6b1f018310f88f1c8)
- Added Refined Types in refined. - see [Commit](https://github.com/pme123/camundala/commit/65301e05d4cbba5a7cf9ceb6c3d571244c44b467)
- Fixed BigDecimal in jsonToEngineValue. - see [Commit](https://github.com/pme123/camundala/commit/6c833b9c4099678fdc2a108683553f80292485fd)

## 1.25.0 - 2023-11-21
### Changed
- Added In to outputMapper in ServiceTask to filter the output. - see [Commit](https://github.com/pme123/camundala/commit/542ba9f16d45145d3e5d636fa719c126e0cedd02)

## 1.24.1 - 2023-11-20
### Changed 
- Fix given in auth. - see [Commit](https://github.com/pme123/camundala/commit/ccee0d34b90ac2ac3f1ac99f2081199ddff58b00)
- Refactoring: removed implicits as much as possible. - see [Commit](https://github.com/pme123/camundala/commit/ffb3e07fca8491e39ff16beebac46527355faefb)
- cleanup root level - see [Commit](https://github.com/pme123/camundala/commit/5cf2b138434200f284e3ee2cf3f7eebf0e72cbef)
- fix of documentation in ci - see [Commit](https://github.com/pme123/camundala/commit/b129fc5c12541cc69d16dc242e6d0ad6b9b98f38)

## 1.24.0 - 2023-11-18
### Changed 
- Cleanup Dependency versions. - see [Commit](https://github.com/pme123/camundala/commit/ffcbb5d7c5cae947db51da58c99e3624e218d113)
- Example with Source Link. - see [Commit](https://github.com/pme123/camundala/commit/2cabf1e248b7065d4ca031ca774994023aaf0a00)
- Removed default mocking for Custom Worker. - see [Commit](https://github.com/pme123/camundala/commit/894d45ce19bb8057676976a1a39ddd8e5e9864eb)
- Changed InitProcessWorker to InitWorker - so it can be used for intermediate Events. - see [Commit](https://github.com/pme123/camundala/commit/012de59ffb6b3d45d5fa5a34c8b6352ca954e1bb)
- Updated Laika Document Generation dependencies. - see [Commit](https://github.com/pme123/camundala/commit/d6631dcf48f8ecbfd6d43718bb33b9899745c8f5)

## 1.23.1 - 2023-11-16
### Changed 
- Moved project to layered naming. - see [Commit](https://github.com/pme123/camundala/commit/6940896b96b0f113d580fbf6a45184fd8e3f72d7)
- Moved domain to layered naming. - see [Commit](https://github.com/pme123/camundala/commit/809127a1bb23ee58f5c48cf1fa37e3ab703c514e)
- Added Refined Types (iron) to domain. - see [Commit](https://github.com/pme123/camundala/commit/d9441b8903330390ae76f846ce693659fb7cd8a0)
- Only defaultMocking for Custom- and ServiceWorker. - see [Commit](https://github.com/pme123/camundala/commit/773eaf0452cb65b75f0b2699cb96fb8ef9ed114f)

## 1.23.0 - 2023-11-15
### Changed 
- Removed ServiceIn type from BPMN of ServiceTask. - see [Commit](https://github.com/pme123/camundala/commit/3f546bab6f617789b809c5e8ca70723e58ae436f)
- Fixed Service Mocking without body / made Out not Optional in outMapping / runWork. - see [Commit](https://github.com/pme123/camundala/commit/fd635fc97a40fbac81cce6ccb0b53055ce50af1f)
- MAP-7439: Added header from input in RunWorkHandler. - see [Commit](https://github.com/pme123/camundala/commit/c7393c42a0a66b88f9430145968c6401d6ef502f)
- MAP-7439: Changed servicesMocked to defaultMocked. - see [Commit](https://github.com/pme123/camundala/commit/67471313e158c2f870c20aa39a55eb778f9d01f6)

## 1.22.4 - 2023-11-13
### Changed 
- MAP-7439: Added ProcessStatus for camundala-externalTask-generic / added Simulations for handling Errors. - see [Commit](https://github.com/pme123/camundala/commit/d6407992c6c3bd13c4a7213d795ff7287d8cf02a)
- MAP-7439: Adjusted Worker description / added convenience functions - serviceScenario. - see [Commit](https://github.com/pme123/camundala/commit/54da132ee76978d377264858760b68b9375b2c43)

## 1.22.3 - 2023-11-09
### Changed 
- MAP-7439: Fixing / Cleanup MockedServiceResponse. - see [Commit](https://github.com/pme123/camundala/commit/1628fc2dab4b6c012a3d6e67706f6c1b5e9dfd28)
- MAP-7439: Adjusting validation failure and output-mocking. - see [Commit](https://github.com/pme123/camundala/commit/1e40d3d3e297ab8da4f344c55654a2cf06645e84)

## 1.22.2 - 2023-11-08
### Changed 
- MAP-7439: Renamed camundala-service-generic to camundala-externalTask-generic. - see [Commit](https://github.com/pme123/camundala/commit/384f7d2db94318b30780fd245be5ae7311725e25)

## 1.22.1 - 2023-11-08
### Changed 
- MAP-7439: Added helper Option.toEither / decoupled CamundaHelper. - see [Commit](https://github.com/pme123/camundala/commit/f4c8a38130d395bc4c329751b6d57d390c06a763)
- Made apiUrl in ServiceHandler type-safe in worker. - see [Commit](https://github.com/pme123/camundala/commit/0e4f1067bd189c4be7367a86af69579e646cff2d)

## 1.22.0 - 2023-11-07
### Changed 
- Renamed ServiceProcess to ExternalTask - see [Commit](https://github.com/pme123/camundala/commit/a64a5b33dbe47927fac36648e4056ce4b7bb2988)
- Removed not needed output from proceedMocking. - see [Commit](https://github.com/pme123/camundala/commit/2cee4b5c19a9ba8bc2938365ca79bfa8a1f2b318)
- Fixed default outputMock for services. - see [Commit](https://github.com/pme123/camundala/commit/039b488188697b016c63c0d6d8abba4239fe6a4b)
- Fixed default serviceMock. - see [Commit](https://github.com/pme123/camundala/commit/1f999fa55e8bd539edb695c8559bfc928fa420d0)
- Working Workers. - see [Commit](https://github.com/pme123/camundala/commit/3276075435d7c85d987b2bb580ab4f855e793a25)
- Removed EngineWorker. - see [Commit](https://github.com/pme123/camundala/commit/0adf224ef924d49ae6856fc780034d9826a2a529)
- Cleanup toCamunda Values. - see [Commit](https://github.com/pme123/camundala/commit/e2995f0755d5592b3b6dc01debbc65ccf6862890)
- Worker as Typesafe types. - see [Commit](https://github.com/pme123/camundala/commit/a064723b2d3f7e7481fa2f0833346ec1e8f34ea6)
- Working example. - see [Commit](https://github.com/pme123/camundala/commit/bbf1d5e637e51f826269773cf6aef09988b7f68f)
- State of work. - see [Commit](https://github.com/pme123/camundala/commit/a60fc6c91fa8b3c39954cd7910cd7bcd61816cc3)
- State of work. - see [Commit](https://github.com/pme123/camundala/commit/be7c5231883b467690448286d6c35dd77305cc2e)
- Support unique Workers. - see [Commit](https://github.com/pme123/camundala/commit/d9a95a5b3ce5830183eaffcfd59aa28c86eb55a4)
- Support multiple Workers. - see [Commit](https://github.com/pme123/camundala/commit/cbe94e1c3cab6b5c2f182308a5fbb8caa49dd440)
- Added simple Logging in EngineContext. - see [Commit](https://github.com/pme123/camundala/commit/7cf64773829ed00246d0d5c6020801863f66dd5f)
- Separated between EngineContext and EngineRunContext. - see [Commit](https://github.com/pme123/camundala/commit/61ed4c800365b244cf1b0246c164e1ab86d7e266)
- Moved sendRequest from ServiceHandler to EngineContext. - see [Commit](https://github.com/pme123/camundala/commit/8ecc62ca16bff1d779a7f30162b33c1df052afa5)
- Added Path Variables - see [Commit](https://github.com/pme123/camundala/commit/ffc24c64d36add76b94264a6ea3708d3cf843739)
- Fixing InvoiceSimulation. - see [Commit](https://github.com/pme123/camundala/commit/937e8938c73caccc0275cf9c1f8fb751d32e98c4)
- Adjusted ServiceHandler. - see [Commit](https://github.com/pme123/camundala/commit/99f36a1d42de4e187915adea6800de2de9814779)
- Added ServiceHandler. - see [Commit](https://github.com/pme123/camundala/commit/2e7ae362162c162347b60e0be3ddc6a515fbd165)
- Added InitProcessHandler. - see [Commit](https://github.com/pme123/camundala/commit/403d102473d0c999f59e122db3be0e2f3c47fbea)
- Adjusted ValidationHandler. - see [Commit](https://github.com/pme123/camundala/commit/ea6b9a9f8ef4f870de4827751be07a9799c1aad1)
- Added ValidationHandler. - see [Commit](https://github.com/pme123/camundala/commit/db94ed4b2a80ff0026e92f49889615ac75a05909)
- Cleanup Worker. - see [Commit](https://github.com/pme123/camundala/commit/fcfa752497c1b8b30f04460d0b796517df472db2)
- Cleanup Service Worker. - see [Commit](https://github.com/pme123/camundala/commit/66031ad2ec7a3659314c56d3db28856f7a84f739)
- Working RestService Worker. - see [Commit](https://github.com/pme123/camundala/commit/d2e1d027f5fa36c9a4c4a265a0cea247834e396f)
- Added Runner to WorkerExecutor. - see [Commit](https://github.com/pme123/camundala/commit/2436c907d22ccfe3c2e43930e67aa6608b30c445)
- Added WorkerExecutor and EngineContext. - see [Commit](https://github.com/pme123/camundala/commit/13655769eec62ff712680f17fa4216cf263b3bc4)
- Fixing mockedOrProceed. - see [Commit](https://github.com/pme123/camundala/commit/92122c13c963f0098be38a8ba9fd9012f7066c32)
- Added mockOrProceed. - see [Commit](https://github.com/pme123/camundala/commit/7bd6a580ca9d0cc356df4d997c0fac422fd174da)
- Adjusted simulation.md that ignore does not need a `.`. - see [Commit](https://github.com/pme123/camundala/commit/7341efd816787ae3b3d79e57826dbcd1a7e27efc)
- Changed register to workers in WorkerDsl. - see [Commit](https://github.com/pme123/camundala/commit/9ef203c61f64bf8b1060ad181026997efdd24b5b)
- Added initVaraibles to Workers. - see [Commit](https://github.com/pme123/camundala/commit/772d5816d9103f627f1ad6875e3e9f950c3470fd)
- Added variableNames as filter to register Worker - simulation work. - see [Commit](https://github.com/pme123/camundala/commit/3460ccc5b40aab586cb16e5ee3b2e9f7eb924cc1)
- Added customValidator to Workers - simulation work. - see [Commit](https://github.com/pme123/camundala/commit/e361c595732fffec718ef5fa71ef17465fd677fc)
- Fixed Problem topics not executed. - see [Commit](https://github.com/pme123/camundala/commit/7be3ca1984758930bab24f2dea71340c54cdae9e)
- Fixed Problem topics not executed. - see [Commit](https://github.com/pme123/camundala/commit/aef15b983cbd0e0a5f83c3210317e759a2ee649d)
- State of work Workers from DSL - Problem: topics are not executed. - see [Commit](https://github.com/pme123/camundala/commit/958671ca859f6024ff4846814baafbbebfbc899b)
- Changed to non-Annotated Workers. - see [Commit](https://github.com/pme123/camundala/commit/e4c2f762882f346a182b8f8b710708644a91f5be)

## 1.21.11 - 2023-10-06
### Changed 
- Added DevStatisticsCreator to CompanyDocCreator. - see [Commit](https://github.com/pme123/camundala/commit/054454b55c85b3a40cfe4cd1f8485cfffa8dbb59)
- Fixed links in Release page of MyCompanyDocCreator. - see [Commit](https://github.com/pme123/camundala/commit/48dc676b669f64cbc1f2e4276f49f24542e68a3b)

## 1.21.10 - 2023-10-04
### Changed 
- Small Fix in DependencyGraphCreator. - see [Commit](https://github.com/pme123/camundala/commit/4eeb2935e01ccd3a09460ddd3b56bdff1588c776)
- Adjusted Docs as automatic releasing works now with Github. - see [Commit](https://github.com/pme123/camundala/commit/b1641864a8b4773e7ceec2babacfd3bfbd87f027)

## 1.21.9 - 2023-10-02
### Changed 
- Added organization to root. - see [Commit](https://github.com/pme123/camundala/commit/ea546feba8da8103c1a3188a53cdc8eb04fcd8ed)

## 1.21.8 - 2023-10-02
### Changed 
- Fixed sonatypeProfileName := "pme123" in root. - see [Commit](https://github.com/pme123/camundala/commit/265ed644a3050c29460b520fbab30fb5149b370a)

## 1.21.7 - 2023-10-02
### Changed 
- Fixed sonatypeProfileName := "pme123". - see [Commit](https://github.com/pme123/camundala/commit/76ef1449b8c2bac08bdbf400118ab37571b688ef)

...