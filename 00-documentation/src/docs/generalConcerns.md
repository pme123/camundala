## General Concerns
(General Variables)

To avoid a lot of boilerplate in your _Input Objects_, we define a list of input variables we handle by default.

We generate a description with the API Documentation under [_General Variables_](exampleApi/OpenApi.html).

- camundala-api:
    - Creates the documentation of these variables, including example.
    - For mocking, it will generate a concrete example in each Process or ServiceProcess.

- camundala-simulation:
    - Adds them, if defined to call the Camunda's REST API.

- camundala-worker:
    - Part of the implementation.

You can override the list of variables, you support in your _ApiProjectCreator_, like

```scala
import camundala.bpmn.InputParams.*

override def supportedVariables: Seq[InputParams] = Seq(
    servicesMocked,
    outputMock,
    outputServiceMock,
    handledErrors,
    regexHandledErrors,
    impersonateUserId
  )
```

@:callout(info)
If you use our Workers - you must use the predefined Variables.
@:@

### Mocking
This looks a bit strange, that mocking is at the domain level.
However, it turns out that this is quite helpful:

- API Documentation: You see if a process provides Mocking, and/or if it is possible to mock certain sub processes.
- Simulation: You can simply mock sub processes and workers.
- Postman Requests: You can manipulate with mocks the path taken in the process (even on production).

@:callout(info)
The mocking is done with General Variables - see also the chapter above.

**The Usage** is described here under [_General Variables_](exampleApi/OpenApi.html)
@:@

We have four ways to mock. Each possibility is done with a dedicated Process Variable.

#### 1. Services mocked

- Variable: `servicesMocked: Boolean` - default: _false_

In a process, this mocks every _ServiceTask_ (_ServiceWorker_),
with the `serviceMock` (`MockedServiceResponse[ServiceOut]`).

#### 2. Mocked Workers

- Variable: `mockedWorkers: Seq[String]` - default: _Seq.empty_

In a process, this mocks the _SubProcesses_ and _ServiceTasks_,
if their _topicName_ or _processName_ is in this list.

_Processes_ must have an _InitWorker_ and you need to add an _In Mapping_ in the _BPMN_!

#### 3. Mocked Output

- Variable: `outputMock: Option[Out]` - default: _None_

A Process or a Worker, can be mocked with its `Out` object.

_Processes_ must have an _InitWorker_!

#### 4. Mocked Service Output

- Variable: `outputServiceMock: Option[MockedServiceResponse[ServiceOut]]` - default: _None_

A _ServiceWorker_, can be mocked with its `ServiceOut` object.

This allows you also to mock failures in the Service, e.g. `MockedServiceResponse.error(404)`.

#### Mocking Input (_outputMock_ & _outputServiceMock_)

We define specific Mocks of a Process in its Input Class (`In`).

```scala
  case class In(
                 //..
                 mocks: Option[Mocks] = None
               )
```
For better readability we put all Mocks in a separate _case class_:

```scala
  case class Mocks(
                    @description(serviceOrProcessMockDescr(GetAccount.Out()))
                    getAccountMock: Option[GetAccount.Out] = None,
                    @description(serviceOrProcessMockDescr(GetAccount.serviceMock))
                    getServiceAccountMock: Option[MockedServiceResponse[GetAccount.ServiceOut]] = None,
                  )
```
This class contains all mocks as optional variables.

- In the _BPMN_ you map the mocks to the according _outputMock_ or _outputServiceMock_.

    - _getAccountMock_ -> _outputMock_
    - _getServiceAccountMock_ -> _outputServiceMock_

### Process Configuration
The configuration of a process is also part of the Input object (`In`),
and so part of the domain specification.

A like the _Mocks_ this has these advantages:

- API Documentation: You see if there are technical variables that you can adjust. E.g. _Timers_ etc.
- Simulation: You can simply set a _Timer_ to 0, to also test _Timers_.
- Postman Requests: A Business Analyst can test the process also, without waiting until a _Timer_ is due.

We define specific Mocks of a Process in its Input Class (`In`).

```scala
  case class In(
                 //..
                 config: Option[InConfig] = None,
               )
```
For better readability we put all Configurations in a separate _case class_:

```scala
  case class InConfig(
                       @description("Timer to wait....")
                       waitForInput: String = "PT2H"
                     )
```

The class contains all variables with its default values.

### Mapping
By default, all output variables (`Out`) of a Worker are on the process (External Task completion).

To reduce the variables you have two possibilities, that also can be combined:

#### 1. Filter Output Variables

- Variable: `outputVariables: Seq[String]` - default: _Seq.empty_

You can filter the Output with a list of variable names you are interested in.
This list may include all variables from the output (`Out`).

#### 2. Manual Output Mapping

- Variable: `manualOutMapping:  Boolean` - default: _false_

This will complete the External Task only with **local** output variables.
And you must do the output mapping manually in the _BPMN_.


@:callout(info)
This is needed, if you have the output variable already in the process with another value.
@:@

### Exception Handling

To handle an Exception in a Worker, we can do the following:

#### 1. List of Error Codes

- Variable: `handledErrors: Seq[String]` - default: _Seq.empty_

To handle Errors in a _ServiceTask_, you need to define a list of error codes.
If an error has this error code, it will complete with a _BpmnError_, instead a Failure.

If you want to handle **all** Errors, you can use `CatchAll` instead listing all possible error codes.

#### 2. List of Error Messages

- Variable: `regexHandledErrors: Seq[String]` - default: _Seq.empty_

If the error code is not enough, you can also add a list of regex expressions, to filter the Errors you handle.

For example, you want to handle a _400_ error, but only if the message contains _bad response_.

### Authorization

#### Impersonate User

- Variable: `impersonateUserId: Option[String]` - default: _None_

User-ID or Correlation-ID of a User that should be taken to authenticate to the services.
This must be supported by your implementation.

It is helpful if you have Tokens that expire, but long-running Processes.

### Validation

The Validation is handled by all _[Workers]_ and no additional variables are needed.

The following objects are handled:
- Input Variables (`In`)
- Service Output Variables (Service Output Body `ServiceOut`)
