# Project Development

The following chapters describe the tasks to support the project development.

We provide a `helper.scala` script that helps you with the most common tasks.

In general, you can type `./helper.scala x` to get a list of available commands.

And then you can type `./helper.scala <command>` to get help for a specific command.

The `version` is optional and defaults to `1`.

## helper.scala
This file will be replaced with `./helper.scala update`. However, you need to set there the 
subprojects you want to use.

```scala
#!/usr/bin/env -S scala shebang
// DO NOT ADJUST. This file is replaced by `./helper.scala update`.
//> using dep mycompany::mycompany-camundala-helper:0.1.0-SNAPSHOT

import mycompany.camundala.helper.*

lazy val projectName: String = "mycompany-myProject"
lazy val subProjects = Seq(
  "accounting",
  "hr"
)

@main
def run(command: String, arguments: String*): Unit =
  CompanyDevHelper(projectName, subProjects).run(command, arguments*)
```
### subProjects
Compile time can be optimized by using subprojects - this makes the project a bit more complex, 
as for each subProject, a SBT module is created.

`./helper.scala update` will generate this file but preserve the project name and subprojects.

## update
Whenever you have changes in the `company-camundala` project or in one of your dependencies, 
you can update the project with the following command:

Usage / example:
```bash
./helper.scala update
```

This will create or update your project with the latest changes.

Files that contain the `DO NOT ADJUST` comment will be replaced.
If you do adjust them, remove this comment. 
You will get a warning, but the file will not be replaced.

## publish

Creates a new Release for the BPMN project and publishes to the repository(e.g. Artifactory)

@:callout(info)
If you want to provide the documentation on a WebDAV server, 
you need a `CompanyDevHelper.devConfig.publishConfig` configuration.

@:@

Usage:
```
./helper.scala publish <VERSION>
```

Example:
```
./helper.scala publish 0.2.5
```

The following steps are executed:
- Check if there are no SNAPSHOTs as dependencies.
  - Release your dependencies first.
- Check if the `CHANGELOG.md` is updated.
  - Check and adjust manually `CHANGELOG.md`.
  - Remove `//---DRAFT start` / `//---DRAFT end`.
  - Run the command again.
- Push the `develop` branch.
- Adjust the version in `ProjectDef.scala` and `ApiProjectCreator.scala`.
- Run `ApiProjectCreator.scala`.
- Publish the project to the repository.
- Uploads the documentation to a WebDAV-webserver (optional).
- Merge the branch (`develop`) into `master`.
- Tag the GIT repository with the version.
- Increase the version to the next minor _SNAPSHOT_ version.


## deploy
Deploys the BPMN project to the local Camunda server and runs the Simulation you're working on.

@:callout(info)
**Be aware** that `CompanyDevHelper.devConfig.postmanConfig` must be defined.

At the moment, only deployment via _Postman Collection_ is supported (using Camunda REST API to deploy).
@:@

Usage:
```
./helper.scala deploy [simulation]
```

Example:
```
./helper.scala deploy MyProcessSimulation
```

The following steps are executed:
- Publishes Local (`sbt publishLocal`)
- runs the _deploy-collection_ of _Postman_
- runs a Simulation (optional)

## Generate Process/-Elements
To handle name conventions and to avoid errors, we generate as much as possible.

So it is essential, not to change the generated names.

The following generators are provided:

### process
Creates a new Process.

Usage:
```
./helper.scala process <processName> [version: Int]
```

Example:
```
./helper.scala process myProcess 1
```

This creates the following files:
```
// the BPMN
src           - main -> myproject-myProcessV1.bpmn
// the domain In -> Out
02-bpmn       - main -> mycompany.myproject.bpmn.myProcess.v1.MyProcess      
// the Simulation        
03-simulation - test -> mycompany.myproject.simulation.MyProcessSimulation           
// the InitWorker    
03-worker     - main -> mycompany.myproject.worker.myProcess.v1.MyProcessWorker  
              - test -> mycompany.myproject.worker.myProcess.v1.MyProcessWorkerTest 
```

### customTask
Creates a new Custom Task.

Usage:
```
./helper.scala customTask <processName> <bpmnName> [version: Int]
```

Example:
```
./helper.scala customTask myProcess MyCustomTask 1
```

This creates the following files:
```
// the domain In -> Out
02-bpmn   - main -> mycompany.myproject.bpmn.myProcess.v1.MyCustomTask   
// the CustomWorker
03-worker - main -> mycompany.myproject.worker.myProcess.v1.MyCustomTaskWorker  
          - test -> mycompany.myproject.worker.myProcess.v1.MyCustomTaskWorkerTest    
``` 

### serviceTask
Creates a new Service Task.

Usage:
```
./helper.scala serviceTask <processName> <bpmnName> [version: Int]
```

Example:
```
./helper.scala serviceTask myProcess MyServiceTask 1
```

This creates the following files:
```
// the domain In -> Out (ServiceIn -> ServiceOut)
02-bpmn   - main -> mycompany.myproject.bpmn.myProcess.v1.MyServiceTask
// the ServiceWorker
03-worker - main -> mycompany.myproject.worker.myProcess.v1.MyServiceTaskWorker  
          - test -> mycompany.myproject.worker.myProcess.v1.MyServiceTaskWorkerTest    
```

### userTask
Creates a new User Task.

Usage:
```
./helper.scala userTask <processName> <bpmnName> [version: Int]
```

Example:
```
./helper.scala userTask myProcess MyUserTask 1
```

This creates the following files:
```
// the domain In -> Out
02-bpmn - main -> mycompany.myproject.bpmn.myProcess.v1.MyUserTask      
```

### decision
Creates a new Decision.

Usage:
```
./helper.scala decision <processName> <bpmnName> [version: Int]
```

Example:
```
./helper.scala decision myProcess MyDecision 1
```

This creates the following files:
```
// the domain In -> Out
02-bpmn - main -> mycompany.myproject.bpmn.myProcess.v1.MyDecision      
```

### signalEvent
Creates a new Signal Event.

Usage:
```
./helper.scala signalEvent <processName> <bpmnName> [version: Int]
```

Example:
```
./helper.scala signalEvent myProcess MySignalEvent 1
```

This creates the following files:
```
// the domain In -> NoOutput
02-bpmn - main -> mycompany.myproject.bpmn.myProcess.v1.MySignalEvent      
```

### messageEvent
Creates a new Message Event.

Usage:
```
./helper.scala messageEvent <processName> <bpmnName> [version: Int]
```

Example:
```
./helper.scala messageEvent myProcess MyMessageEvent 1
```

This creates the following files:
```
// the domain In -> NoOutput
02-bpmn - main -> mycompany.myproject.bpmn.myProcess.v1.MyMessageEvent      
```

### timerEvent
Creates a new Timer Event.

Usage:
```
./helper.scala timerEvent <processName> <bpmnName> [version: Int]
```

Example:
```
./helper.scala timerEvent myProcess MyTimerEvent 1
```

This creates the following files:
```
// the domain NoInput -> NoOutput
02-bpmn - main -> mycompany.myproject.bpmn.myProcess.v1.MyTimerEvent      
```

## Docker
To run the Camunda Server locally, you can use `docker-compose`.

@:callout(info)
**Precondition**: 
- You have to have `docker` and `docker-compose` installed.
- You need to have a `docker-compose.yml` in `dev-company/docker` directory.
- Adjust the `CompanyDevHelper.devConfig.dockerConfig` configuration.

@:@

### dockerUp
Starts the server with `docker-compose`.

Usage / example:
```
./helper.scala dockerUp
```

### dockerStop
Stops the server with `docker-compose`.

Usage / example:
```
./helper.scala dockerStop
```

### dockerDown
Stops and removes the server with `docker-compose`. 

**Be aware** that all data will be lost - you have to deploy again.

Usage / example:
```
./helper.scala dockerDown
```
