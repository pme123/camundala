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

## 0.15.9 - 2023-04-16
### Changed

## 1.15.9 - 2023-04-15
### Changed
- Merge remote-tracking branch 'origin/develop' into develop - see [Commit](https://github.com/pme123/camundala/commit/d1ffbc59bcfa85578b789568c95f201b443bc39f)
- Added new jsonCheck, that gives better diff-messages. - see [Commit](https://github.com/pme123/camundala/commit/fbf1c922bdba04f6564b907ed823b762a9fb4b92)
- Added new jsonCheck, that gives better diff-messages. - see [Commit](https://github.com/pme123/camundala/commit/569ae4371c24b18c2ee07044df0fbbf1ddea7f20)
- Added new jsonCheck, that gives better diff-messages. - see [Commit](https://github.com/pme123/camundala/commit/37467c103df9e7343eabd52612ac3b34acd843ac)
- Added catalog optimization in api. - see [Commit](https://github.com/pme123/camundala/commit/487388e2dda7f7ede92d34fbbd4cdc307d75cccd)
- Added Type for api creation. - see [Commit](https://github.com/pme123/camundala/commit/2c8dd10ae017d11a565c1eed68692530612a701d)

## 0.15.8 - 2023-04-13
### Changed
- Added diagramNameAdjuster for API Documentation. - see [Commit](https://github.com/pme123/camundala/commit/3715902fd2b5f71dc21345a7ec52db0b5d5d7e79)

## 0.15.7 - 2023-04-12
### Changed
- MAP-5895: Adjustment for Page navigation. - see [Commit](https://github.com/pme123/camundala/commit/fdf0de222b2fcd67fa674c2fdab4cc5e3bd16c08)

## 0.15.6 - 2023-04-11
### Changed
- MAP-5895: Fix for wrong Title hierarchy. - see [Commit](https://github.com/pme123/camundala/commit/1b31515b206a5725d6f8820a7ac873cf7212de4d)

## 0.15.5 - 2023-04-11
### Changed
- MAP-5895: Fixes in Link Generation. - see [Commit](https://github.com/pme123/camundala/commit/90303fafbb9522e9b93bf740692d658bfea7c1d8)

## 0.15.4 - 2023-04-11
### Changed
- Using Git for Reference Creation / started CompanyDocCreator. - see [Commit](https://github.com/pme123/camundala/commit/d7dbb2c91dfea40c88a421ad55b0d259572d8b73)

## 0.15.3 - 2023-04-04
### Changed
- Supported Option input variables in DmnTesterConfigCreator. - see [Commit](https://github.com/pme123/camundala/commit/bd510d1074d5908c6d4121fa7380d88b17cfd71e)
- Fixes in the dmnTester.md - see [Commit](https://github.com/pme123/camundala/commit/05e2918649b91acf82cb9183754ef830cbf6087f)
- Fixed Demo mistake. - see [Commit](https://github.com/pme123/camundala/commit/d56e75d26f28b9ce11da2458269f9b5c02a577bc)
- Documented, changed testValues key to typesafe (_.name). - see [Commit](https://github.com/pme123/camundala/commit/7a71c25bc31449674eb5695bda2ae0e4156c8a1a)

## 0.15.2 - 2023-04-04
### Changed
- Documented, changed testValues key to typesafe (_.name). - see [Commit](https://github.com/pme123/camundala/commit/898d73ec7b2ef4a78ac675fa3e346f40666395a0)

## 0.15.1 - 2023-04-03
### Changed
- Fixed error, when Server for Dmn Tester was not ready. - see [Commit](https://github.com/pme123/camundala/commit/272e7bc9ad06b1887ca7a3d8dc264fb0344f6976)

## 0.15.0 - 2023-03-29
### Changed
- MAP-6085: Adding to api documentation without inlining for better performance. - see [Commit](https://github.com/pme123/camundala/commit/973af3ac8301c56814f0c991847ea162d8636fa6)
- Using CirceCodec instead of circe Encoder / Decoder. - see [Commit](https://github.com/pme123/camundala/commit/e439df39df696ab8e5f6a59bccde009d9f21538d)
- Migrated to circe's Enum encoding / decoding. - see [Commit](https://github.com/pme123/camundala/commit/6871107b883b080219265574f9caf0f3e7d35f0c)

## 0.14.10 - 2023-03-14
### Changed
- Added uploaded Diagrams in api. - see [Commit](https://github.com/pme123/camundala/commit/077ab3e5a29ee6478df2ef2bb18df1d9a6a70c7d)
- MAP-6574: Changed variables to wrapper DmnVariable. - see [Commit](https://github.com/pme123/camundala/commit/44fdba28e02d2bbb244d219b3e441e2929ea5cfb)
- DMNs: Fixed Date TestCases. - see [Commit](https://github.com/pme123/camundala/commit/e02913c8796176d8cc66e5a93e1df6c5abadff18)

## 0.14.9 - 2023-02-27
### Changed
- DMNs: Added inTestMode. - see [Commit](https://github.com/pme123/camundala/commit/376ca01237e37e28ebde0d7dc65d4f97c4dca443)
- DMNs: Started integrating DmnTester. - see [Commit](https://github.com/pme123/camundala/commit/09e3a85f15f183f08c698dd327e91c0a0a95f791)

## 0.14.8 - 2023-02-22
### Changed
- Simulations: Added statuses _TERMINATED to the finished check. - see [Commit](https://github.com/pme123/camundala/commit/c816ad9c83ea0d8d9b9a2947887fef932d641cd5)

## 0.14.7 - 2023-02-15
### Changed
- Fix in create CamundaVariables for simple enums. - see [Commit](https://github.com/pme123/camundala/commit/b7208c3cda627e418709ceee34a02b260e6066a5)

## 0.14.6 - 2023-02-10
### Changed
- Fix in create CamundaVariables for enums with variables. - see [Commit](https://github.com/pme123/camundala/commit/095da839f9b847ae74129db82a6d23ddb5221698)
- Dmn Tester: Fixed dependency to dmn-tester. - see [Commit](https://github.com/pme123/camundala/commit/3503cee8195d1491cf9a39de87b1efd397b5dac4)
- Dmn Tester: Simple integration - using REST API of tester (using docker). - see [Commit](https://github.com/pme123/camundala/commit/ffdf3503148a7bed046932b89d8997f7fee3c881)

## 0.14.5 - 2023-01-19
### Changed
- Simulation: Implemented concrete waitFor. - see [Commit](https://github.com/pme123/camundala/commit/f8af723e04df3aec5dd2b87c1a6e34208b118cb8)
- Simulaition: Added Error if process is null. - see [Commit](https://github.com/pme123/camundala/commit/67647a0ca13e66222f120db0d2f312b97784ddc3)

## 0.14.4 - 2023-01-17
### Changed
- Added generic Json example to specification / moved toJson to domain. - see [Commit](https://github.com/pme123/camundala/commit/2a5aa47f14f3b1ac0f21f5d498de886025933bb8)
- Fixed bad equals with Enums. - see [Commit](https://github.com/pme123/camundala/commit/692a2bc7c88e31ed9291ceee3a4a47425b98acba)

## 0.14.3 - 2023-01-13
### Changed

## 0.14.2 - 2023-01-13
### Changed

## 0.14.2 - 2023-01-13
### Changed
- Added possibility to use also api(..). - see [Commit](https://github.com/pme123/camundala/commit/bd6e5f46a6e7bbb890b836013d9da926bea9d99b)
- Moved laikaSite to publish script. - see [Commit](https://github.com/pme123/camundala/commit/64c33da92f23d073e02d20b06817e3993440f6b5)

## 0.14.1 - 2023-01-13
### Changed
- Fixed Laika Site - see [Commit](https://github.com/pme123/camundala/commit/41aae34b53ee8f7b0d7364661d4e362b5991d328)

## 0.14.0 - 2023-01-12
### Changed
- Adjusted simulations according to api documentation. - see [Commit](https://github.com/pme123/camundala/commit/21a712d5224df756d928b0089c7346c070e9fef6)
- Adjusted api documentation with just varargs. Documented API documentation. - see [Commit](https://github.com/pme123/camundala/commit/df7dcbf0eff8004ad06c74248041a4610526dd86)

## 0.13.0 - 2023-01-11
### Changed
- Fixing laikaSite problem - see [Commit](https://github.com/pme123/camundala/commit/17628fa8ccedb4ddea2483d4ae51621ec54457ca)
- Fixing laikaSite problem - see [Commit](https://github.com/pme123/camundala/commit/4335d50fecac73def4cf44729910030c4eb1f2c2)
- Fixing laikaSite problem - see [Commit](https://github.com/pme123/camundala/commit/cdf452df8088208951b08eb4183d76b347b60e4d)
- Adjusted TestOverride Logic / documenting simulation. - see [Commit](https://github.com/pme123/camundala/commit/e63db236379d26722712ce195a1f7228ed108c49)
- Adjusted ignore scenario functionality / documenting simulation. - see [Commit](https://github.com/pme123/camundala/commit/d1d3ff268917870babaf507347d78753583325e6)
- Fixed compile error. - see [Commit](https://github.com/pme123/camundala/commit/d6619e9d847bb7be42b93aec6364dd9405781b35)
- State of work documentation Simulations. - see [Commit](https://github.com/pme123/camundala/commit/dfaa84b55d8234628680434bc38452ae3798e9c3)
- Cleanup in twitter example. - see [Commit](https://github.com/pme123/camundala/commit/13806b32955aac803414af875a788eb53a638ff4)

## 0.12.0 - 2022-12-19
### Changed
- Migrated to Tapir 1.2.4. - see [Commit](https://github.com/pme123/camundala/commit/bc7a8a1688986ba0b57ebca99a926b3947782ccb)
- Fixing compile problems / warnings. - see [Commit](https://github.com/pme123/camundala/commit/dfb5c2833a52135a28747a3c39d4d0221922d169)
- Removed Gatling. - see [Commit](https://github.com/pme123/camundala/commit/4d0f787d1e090d29cb15770f6d0fa539230ec21c)
- Migrated Camunda Versions. - see [Commit](https://github.com/pme123/camundala/commit/61c61eef61bdb790b6e39f478b4a5e4058bc50ef)

## 0.11.0 - 2022-12-17
### Changed
- Added Authentication to the Custom Simulation. - see [Commit](https://github.com/pme123/camundala/commit/8b1916abb8fdb564523d61b01c00554da349b1e3)
- Fixing missing json Simulation / check compatibility with Gatling Simulation. - see [Commit](https://github.com/pme123/camundala/commit/326d276df3568e17163005e95a7848335b58ce75)
- Remove simulation value so it is compatible with gatling. - see [Commit](https://github.com/pme123/camundala/commit/d77a06cbb9c3ade7f7a5f61d4c867d753a571d0c)
- Added BadScenario to the simulations. - see [Commit](https://github.com/pme123/camundala/commit/b5b5974dcc31629bc060997bae823e9c88b4da65)
- Added timing to the simulations. - see [Commit](https://github.com/pme123/camundala/commit/c35f00023d647f21bdcdab4e1ab6f0d23af6a762)
- Added sub-processes to the simulations. - see [Commit](https://github.com/pme123/camundala/commit/ced6d4363455a853ab0cf9936d4dafb853bc84fb)
- Added intermediate message / signal events to the simulations. - see [Commit](https://github.com/pme123/camundala/commit/687ed5c7df70ecf495679d5707f367a1d2680a38)
- Added start with Message to the simulations. - see [Commit](https://github.com/pme123/camundala/commit/d925696739c1f0d6b4466b2ced2e94a2c6388b7b)
- Preparing signal and message start - see [Commit](https://github.com/pme123/camundala/commit/37e8570ca685bc141c834a0f3a39cd61df4d7f7a)
- Added result printing for CustomSimulation. - see [Commit](https://github.com/pme123/camundala/commit/f741b7fe9682f9db412bd3ee659ff90d62e76c11)
- Introduced sbt Testframework for CustomSimulation. - see [Commit](https://github.com/pme123/camundala/commit/f6113787466f58b72368e5a75061226162076afe)
- Implemented IncidentScenario. - see [Commit](https://github.com/pme123/camundala/commit/97031c4b5a8f1d3668f47c6e48fa2797fe2f078c)
- Implemented Process check variables. - see [Commit](https://github.com/pme123/camundala/commit/283d7eb71e34d6f778efff7cd83f82d506268007)
- Implemented Process finished. - see [Commit](https://github.com/pme123/camundala/commit/a6831fba617ef10c7f6ac4b2121cdb0d84c51b34)
- Implemented UserTasks for CustomSimulation. - see [Commit](https://github.com/pme123/camundala/commit/22f705f4ce1989a920907c62f0ef8f93af59f2cf)
- First start with Custom Simulation. - see [Commit](https://github.com/pme123/camundala/commit/95b9164e2eaac89d7b9bcea631ff4a2897fa6092)
- Updated examples for gatling extraction in simulations. - see [Commit](https://github.com/pme123/camundala/commit/4b233d6b2474938c90d3ff16aa122ca6a30d6311)
- Extracted Gatling specific functionality. - see [Commit](https://github.com/pme123/camundala/commit/8a64c5728f7f38e259f2bd3a24f2134105f34bdd)
- Extracted Gatling specific functionality. - see [Commit](https://github.com/pme123/camundala/commit/f785a7349dceb1b1262ab2845a0a7de78f3a4c85)
- Fixed Simulations in invoice examples. - see [Commit](https://github.com/pme123/camundala/commit/0e4775961e0eaf4c211cef39ef2edf717a156b55)

## 0.10.4 - 2022-11-08
### Changed
- Dmn Encoding work in progress. - see [Commit](https://github.com/pme123/camundala/commit/09992fddef3655afc6731ac982d3b2accf9bc2c1)

## 0.10.3 - 2022-11-06
### Changed

## 0.10.2 - 2022-11-06
### Changed

## 0.10.1 - 2022-11-06
### Changed
- Fixed encoding of DmnValueType. - see [Commit](https://github.com/pme123/camundala/commit/c1200c00a405bc67534b1c243a6cd8e98c23e215)
- updated sbt / scala version - see [Commit](https://github.com/pme123/camundala/commit/fb3d093cab0216100eee723aceff9afb8f3ef1ce)
- moved DmnValueType encoder to DmnValueType object due to clash with adt Encoder. - see [Commit](https://github.com/pme123/camundala/commit/10126fcf7a00caf1cb34b70e970e9ee7aa786e6e)
- Document BPMN DSL. - see [Commit](https://github.com/pme123/camundala/commit/faf5d47853dc2617b2e45231158f6be3d3dc07a6)
- Removed experimental features: unit tests / bpmn generation / mapping. - see [Commit](https://github.com/pme123/camundala/commit/ea4259ab7dce66d8d82523a341a3b28ff5d07ff9)
- MAP-5823: Started BPMN DSL documentation. - see [Commit](https://github.com/pme123/camundala/commit/dd59b840ce497d0375a12d570d39e0a25e055e7f)

## 0.10.0 - 2022-10-14
### Changed
- MAP-5897: Added uses DMNs in processes. - see [Commit](https://github.com/pme123/camundala/commit/bb26679363371abf91042180913cf3a15d7c983f)
- MAP-5897: Added used by to DMNs. - see [Commit](https://github.com/pme123/camundala/commit/d0283c3e445bdcb383f590c9c7203ec6a764ac19)
- Added _specification.md_. - see [Commit](https://github.com/pme123/camundala/commit/f1b4a2cfefcdb8ff6f7e4fdb70e7bcdd6a26fc11)
- Fixed compile problems. - see [Commit](https://github.com/pme123/camundala/commit/fe121d89c713ead53971f67d9382895cb018e424)
- State of work documentation / problem with separate domain project! - see [Commit](https://github.com/pme123/camundala/commit/5cea16d5801ef550ab6ef4145f0029903b025918)
- Added laikaSite generation to Github Actions. - see [Commit](https://github.com/pme123/camundala/commit/d43ec0aa93c7cca5cacd20443d6a69f6e75df562)
- Documentation: Design and Technology. - see [Commit](https://github.com/pme123/camundala/commit/fc6ab2164bc9ab09f2ff1c715395cde94a4f1cfe)
- Documentation: Design and Technology. - see [Commit](https://github.com/pme123/camundala/commit/cd150d9469b93f007b34c9eb98a281bba79dc123)
- Documentation: Design and Technology. - see [Commit](https://github.com/pme123/camundala/commit/a5555e1cf9681c1c344bb0f70c6e597881ee0c48)
- Camunda 8: Migrated External Task. - see [Commit](https://github.com/pme123/camundala/commit/1fbccbc1f92e79751ee0039c2939929bb76c0bb3)
- Camunda 8: finishing migrating UserTasks / Migrated Call Activity. - see [Commit](https://github.com/pme123/camundala/commit/c37ad16dea238f9f4cce650c4aeeec9c8b8a6187)
- Documentation: Small Intro. - see [Commit](https://github.com/pme123/camundala/commit/2e698e1b83efd1a9bda186dd0d28050236e743c8)
- Documentation: Test Laika Doc change folders. - see [Commit](https://github.com/pme123/camundala/commit/ab658c79a9e5f4638d0a9ef92e8438c27c7c56de)
- Documentation: Test Laika Doc. - see [Commit](https://github.com/pme123/camundala/commit/ea5e7bebdce6217744f7d28c7b0bd9f6af7b6e23)
- Documentation: Test index.html - see [Commit](https://github.com/pme123/camundala/commit/7eccba68c670d51964d071a7a38883db8ee7aa93)
- Camunda 8 - created forms / UserTasks. - see [Commit](https://github.com/pme123/camundala/commit/37a3f3c566e380211895731ac0d7e2744ce45718)
- Camunda 8 - create sync createProcessInstance. - see [Commit](https://github.com/pme123/camundala/commit/e71dbfad08407a109b26b6280c844692e6639874)
- Fixing failed Tests. - see [Commit](https://github.com/pme123/camundala/commit/3d842c3b165f67b224e5c45d6e06a8a7d74881fc)
- Migration Camunda8: Integrated invoice example DMN. - see [Commit](https://github.com/pme123/camundala/commit/246e922e1861f51034f40eee06bcec834a93f018)

## 0.9.0 - 2022-08-15
### Changed
- Small adjustment Union Type to Option in descr. - see [Commit](https://github.com/pme123/camundala/commit/3e2b68e07d252c2446849181f04f7494e1468cdc)
- Added DecisionDMNs as main api object. - see [Commit](https://github.com/pme123/camundala/commit/43a40a477ff55e12ba05bcf663dc3b4a6d6f9d1b)
- Added missing invoice.v1.bpmn for java test. - see [Commit](https://github.com/pme123/camundala/commit/9de49a9e78340940c3bbc68c5ae1ff9f8158e588)
- Started migrate Invoice example to camunda 8. - see [Commit](https://github.com/pme123/camundala/commit/cbc3dc64200a34eaefb5665ad1db5577a5b834b5)
- Added TestOverrides for DMNs. - see [Commit](https://github.com/pme123/camundala/commit/edace2ea0eb20dfbe6e85b32dd5de4bad956af56)
- Fixes for running DMN Scenarios. - see [Commit](https://github.com/pme123/camundala/commit/185653bad54a3fca4e4c96ecdaeffe934954e4ed)
- Added Optable for Options in the DSL. - see [Commit](https://github.com/pme123/camundala/commit/90d7782b073c2e25c24fb38fe2c6f337629b3fda)
- Added DMNs to simulations. - see [Commit](https://github.com/pme123/camundala/commit/60a0ae388b53a327c24d6982eb741e3a903991f5)

## 0.8.1 - 2022-07-29
### Changed
- Added descriptions for Camundala standard attributes. - see [Commit](https://github.com/pme123/camundala/commit/e9d5097aa484e28e217cd5fea67642b5cb759120)

## 0.8.0 - 2022-07-28
### Changed
- MAP-5800: Added BPF Services. as optional ApiCreator. - see [Commit](https://github.com/pme123/camundala/commit/636d967b4b2364536043b82475f80dd0269dfbcd)
- Fixed compile problems - see [Commit](https://github.com/pme123/camundala/commit/790b50db0ab371ccdad3c6bfe9d96e44b15aa197)
- Camunda 8 experiments with Twitter Example. - see [Commit](https://github.com/pme123/camundala/commit/a9c445f45899ebcf1af4ce73e206b371274a6b1e)

## 0.7.0 - 2022-07-15
### Changed
- Adjusted groups to behave like document. - see [Commit](https://github.com/pme123/camundala/commit/f060688c02ab9d6033183e8a7e8ebb649248c17d)

## 0.6.3 - 2022-07-14
### Changed
- Fixes for groups of Processes. - see [Commit](https://github.com/pme123/camundala/commit/88f60ea3d4ca79cfd4d6bf4256f019a9fa2098e4)

## 0.6.2 - 2022-07-13
### Changed

## 0.6.1 - 2022-07-13
### Changed

## 0.6.0 - 2022-07-13
### Changed
- Fixes for Generic Services in API Generation. - see [Commit](https://github.com/pme123/camundala/commit/b05376b06bf0e46874b0e818b97464bf982ce185)
- Refactoring ProcessReferenceCreator - see [Commit](https://github.com/pme123/camundala/commit/9131d091971e759bf33cffccbd264ae0846f4c9a)
- Introduced GenericServiceIn to distinguish them - different behavior for looking for references. - see [Commit](https://github.com/pme123/camundala/commit/94b117d85f391960b1c47e64bd766478d2cbdd49)
- Fixing compile problems. - see [Commit](https://github.com/pme123/camundala/commit/b1a1a47bfec4b2a4cb1ee6151c5fae3fb920724e)
- Adjustments after first API migrations. - see [Commit](https://github.com/pme123/camundala/commit/c03a134b77b101a5b14900b4cc79099870c36f9c)
- Adding Postman APIs for Events. Migrated Examples. - see [Commit](https://github.com/pme123/camundala/commit/9e8b7a9f6484e4cdfadc1ad5e558ed0cb05e3444)
- Adding Postman APIs for UserTask and DecisionDmn - see [Commit](https://github.com/pme123/camundala/commit/35d05522895df8dfa67392cf8c0d6dfb8d5367a8)
- First new Postman API implementation - see [Commit](https://github.com/pme123/camundala/commit/4b6532e8ba3b19f7dce01f71ba6b59b9d2ddc254)
- First new API DSL - see [Commit](https://github.com/pme123/camundala/commit/23ec970f0fcfc2adaff659c69d4cf0823aaf0388)
- Fixing example simulations. - see [Commit](https://github.com/pme123/camundala/commit/fe6652c0a9bd74a6c4440b094c7cbc61cf7fd050)

## 0.5.0 - 2022-06-21
### Changed
- Added way to start SReceiveMessageEvent as StartEvent. - see [Commit](https://github.com/pme123/camundala/commit/0d48b585a1a387f60e19c79d8213cf9a8b3578c7)
- Added ignored / IncidentScenario. - see [Commit](https://github.com/pme123/camundala/commit/60754d296d75eefacdaf49791a0956c4246607df)
- Added ReceiveSendEvent / ReceiveMessageEvent. - see [Commit](https://github.com/pme123/camundala/commit/f260a8089324cd4aa2985f58dd47cc8c3e2b9f15)
- Splitted api to bpmn / dmn / api - see [Commit](https://github.com/pme123/camundala/commit/1f1e98de91f9cb5ec38d5ca452dbcb137d2b31a8)
- Introduced simulation -> version 2 of gatling Simulations. - see [Commit](https://github.com/pme123/camundala/commit/9cb8d1041c1917a2b9ef0610dbc8aef6d7c537df)
- Uses References: Adding defaults if there are no results. - see [Commit](https://github.com/pme123/camundala/commit/a480122f86647dc32d730257e2ecfb419f9b9364)
- Fixed failing compilation. - see [Commit](https://github.com/pme123/camundala/commit/93328031dc0a324e6272b0839fcb48852186b87c)
- Fixed failing compilation. - see [Commit](https://github.com/pme123/camundala/commit/db0a0aeb5d39508d7a10f462090a90a31c9902ac)
- Added Uses References to API documentation. - see [Commit](https://github.com/pme123/camundala/commit/11263e24e38a7ab88781c7a5d3e4243c599c6b03)
- Added JIRA Links for Changelog. - see [Commit](https://github.com/pme123/camundala/commit/6a2b107a37f4ac77312bec874819d48aa91a0a0c)
- Fix in checkIncidents. - see [Commit](https://github.com/pme123/camundala/commit/dc90aa6cd8996f9375b2800bdb9fccdcfff07483)
- Added Camunda Community Link. - see [Commit](https://github.com/pme123/camundala/commit/b090764d8af4d46430caa7dc9a5b9731b0aaf8e5)
- Added checkIncidents. - see [Commit](https://github.com/pme123/camundala/commit/435b907327211780476438ef0dcb10f559a98157)
- Added waiting some Time to simulate cancel. - see [Commit](https://github.com/pme123/camundala/commit/f7f3cbc9b2658552bda867697ba1054c84a6b7a1)
- Adding Examples to Processes withInExample / withOutExample - see [Commit](https://github.com/pme123/camundala/commit/e361b96133cab419e0738806e9425c0d1ade98fb)

## 0.4.0 - 2022-04-26
### Changed
- Moved NameFromVariable to api. - see [Commit](https://github.com/pme123/camundala/commit/af52106725dcb7ed493230003e8f4900ed347cf7)
- Fixed Problem with given and asJson - using companion object. - see [Commit](https://github.com/pme123/camundala/commit/00f09e528857117c9ffbff702ea0c22b972b420d)
- Fixeed bad assert version - see [Commit](https://github.com/pme123/camundala/commit/9461d5bb12d668c54a892a4fa1c345f271ac6a29)
- Cosmetics / replaced implicit with given where possible. - see [Commit](https://github.com/pme123/camundala/commit/85a3702990e5578058c12fc27f9e2079b4c2102a)
- Improved Reference Matcher. - see [Commit](https://github.com/pme123/camundala/commit/e34d35de95914e6b4c10d33fddeecbe7d415221e)
- Added Twitter Example for Camunda 8. - see [Commit](https://github.com/pme123/camundala/commit/fbcbe94de792961c1ebb83855bb93d94d8bf9ded)
- Fixed TwitterApiCreator for c7 and c8. - see [Commit](https://github.com/pme123/camundala/commit/93eefbc1bf5b3e0757977628a138006c773eca2d)
- Added Twitter Example for Camunda 8. - see [Commit](https://github.com/pme123/camundala/commit/118eb2c7616760e3beb0eaa331765e7dbf7ae212)
- Splitted Twitter example to Camunda 7 / Camunda 8. - see [Commit](https://github.com/pme123/camundala/commit/cc10dbad88e4de4cd4f1a33c9c06f1e005a2d448)
- Cosmetics in api. - see [Commit](https://github.com/pme123/camundala/commit/f9c3880305efef4cd69d670eaa4f9e8627430731)
- Improved Check messages to expected values. - see [Commit](https://github.com/pme123/camundala/commit/59aba0f583c1afff9f572ea9e636e528342eb9a7)

## 0.3.10 - 2022-03-22
### Changed

## 0.3.9 - 2022-03-21
### Changed
- Added References to processes that uses this process. - see [Commit](https://github.com/pme123/camundala/commit/23085d97309d651ae3283bd6fc2bf59d8702e9ee)

## 0.3.8 - 2022-03-21
### Changed
- Fix in Postman API. Use definition key in path. - see [Commit](https://github.com/pme123/camundala/commit/6f1a0a86a5a382267e60fe57916429fa75cf7ed1)

## 0.3.7 - 2022-03-17
### Changed
- DSL improvements / started better mapping example - see [Commit](https://github.com/pme123/camundala/commit/ac78ccd03495b7b5e8872e09e88b65e9a1681da5)
- DSL improvements - see [Commit](https://github.com/pme123/camundala/commit/d555cd2e9a603f03cf819f9f5bbc465f4a3145d2)
- Migrated to tapir 0.20.1 - see [Commit](https://github.com/pme123/camundala/commit/86a30adfaeea3a8cb1ac09c0b7c2e6e5b43a5d35)
- added executionCount to allow to run a Scenario multiple times. - see [Commit](https://github.com/pme123/camundala/commit/65438baeb79660165594b677e22e8589ff95d4bc)
- Moved the Listener from the generated. - see [Commit](https://github.com/pme123/camundala/commit/94ac6ff601823ce655b8e633252641f4b1c4c736)
- Fixes in dmnTester. - see [Commit](https://github.com/pme123/camundala/commit/25edc4b928d97e2dc1f19bf239080508cbd728de)
- Fixed Validator. - see [Commit](https://github.com/pme123/camundala/commit/6bce523149d6f2bde448fada03b6af168fb77041)
- Fixing broken unit test - see [Commit](https://github.com/pme123/camundala/commit/629eaa3798fb8f4a9ca734d66eceef8d8b69c201)
- Fixed Mocking CallActivity. - see [Commit](https://github.com/pme123/camundala/commit/fb84ad65e5e7217e39383e14f815e022454aa35f)
- Added Subprocess to Simulation. - see [Commit](https://github.com/pme123/camundala/commit/3ebe3fee96f6605230756866ed64a8d53a3d2d5e)
- Changed Names to readable Labels / adjusted ignore possibility. - see [Commit](https://github.com/pme123/camundala/commit/c5cb940da4a60d081da0042af3597b12e12d70ce)
- Added automatic Scenario Naming through variable name. - see [Commit](https://github.com/pme123/camundala/commit/b0e6315ec2d8906a056e2df48a5d942a1e0aa468)
- Added Load Testing - see [Commit](https://github.com/pme123/camundala/commit/b23b95d762a3db15f19476b5ee955019499f0aea)

## 0.3.6 - 2022-02-24
### Changed
- Fixed problems with branch. - see [Commit](https://github.com/pme123/camundala/commit/b038388fc3d62ed73df87b82c89b8779bf4e51cc)

## 0.3.5 - 2022-02-24
### Changed
- Fixed wrong github url - see [Commit](https://github.com/pme123/camundala/commit/79f2b94045eca06e1ca432def46a7e4e09583b97)
- Added implicit endpoints for processes. - see [Commit](https://github.com/pme123/camundala/commit/a751aac5867d7bf00db1d348f4b841fd6c066b30)
- Added implicit endpoints. - see [Commit](https://github.com/pme123/camundala/commit/b0fdee988ede5d85af9b929c2053a90a1a20bff8)
- Cosmetics - see [Commit](https://github.com/pme123/camundala/commit/12ac93d28f27d3f219fa9790d58a223a4f092a36)
- Fixes for OAuthSimulationRunner - see [Commit](https://github.com/pme123/camundala/commit/55198d3db0c881dd0f497cd2e563d7592266673a)
- Fixing Compile Problems - see [Commit](https://github.com/pme123/camundala/commit/6537bb7fc8d1dd8bc7abf3b8bdbca4cb6466979d)
- Refactoring SimulationRunner - see [Commit](https://github.com/pme123/camundala/commit/6277c391af982cbe77c893bee9b0f766392255bd)
- Added Validation Example. - see [Commit](https://github.com/pme123/camundala/commit/83922fe1ad7d138815253e16ee86f0ab80bbdd6d)
- Added Map and Iterables to objectToCamunda. - see [Commit](https://github.com/pme123/camundala/commit/518d69e5de3f6276b3793079e7adb87e53d21b29)
- Improved SimulationRunner - added checkRunningVars - see [Commit](https://github.com/pme123/camundala/commit/ecf2d1b1265ca22b80350f7abc075f8c14a82d64)
- Finishing DMN Examples with result variable 'result' - see [Commit](https://github.com/pme123/camundala/commit/61cdf6613a8f2fc259384ba1b75ffaba9ad11318)
- Added CollectEntries to simplify DMNs with CollectEntries. - see [Commit](https://github.com/pme123/camundala/commit/adea5e35db560b1f8bfa0b340921ef74125f2a19)
- Added SingleEntry to simplify DMNs with SingleEntry. - see [Commit](https://github.com/pme123/camundala/commit/755aa1fe65a3c53aa4df9a9a9d83140ad823e176)
- Added ResultList to simplify DMNs with ResultList. - see [Commit](https://github.com/pme123/camundala/commit/951f4f854d651bb2257270e25f59fed42015de4e)
- Added SingleResult to simplify DMNs with SingleResult. - see [Commit](https://github.com/pme123/camundala/commit/d84d762b1dd6fb3401206f7eae4d37d761d465ae)
- Merge branch 'develop' into dmn-singleResult - see [Commit](https://github.com/pme123/camundala/commit/19c3ef307f274862360fb0f27f237f9775e6f760)

## 0.3.4 - 2022-02-08
### Changed
- first try - not ready / working - see [Commit](https://github.com/pme123/camundala/commit/d0092496179ea49a33cb35d9de1e64f15f606c51)
- Added Cawemo to the endpoint documentation 2. - see [Commit](https://github.com/pme123/camundala/commit/37cadad548151a1444e15c4038549f889e976182)
- Added Cawemo to the endpoint documentation. - see [Commit](https://github.com/pme123/camundala/commit/8ce90f023974385b03242ce3a496dec5acd1512b)
- Fixes in SimulationRunner and api/model - see [Commit](https://github.com/pme123/camundala/commit/d50d04d1ad70214574093c38f39b843974ac3e65)
- Mapping - state of work. - see [Commit](https://github.com/pme123/camundala/commit/57f097155abd39df6d699f776c840fb5c4a00cdc)
- Added Option and List example to mapping. - see [Commit](https://github.com/pme123/camundala/commit/39b4701f72141c85bc0b5c1780d6ab0225626345)
- Checked Scenario Testing - see [Commit](https://github.com/pme123/camundala/commit/5d69bf3eec0f4f35dad0cdabd728b518e231035f)
- Improved Unit Testing - see [Commit](https://github.com/pme123/camundala/commit/8971eda2186dfb36609492e276de9cfe5f1e5753)
- Updates to API Generation. - see [Commit](https://github.com/pme123/camundala/commit/54421dd0471797fbe0cc322e0e99a8885565871c)

## 0.3.3 - 2022-02-01
### Changed
- updating Unit tests to handle json as inputs. - see [Commit](https://github.com/pme123/camundala/commit/5f7dfa7ae67a2beb794fe3a74156d35bb0c340f9)
- fixing failing tests. - see [Commit](https://github.com/pme123/camundala/commit/165d90edbcb9d5c6cfce424f03ac198ce1ad3213)
- fixing compile error - see [Commit](https://github.com/pme123/camundala/commit/4fa18368239c9cc549e7b19685294d4ce9424ecb)
- first running TestSimulation with in and out - see [Commit](https://github.com/pme123/camundala/commit/ae4923c3883286d787ca660b848c5b2058c0faf9)
- first running TestSimulation - see [Commit](https://github.com/pme123/camundala/commit/dfc093d3193711509f3f8197a0d10bcd9cd7dc7b)
- first test with expression mapping. - see [Commit](https://github.com/pme123/camundala/commit/b3f55dcc34c57d6d1c530bf16380750da02499fc)
- Fixed missing Dependencies. - see [Commit](https://github.com/pme123/camundala/commit/811f4b9caee0b4e5e224f793f935d672fdfb44c5)
- Created Demos Example - see [Commit](https://github.com/pme123/camundala/commit/b99827db8fa724d34a670ca10adfe70901cf807a)
- Added SendMail to API Doc. - see [Commit](https://github.com/pme123/camundala/commit/94c23430f31b2140642a38de6a00e026dc81fbbb)
- Fixed failing Postman Generator. - see [Commit](https://github.com/pme123/camundala/commit/7fa3edb89f431d06a14742ac04de4a7613c518f3)
- Added CorrelateMessage to Process. - see [Commit](https://github.com/pme123/camundala/commit/620bc47d5b6168c8e672e5c3654b98c844097454)
- Added SendSignal to Gatling Simulations - see [Commit](https://github.com/pme123/camundala/commit/0489a52957698d8a9fb636fb63563955e1603023)
- Added dates as DMN data types. - see [Commit](https://github.com/pme123/camundala/commit/c44dc993d9928dfb544bc7eb86175698f3814284)
- Added ReceiveMessageEvent to SimulationRunner - see [Commit](https://github.com/pme123/camundala/commit/e7b2b6a4f7a02d113522f6da11130d6f8539db1e)
- Commented Postman - Problems with Types. - see [Commit](https://github.com/pme123/camundala/commit/f9b5ac4b1f07a5b43592402929c522bccef7a49f)
- added CorrelationMessageIn - see [Commit](https://github.com/pme123/camundala/commit/33cb2a19c016424837133bf102cce98da62eefcb)
- Adding Test for Generating / Mapping Camunda. - see [Commit](https://github.com/pme123/camundala/commit/e95db7bdc304f9a64c085e469230d6219b96ca9e)
- Fixed compile problem - see [Commit](https://github.com/pme123/camundala/commit/e2fac05d621ddda91cba8ea282d6aa621802932e)
- First dummy input mapping. - see [Commit](https://github.com/pme123/camundala/commit/ea32309bfd13bc496c09f4967efa3dc2b09dd3aa)
- Fixed reviewInvoice. - see [Commit](https://github.com/pme123/camundala/commit/0f1bc949609721817c4e0ad112df5805c3256e38)
- First out-mapping example. - see [Commit](https://github.com/pme123/camundala/commit/4211df3feddb7409b0d0646a35e1fec74a7d96b9)
- working macro. - see [Commit](https://github.com/pme123/camundala/commit/556680f0769132ad87164aac624f95cd2d747125)
- first mapping macro. - see [Commit](https://github.com/pme123/camundala/commit/0d797adca4b09f7fe8bff149156bc6bc9781b622)

## 0.3.2 - 2022-01-22
### Changed
- removed enumDescr - done by Tapir automatically. - see [Commit](https://github.com/pme123/camundala/commit/cf9c802631034640b4315a8991b82df8cd5bd182)
- moved gatling to semiauto derivation - to improve performance. - see [Commit](https://github.com/pme123/camundala/commit/d1cc890bbbf50cb1fae0f7cc0531af51751a942a)
- switched semiauto derivation - to improve performance. - see [Commit](https://github.com/pme123/camundala/commit/13b6ce1caa0278685b3b1b1c7ea448c649d3b10d)
- added module camunda to the project - pimp your bpmn. - see [Commit](https://github.com/pme123/camundala/commit/eac25b33c3f1b7b4079670d90544a469d66c4cfd)
- moved to newest spring boot version. - see [Commit](https://github.com/pme123/camundala/commit/d9b0541fb982ec8854cd753dbf2bb5a7aa23321e)
- Adjustments - see [Commit](https://github.com/pme123/camundala/commit/dad02f0a7b2743b50040239da4e24406d5db71cd)

## 0.3.1 - 2022-01-16
### Changed
- Added minimal README.md - see [Commit](https://github.com/pme123/camundala/commit/23e98d8b5a53e2a09c3d47defa82399a4468bc2c)

## 0.3.0 - 2022-01-16
### Changed
- Added endpointType to summary / name. - see [Commit](https://github.com/pme123/camundala/commit/42b3073d6bf5d75c5e8d3d86882b0182945d8997)
- Added scenarioName to start and check process. - see [Commit](https://github.com/pme123/camundala/commit/bffb0ebe01542793b3fc7cb05875c2161f7756cb)
- Adjusted version - see [Commit](https://github.com/pme123/camundala/commit/452e288a2ad55be2726dabde23235dc5566fae4b)
- Splitted to modules test and gatling - see [Commit](https://github.com/pme123/camundala/commit/852315a27d1d9f5a858b1a48062240691bfec135)
- Fixing failed Test Compile - see [Commit](https://github.com/pme123/camundala/commit/fab6b0da3fb247d942ed7ee76c34490d2d4bd821)
- Simulations are working. - see [Commit](https://github.com/pme123/camundala/commit/299bc5ae8753247b233b4af176dde9c5c04323b1)
- Cleanup examples. - see [Commit](https://github.com/pme123/camundala/commit/0d738c017180f68accffac6961ebdd8fc7f68d5f)
- Started migrating from camundala-dsl - see [Commit](https://github.com/pme123/camundala/commit/d3ebc2b9f1a4c05bc1a9efad2da1c5c769afad1e)
- Merge remote-tracking branch 'origin/master' - see [Commit](https://github.com/pme123/camundala/commit/b037306178772ad0819934ec8fba4877e2a8c9a0)
- Removed old project - see [Commit](https://github.com/pme123/camundala/commit/db7e89ad51571b7a449043351edec8dfec382ce1)
- Update README.md - see [Commit](https://github.com/pme123/camundala/commit/8163632912de43c1c9d36c8afbe7c425a7a94b0a)
- added next version - see [Commit](https://github.com/pme123/camundala/commit/d19e17e87c40eb081b3ef66e6eb57a640eb7d325)
- StandardCliApp now standalone version - see [Commit](https://github.com/pme123/camundala/commit/ab87fbc71d5cc9b8288ab33cb3df6d17240ad457)
- migrated to ZIO 1.0.1 - see [Commit](https://github.com/pme123/camundala/commit/da1db0102b043202f583d38630297e0cd53ab2ef)
- removed app module - see [Commit](https://github.com/pme123/camundala/commit/5d7376949756eb04d76d742141444b51977b7ac8)
- extracted standalone CamundaApp and HttpServerApp. - see [Commit](https://github.com/pme123/camundala/commit/cfedb993f6128bb50ecd5326869a80fbf2b90b6b)
- added Bpmn DSL from Bpmn XMLs - see [Commit](https://github.com/pme123/camundala/commit/f6d75f50ccedf0f9937390609ec1c880980a2359)
- added Generate DSL from Bpmns - see [Commit](https://github.com/pme123/camundala/commit/63f44f84ee0de36f6f91a02fa5fed17073d52edf)
- added DSL Module without any dependencies - use of Type Classes - see [Commit](https://github.com/pme123/camundala/commit/1177906a7a2082896dcee3bf0e55f4dec7724476)
- extended mapping for Service Task. - see [Commit](https://github.com/pme123/camundala/commit/3218a7a16fc0706696d8dbd0fcb363c418e1e6b4)
- added CallActivity - see [Commit](https://github.com/pme123/camundala/commit/3382cda39cae0807b31d407e95bb852461e56570)
- fix bad path - see [Commit](https://github.com/pme123/camundala/commit/f5e455664ee008c1024f636a51c2057e6adf6371)
- added Merge functionality - added test cases - see [Commit](https://github.com/pme123/camundala/commit/33671c0955f070edfd175f9d7d5babd0a3b4f45d)
- added CallActivity - see [Commit](https://github.com/pme123/camundala/commit/491abecf446fe1cfc7c2fea928eac6a08aec4252)
- added mapping documentation - see [Commit](https://github.com/pme123/camundala/commit/2c2692854c1671a7b662c5bdb8b5c8aaad147afb)
- added mapping input from json path - see [Commit](https://github.com/pme123/camundala/commit/1960009844b186229c16137d4c0c71ea3a2b271d)
- adding generic json from form mapping - see [Commit](https://github.com/pme123/camundala/commit/280c2f84dab509be5fc79c612c9dc5b5cc6b42f9)
- adding generic mappings from form - see [Commit](https://github.com/pme123/camundala/commit/f18ea9d813194c849a6c834927a1a41054fa78af)
- first attempt to introduce testing - FAILED:( - see [Commit](https://github.com/pme123/camundala/commit/ee01072308db941b656af828554835e3f24ec0b9)
- fixing tests and introducing idea of ProcessScenario. - see [Commit](https://github.com/pme123/camundala/commit/225cdd596479cd58e25753207c7859331aafe6d0)
- added in-out sequenceFlows. - see [Commit](https://github.com/pme123/camundala/commit/8b313fb418969879cc98fe93a22de9efca90602f)
- adjusted GenerateDsl functionality again;) - see [Commit](https://github.com/pme123/camundala/commit/b0eb316ff34d0b3433ec4a77b601e2fde3caf7a8)
- adjusted GenerateDsl functionality - see [Commit](https://github.com/pme123/camundala/commit/cc27d790a0f9651536efe25cec35d4fbcc9d10ee)
- example same level of abstraction - see [Commit](https://github.com/pme123/camundala/commit/356ced74e25a3e2cd94c84279daceab023bc9d09)
- added linting - see [Commit](https://github.com/pme123/camundala/commit/38204f7cf6db783acbe57e2247ce2bbaf967048b)
- added -Xfatal-warnings - see [Commit](https://github.com/pme123/camundala/commit/e72b7a59434b457dabe12b6353e8d2418636385b)
- cleanup bpmn - see [Commit](https://github.com/pme123/camundala/commit/5b0fc76ec1c5a602e01d505b3eedb0b5224828f3)
- added TaskImplementation - finishing up Type Classes for BPMN - see [Commit](https://github.com/pme123/camundala/commit/748e586bd2dbf6b8dfb1c27b4829161e6014c3af)
- added WithInOutputs - see [Commit](https://github.com/pme123/camundala/commit/0699749e0ace4ef6a0a3ceaf39aee2396cae6b2d)
- added TypeClass for ExtProperties - see [Commit](https://github.com/pme123/camundala/commit/3474026c8837edabe845c018dc0b27dd2e3d713a)
- added TypeClass for Constraint and HasForm - see [Commit](https://github.com/pme123/camundala/commit/57c2d0fdc5bf5f2785911154f5e66355a4b05cde)
- first tests with Camunda Assert - see [Commit](https://github.com/pme123/camundala/commit/b79a145b6260abc0f25d58feb42c0d48cae17581)
- cleanup mapping - see [Commit](https://github.com/pme123/camundala/commit/0742bc8352217b3258d14baa7616b0156dd2f23b)
- first manual working ChangeAddress Process. - see [Commit](https://github.com/pme123/camundala/commit/2bf5d5c56608d1377b7779668f7207aad0ab15af)
- added Dmn table / starting Change Address example - see [Commit](https://github.com/pme123/camundala/commit/db31bf7b3fe60fef589a2d1657599aea55eb3282)
- cleanup DSL - added generateDsl to print BPMN as Scala Classes - see [Commit](https://github.com/pme123/camundala/commit/5b617b4f6a61015727924e39d44867ad2e1e58cc)
- added Multipart tapir example to check problems - see [Commit](https://github.com/pme123/camundala/commit/bf89497c1c49bceba424e02f5f4da77a9ba76667)
- added DSL to Groups and Users and Forms - see [Commit](https://github.com/pme123/camundala/commit/082e344b0c3918faf55b39bcdc831ee3b27ecbd1)
- added more functions to DSL - see [Commit](https://github.com/pme123/camundala/commit/07c1a69aa2e9ec4a5c3325b052d337f3df200254)
- adjusted all examples to DSL - see [Commit](https://github.com/pme123/camundala/commit/23a3bfc12ded5dede4908aa3425612141490ca37)
- added DSL for creating BPMN Model - see [Commit](https://github.com/pme123/camundala/commit/9dcd5bc219edc99aa6936af5c6abe9998dfef87a)
- introducing Tapir and Open API documentation. - see [Commit](https://github.com/pme123/camundala/commit/8ad37ffe184b83ba736a0fe3fdda7875d3b58593)
- Fixes for Starter App - see [Commit](https://github.com/pme123/camundala/commit/5b1d73714ed15b4c83418b2e250c7cbc5a5a010e)
- Fixes for Starter App - see [Commit](https://github.com/pme123/camundala/commit/907e3b01936afea9bc081928ffbbf11832bf007a)
- Fixes for Starter App - see [Commit](https://github.com/pme123/camundala/commit/be2e5eb70998745e91f1d67dcc5fcf8e3173ad1a)
- fixing all tests. - see [Commit](https://github.com/pme123/camundala/commit/8c66ecd3bc0592beb49a0b6aa88bd1641fa277d7)
- deployment now remote possible from Modeler. - see [Commit](https://github.com/pme123/camundala/commit/ad966d842b8767eff44e5e619ed0e622ed7a28ee)
- deployment now remote possible from Modeler. - see [Commit](https://github.com/pme123/camundala/commit/ee9f92f394ca0cce085b6e21d209d7d161f76476)
- added task to initialize Users and Groups - see [Commit](https://github.com/pme123/camundala/commit/f1dbf02a84d23dc8ddef07a8850a4a10de5b3833)
- added Groups and Users / CandidateUsers / CandidateGroups - see [Commit](https://github.com/pme123/camundala/commit/fc352e25315d2eb62823831813c717cbcdcd15a0)
- removed deploymentService and processEngineService. - see [Commit](https://github.com/pme123/camundala/commit/2f4cb5aa5b3a80a0dd211a0c91e5a0a18d8b2770)
- removed unused deployment functions (they go now over Rest API) - see [Commit](https://github.com/pme123/camundala/commit/3f8e25bd4e5e540050afb525efa0561db8f7ebe8)
- moved Deploy Client to restService. - see [Commit](https://github.com/pme123/camundala/commit/1600f9a409defb09b7ab02778fe973c9757c1c74)
- added loading from File Path - no restart is needed - see [Commit](https://github.com/pme123/camundala/commit/515d9d4aeb6bd3b39cf42fbeb0895b5656893fde)
- added Specification of a ServiceTask - see [Commit](https://github.com/pme123/camundala/commit/5e1dba824c487c28411ce04b365a46f8ee09d7c9)
- added Form support - see [Commit](https://github.com/pme123/camundala/commit/aba3385bcefadd364b4350c7e57924d189b5e1c9)
- working SWAPI process - see [Commit](https://github.com/pme123/camundala/commit/6e528d0a9c001d7c6ae394dc9a2060b3a2c29c2c)
- working Delegate for RestService - see [Commit](https://github.com/pme123/camundala/commit/3e46abe867d32c4a7ab05bcd323c0fa73e181a06)
- added a generic RestServiceLayer - see [Commit](https://github.com/pme123/camundala/commit/62cb93ab3caacf91c5cefda38f48dbc55af9c6b0)
- added Code Generator for BPMNs from XML. - see [Commit](https://github.com/pme123/camundala/commit/612cd65df4559ef5186a7aae88800c30da6c609e)
- added second example with different ports - playground - see [Commit](https://github.com/pme123/camundala/commit/1fc09a0d9afa2149a2b3dd2d6dd6794d04d0aebd)
- changed console to log - except of CLI - see [Commit](https://github.com/pme123/camundala/commit/47f1e1f5895279db56ace736a35d2df694ae005f)
- added remote undeploy - see [Commit](https://github.com/pme123/camundala/commit/e3b77e0ae4bb09e421312f472df6d070b4d12170)
- added remote deployments - see [Commit](https://github.com/pme123/camundala/commit/d44d4711e3244083cf715b3909cfe9da1de9cdc0)
- problem working with mill assembly (Script Engine is null) - see [Commit](https://github.com/pme123/camundala/commit/6f966cfcfff02d59ffd39329327a9ed8d4ec23f6)
- added Remote Deploy functionality. - see [Commit](https://github.com/pme123/camundala/commit/930de68e77c55da3ae82bec77977e5aad47e09d2)
- added Docker with the REST API. - see [Commit](https://github.com/pme123/camundala/commit/da40e30ce377ed2588903f9c58e60a0a2549a6ae)
- added Refined wherever possible in the model - see [Commit](https://github.com/pme123/camundala/commit/b5783c388ee6a9464b77cf439cdd763c1bf6079f)
- introduced refined - see [Commit](https://github.com/pme123/camundala/commit/d363d4b5f2cd23ee0cce370ba760dcd26bf5d70e)
- moved TwitterApp to StandardApp - see [Commit](https://github.com/pme123/camundala/commit/2ff3db00f6ed74c1192736f15c4bcb7f06cab53b)
- added App support stop / start / restart - see [Commit](https://github.com/pme123/camundala/commit/c916aca7a7761bd1e0cca813f93b85f1353cea4b)
- added Docker support. - see [Commit](https://github.com/pme123/camundala/commit/87df8d05ae27c30dd2ae6ae356be5c5395584311)
- Added Input / Output as Extensions. - see [Commit](https://github.com/pme123/camundala/commit/608335e34985d5766690bd55054c4fbccc634e1a)
- added ConditionalExpressions for SequenceFlow - see [Commit](https://github.com/pme123/camundala/commit/57d6b4a67c9e71eee28ac82b3dc0d23d529aba3c)
- added SequenceFlow with Extensions - see [Commit](https://github.com/pme123/camundala/commit/e7c90a405b0718678a88adfca1d9d657293251d8)
- fixed Problem with Camunda Modeler check if Server is available. - see [Commit](https://github.com/pme123/camundala/commit/b6a9ce4279087dd7c12eac76aabb5cb45e7d913c)
- adjusted root of HttpServer - see [Commit](https://github.com/pme123/camundala/commit/31217288c26471667bac658fccaea1e65d7147ca)
- added undeploy functionality. - see [Commit](https://github.com/pme123/camundala/commit/24a23c9a67f3b14fa10be284b4097fa3b3c7c564)
- removed extra static files. - see [Commit](https://github.com/pme123/camundala/commit/80b369db5724282bc46303ad61346e0dd88be716)
- moved embedded Form to Scala BPMN - see [Commit](https://github.com/pme123/camundala/commit/5976675483d4a79cfc4d8e077218d8f6a6888463)
- find solution with Scala Script. - see [Commit](https://github.com/pme123/camundala/commit/040f1aa18725eab9d2628d422146ced25a1eb88d)
- experimenting with update / added appRunner - see [Commit](https://github.com/pme123/camundala/commit/9b26e5b6508dd827ef0facde15cdf573e741cb12)
- experimenting with update / added appRunner - see [Commit](https://github.com/pme123/camundala/commit/61c482694842a71644d253cc03c90e45f383631c)
- experimenting with restart - see [Commit](https://github.com/pme123/camundala/commit/8c0374f0b3a5962bbe23dfb567a6b8f915d85e5b)
- added validateBpmn function. - see [Commit](https://github.com/pme123/camundala/commit/5a53fcf256aa3baf1aef3b02013a86e09a725559)
- fixed bad bpmn / adjusted README. - see [Commit](https://github.com/pme123/camundala/commit/f13507fbadc1e3c3edfd035eb6cde942c919d6c0)
- added External Task to Service Task / added additional Test Process. - see [Commit](https://github.com/pme123/camundala/commit/cd801817e3db5b34b415a38728b9969215f07ac5)
- added Expression Delegate to Service Task - see [Commit](https://github.com/pme123/camundala/commit/ec7157128f9e48f7bd4f2b6b6afdcf6b65622555)
- added Deployments Command to CLI - see [Commit](https://github.com/pme123/camundala/commit/3983dacca006b68c588d37007e31d042a3fc7bfd)
- added Deploy Command to CLI - see [Commit](https://github.com/pme123/camundala/commit/4810463eeb6d12f2f0e20cc4dfb2891f76953032)
- added Deploy Registry - see [Commit](https://github.com/pme123/camundala/commit/d3c4732672de4285afc15032b4f4fdaa6f5a5088)
- added CLI to the TwitterApp - see [Commit](https://github.com/pme123/camundala/commit/2260665ecbd9cdee7a96e31d15a43f1c0db31ddc)
- added a ZIO Layer for cli - see [Commit](https://github.com/pme123/camundala/commit/01889159454b76e56df193be4aacae03919c09d0)
- first version of the CLI. - see [Commit](https://github.com/pme123/camundala/commit/c8cfceb22fc93f03aeecebe146b0632a74bd8ee6)
- fixed mocking Deployment - see [Commit](https://github.com/pme123/camundala/commit/fcee72c249fa94aa9e28a2c277a7d8d1f539b0df)
- moved ProcessEngine to a Layer. - see [Commit](https://github.com/pme123/camundala/commit/5209aa631da9c33ce0ac24aa2fffdd734ae6f4c4)
- added StaticFile - with static embedded Forms - see [Commit](https://github.com/pme123/camundala/commit/66bf3309dcc1c9eeb76adb7b03f3732a068f4477)
- moved to bpmn registry / adding StaticFile - see [Commit](https://github.com/pme123/camundala/commit/289d212bd32b6a399c9a6a367b833d1938aeb14e)
- added all elements with extensions. - see [Commit](https://github.com/pme123/camundala/commit/0a25c4d9fdcda58f2e834af63685a4b3ea3aa703)
- deploy diagram with scala extensions. - see [Commit](https://github.com/pme123/camundala/commit/fb0986ec72910aa4d18fb8f8d63c2d6732fd1bf7)
- state of work - see [Commit](https://github.com/pme123/camundala/commit/178497a3ceb432b10472b87462090a0d46a373bc)
- added httpServer module - see [Commit](https://github.com/pme123/camundala/commit/0cd6947fa16cfb5deb97d497bec66ae44d8f4b3e)
- added appConfig module - see [Commit](https://github.com/pme123/camundala/commit/570f66e44b3eba0f9b7de35671809548288aa5df)
- created common SpringApp in camunda module - see [Commit](https://github.com/pme123/camundala/commit/29bc9c2612f05c5f74cdb80179f4f2caa1e38c17)
- moved twitter-auth.conf to twitter example module. - see [Commit](https://github.com/pme123/camundala/commit/230152376ec2f84752f3e3ae7edf3e668bbf7b41)
- moved twitter-auth.conf to twitter example. - see [Commit](https://github.com/pme123/camundala/commit/a00fb52c6354b0d74650655b85b63fc59d30d569)
- adjustments in examples README.md - see [Commit](https://github.com/pme123/camundala/commit/f3d2d4a602502e89e0260629e6357bf88e613848)
- added postgres as Docker image. Now multiple examples can be run in parallel. - see [Commit](https://github.com/pme123/camundala/commit/7e24b1580ecd2812a7f9505d2d4e0ace504503ce)
- added plain REST service - see [Commit](https://github.com/pme123/camundala/commit/315b9ea839f462d21fd2cd76308cdfd33f290df8)
- initial commit - see [Commit](https://github.com/pme123/camundala/commit/5c6d779af129c93bc932172b6559880022c8cb86)