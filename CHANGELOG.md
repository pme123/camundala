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

## 1.21.6 - 2023-10-02
### Changed 
- Changed Sonatype credential Host to ThisBuild. - see [Commit](https://github.com/pme123/camundala/commit/8db4859829e0804aede204aa61c273f5285e2c7e)

## 1.21.5 - 2023-09-29
### Changed 
- Added sbt-ci-release plugin. - see [Commit](https://github.com/pme123/camundala/commit/66066dfd5117cb4a8eb07b3b4eb3063d169ed904)

## 1.21.0 - 2023-09-20
### Changed 
- Fixed catalog generation. - see [Commit](https://github.com/pme123/camundala/commit/76501d1a7476511b9d24d88af1876a96b19fb338)
- Added way to start Process as Message or Signal - adjusted CamundaPostmanApiCreator. - see [Commit](https://github.com/pme123/camundala/commit/12598277faf6398cb8510fe82ee2e017164c4046)
- Fix in naming for ServiceProcessApi. - see [Commit](https://github.com/pme123/camundala/commit/393f76c5721b91fe2efee72872fb4f4f93eec650)
- Added possibility to start process with Signal- or MessageEvent. - see [Commit](https://github.com/pme123/camundala/commit/83b33afbd50fa9b7bb35956618cf95ce3471f162)
- Fixed naming ValidaterError to ValidatorError. - see [Commit](https://github.com/pme123/camundala/commit/f662306421c24b5d0e3b33dcc06b94394d321485)
- Simplified servicePath in worker - see [Commit](https://github.com/pme123/camundala/commit/140b9a59f5fdcada8018b57b5a8b0a59398ef946)

## 1.20.1 - 2023-09-12
### Changed 
- Fix, set counter to 0 when checking Process is finished. - see [Commit](https://github.com/pme123/camundala/commit/9fd14d27c29b0ec8085f14e10bf89285a13d3975)

## 1.20.0 - 2023-09-12
### Changed 
- Added better Logging of Failures. - see [Commit](https://github.com/pme123/camundala/commit/ead889e2902de830d35097268bc92b6dc9ddb169)
- Added defaultHandledErrorCodes as default for extractSeqFromArrayOrString. - see [Commit](https://github.com/pme123/camundala/commit/f5022982f6a6361a182829c379ba22e9e34f68df)
- Added check for incidents to tryOrFail in SimulationHelper. - see [Commit](https://github.com/pme123/camundala/commit/e4b037de9ced36ee8343843bb640714ea8a4e89b)
- Optimized retry if process is finished - checks if there was an incident. - see [Commit](https://github.com/pme123/camundala/commit/ea42abf71d3821a94da4498f30a69f4a505a59ab)
- added convenience method for mapBodyOutput / cosmetics. - see [Commit](https://github.com/pme123/camundala/commit/b682c18a70e660256c675910eb3ee561c6d046dc)
- Fix in ApiConfig. - see [Commit](https://github.com/pme123/camundala/commit/de8391bc9aabbfda1cc751321762aa3dad649855)

## 1.19.1 - 2023-09-04
### Changed 
- Adjusted Documentation. - see [Commit](https://github.com/pme123/camundala/commit/1b154ac6c5c5f59a40bd9291f380d482e818a56c)
- Cleanup and configurable PROJECT.conf. - see [Commit](https://github.com/pme123/camundala/commit/58a8a6529206128233fc67243bb09daf4da8cd98)
- Added impersonateUserId to camundaInMap. - see [Commit](https://github.com/pme123/camundala/commit/d9d07eff4a2fccfd394536ddd53c0288a3ea5f71)

## 1.19.0 - 2023-08-30
### Changed 
- Fixing imports / cosmetics. - see [Commit](https://github.com/pme123/camundala/commit/e5c4e0220fa5f3a32141f9741c84ff0eb26df62a)
- Moving from exports to -Yimports. - see [Commit](https://github.com/pme123/camundala/commit/c071247012c278235f1e5a9101651a2a3b8ac121)
- Added general variables to documentation. - see [Commit](https://github.com/pme123/camundala/commit/8a2bc0ec18e38b91a709ff3c7b66f504813bc149)
- Added technologies.md to documentation. - see [Commit](https://github.com/pme123/camundala/commit/5ef0e1386f78889ae13d7316dc60fb6359014991)
- Added Example of Process Validation Worker - see [Commit](https://github.com/pme123/camundala/commit/0064db8e4d75e20f8bf7b1a3739f177f85595500)
- Fixed compile errors. - see [Commit](https://github.com/pme123/camundala/commit/4869587996d29c8037606922e85444c7e05c842c)
- Added Worker to example-invoice-c7. - see [Commit](https://github.com/pme123/camundala/commit/955641017413e4eac55364d31a4ffc3da259ffa1)
- Updated Versions. - see [Commit](https://github.com/pme123/camundala/commit/21850ef330ee6aa79d7c823f40374e9035ce9fee)
- Added Worker Example / added Simulations to verify Workers handled Errors. - see [Commit](https://github.com/pme123/camundala/commit/69fabbdaff735a2f0afcaf14f68a6e1016dd7ee6)
- Documented general variables. - see [Commit](https://github.com/pme123/camundala/commit/959fe2551f6edbbf81f60cbdc3b5c9201261aa86)
- Added missing general variables. - see [Commit](https://github.com/pme123/camundala/commit/5c641c2332992b2b84047c8806da7bbb2eb4aa85)
- Introducing ServiceProcess. - see [Commit](https://github.com/pme123/camundala/commit/fd346aacfc5fcd746319e5582fe081b2599bcccc)
- added generic service process to us with workers - see [Commit](https://github.com/pme123/camundala/commit/3ecba59bcf5ef577931c8481e9136d135990a19c)
- Added ServiceUnexpectedError - see [Commit](https://github.com/pme123/camundala/commit/e51ef34d3ffa88be5b9978fe7c4af3bfd684e665)

## 1.18.1 - 2023-08-14
### Changed 
- Changed Errors to Json. - see [Commit](https://github.com/pme123/camundala/commit/d5797d1465f3e871cba83de5172bf14b33b0a9b6)

## 1.18.0 - 2023-08-14
### Changed 
- Fixing Compile error. - see [Commit](https://github.com/pme123/camundala/commit/3661b7a19a66d5bf7ff127117cffd66430cf18ff)
- Description Experiments / MockedServiceResponse. - see [Commit](https://github.com/pme123/camundala/commit/44d9d37bce5b8fd75e827cccca4d68e2b9f194df)
- Description Experiments / MockedServiceResponse. - see [Commit](https://github.com/pme123/camundala/commit/d01c2003b140ffa902840fae63b22f158f9f197f)
- Added Camunda7 External Worker Support. - see [Commit](https://github.com/pme123/camundala/commit/8ee83d0a53b36a2581ded07f94796ac29c3cde21)

## 1.17.0 - 2023-08-12
### Changed 
- Working MockedHttpResponse implementation - see [Commit](https://github.com/pme123/camundala/commit/74b4e9d63e3a743038cc0cb2ff65519f3217ebe0)
- Typesafe MockedHttpResponse experiment - see [Commit](https://github.com/pme123/camundala/commit/19f936a2f04cca16b6454d861aa25a6a0870a764)
- Moved GenericServiceIn to domain. - see [Commit](https://github.com/pme123/camundala/commit/d93ec5650fd98b02dea39d7fd7fa0a6851db9868)

## 1.16.0 - 2023-08-04
### Changed 
- MAP-7496: Removed dependency to package.conf - see [Commit](https://github.com/pme123/camundala/commit/7ce14fdf57a921de6fbe9e6f1f2647f80fbdb39a)
- Moved CompanyDocCreator from helper. - see [Commit](https://github.com/pme123/camundala/commit/76b076a5ee1b4cd8fe9009e057b91e720a134e29)
- Cleanup Imports - see [Commit](https://github.com/pme123/camundala/commit/911b051ed1a5baba2b234825ef0b9283b9217f37)
- State of work in CompanyDocCreator - see [Commit](https://github.com/pme123/camundala/commit/088ac2e101fc2c0be36f5773ff376127df84f7dd)
- Fixed counter set to 0 where missing in simulations - see [Commit](https://github.com/pme123/camundala/commit/8b4ba72ead3cc7a8d82b3f8bbd1db48864f6ae83)
- State of Work - Cleanup - see [Commit](https://github.com/pme123/camundala/commit/5d21a814a924c9ca93df231d4dc89054185fbaac)
- Start creating Company docs. - see [Commit](https://github.com/pme123/camundala/commit/6d63aa69a849316748c117d564a5178cde7f1b9a)
- Added some catalog.md for testing - see [Commit](https://github.com/pme123/camundala/commit/83fbe3115cdaafc4c772603295ff01a0b615c259)
- Changed order in Simulation - error last - easier to scroll to. - see [Commit](https://github.com/pme123/camundala/commit/793d23d08bdbccff8f3a61de3ced351530003b30)
- Cosmetics. - see [Commit](https://github.com/pme123/camundala/commit/bfbc51ad5eecf4f6f8f96667f1e69ee9078f6e4e)

## 1.15.17 - 2023-06-22
### Changed 
- MAP-7344: Fix for null values in DMN comparing. - see [Commit](https://github.com/pme123/camundala/commit/d77ebeb72a31b8cdce31e81596c31c387a4071dc)

## 1.15.16 - 2023-06-21
### Changed 
- MAP-6880: Added withIn/ withOut functions / fix in JSON check for values that must not exist. - see [Commit](https://github.com/pme123/camundala/commit/4fd38506d71c0aff17ac30d96e670d617572622e)

## 1.15.15 - 2023-06-19
### Changed 
- MAP-6880: Adjusted de/encoding of JSONs. - see [Commit](https://github.com/pme123/camundala/commit/b4797ae7f3631705784507ee83f10aa821391cfe)
- MAP-6880: Added missing image. - see [Commit](https://github.com/pme123/camundala/commit/482ba93c876d49cb252434fab93d22c77e60e7c1)

## 1.15.14 - 2023-06-16
### Changed 
- MAP-6880: Some cosmetics for Mocker and Validator. - see [Commit](https://github.com/pme123/camundala/commit/682a63af41e3a3ca7c57dee24c5ff94fdb75bda8)
- MAP-6880: Added documentation creation to Github action. - see [Commit](https://github.com/pme123/camundala/commit/158538b25b09efc1f1b9f6cf3177a51ba39b92a1)
- MAP-6880: Adding Mocking documentation and example. - see [Commit](https://github.com/pme123/camundala/commit/3d8f9cd070c990a424e9d397ec51e4aaa15782fd)
- MAP-6880: Specified and documented standard mocking. - see [Commit](https://github.com/pme123/camundala/commit/97c6bfa14b3e0a25efa16af5e29cc2af338b30fc)
- Documentation of API document. - see [Commit](https://github.com/pme123/camundala/commit/63bb466b9db751d3b5c94840b41160b36c0f2ac8)
- Documentation of API document. - see [Commit](https://github.com/pme123/camundala/commit/247fb33c5c8937a6c214e580740d8e7ae10e87e6)
- Fixes in documentation. - see [Commit](https://github.com/pme123/camundala/commit/3ee1961dfd329333cfd3526400c6ccb75e76ca2a)

## 1.15.13 - 2023-05-31
### Changed 
- Added _ignore.simulation_ / extended documentation for Simulations. - see [Commit](https://github.com/pme123/camundala/commit/8e7c44950633a530e89b26a21245ee8f8d9303d8)
- Added check to verify that None variables are tested correctly. - see [Commit](https://github.com/pme123/camundala/commit/74e4b922b31c88d00caf49e2fa34cb7bb2b173dc)

## 1.15.12 - 2023-05-23
### Changed 
- Updated ChangeLogUpdater, so it can be used in other projects. - see [Commit](https://github.com/pme123/camundala/commit/5955e24847e5c6bdee0c46cc51f884b21d2297de)

## 1.15.11 - 2023-05-23
### Changed
- Added ChangeLogUpdater - in new helper module - to the publish process. - see [Commit](https://github.com/pme123/camundala/commit/dc27005f107e6e0f5c582722592170e5612677c2)
- Added CHANGELOG. - see [Commit](https://github.com/pme123/camundala/commit/dfc5ea4eb309208edad8a9ad51b7c6bbcdd2358e)

## 1.15.10 - 2023-05-22
**Due to a bad release - 1.5.9, and it is not possible to remove a release from maven central -
I decided to start the semantic versioning now.

Be aware that breaking changes could still occur, but they will be reflected in the version number.
**
### Changed
- Fixes and additions in the documentation - events. - see [Commit](https://github.com/pme123/camundala/commit/2aca78e38cb73243bbc7c8693c2279ecfdf8092a)
- Added TimerEvents to Postman Api and Simulation. - see [Commit](https://github.com/pme123/camundala/commit/7632bbe48445f25b76df629da3f61aa4cd4ac1ef)
- Added TimerEvents to BpmnDsl. - see [Commit](https://github.com/pme123/camundala/commit/c5954b347056b5dbd236507c44c7c5dc4fa9e0a2)
- Added missing !!! in difference message of Simulation's Json checking. - see [Commit](https://github.com/pme123/camundala/commit/c2cf6bd98b132762b7d4ad09506fa809c6e80a46)

## 0.15.10 - 2023-04-19
### Changed
- Added possibility to wait in UserTask before completing it. - see [Commit](https://github.com/pme123/camundala/commit/f874658328f9df829eed34c3d6e803a1709ebacb)
- Added debug request body. in simulation. - see [Commit](https://github.com/pme123/camundala/commit/4972d6b860ac7239493d6ed50a3ad32be1de3cba)
