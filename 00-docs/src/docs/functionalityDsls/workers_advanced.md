## Advanced Custom Workers

It is possible to orchestrate Workers in a _Custom Worker_.

This is advanced in a sense that you work with the _ZIO_ library.

Here are the main differences to a simple _Custom Worker_:

```scala
import mycompany.myproject.bpmn.myprocess.v1.MyCustomTask.*

@SpringConfiguration
class MyCustomTaskWorker extends CompanyCustomWorkerDsl[In, Out]:

  lazy val customTask = example

  override def runWorkZIO(in: In): EngineRunContext ?=> IO[CustomError, Out] =
    // your business logic
    ???
```
- `runWorkZIO` instead of `runWork` is the method that is called by the _Worker_ to execute the business logic.
- The code can either:
    - complete the task successfully with a result -> `ZIO.succeed[Out]`
    - fail the task with an error -> `ZIO.fail[CamundalaWorkerError.CustomError]`
- What about the strange return value `EngineRunContext ?=> IO[CustomError, Seq[Account]]`?
    - No worries `EngineRunContext ?=>` is just a way to pass the `EngineRunContext` to the function implicitly.
    - As you have this already in the `runWorkZIO` method, you can use it in the `getAccounts` method.
    - This is needed to call the `runWorkFromWorkerUnsafe` method of an orchestrated Worker
      and gets you access to the `GeneralVariables` for example.

### Why ZIO?
_ZIO_ is a library for asynchronous and concurrent programming in Scala.
It is used to handle errors and side effects in a functional way.
It is also used to handle the asynchronous nature of the Camunda 8 API.
This means that composing Workers is easier and more readable.

Let's look at an Example:

```scala
    @Autowired
    var processInstanceService: GetProcessInstanceWorker         = uninitialized
    @Autowired
    var createSetModulesOtherWorker: CreateSetModulesOtherWorker = uninitialized
    
    override def runWorkZIO(in: In): EngineRunContext ?=> IO[CustomError, Out] =
      for
        client <- getProcessInstances(in)
        result <- createSetModulesOther(in, client)
      yield result
```
- `GetProcessInstanceWorker`: you simply can add Workers as dependencies.
- `getProcessInstances(in)`: you can simply compose the Workers.

Running in parallel can also be achieved easily:
```scala
    for
          partnerClientKeys               <- getPartnerClientKeys(in, client)
          partners                        <- getPartners(partnerClientKeys) // wait for the above result
          eBankingContractsFork           <- getEBankingContracts(partners).fork // run in parallel
          cardsFork                       <- getCards(in).fork // run in parallel
          // Join all forks
          eBankingContracts               <- eBankingContractsFork.join 
          allCards                        <- cardsFork.join
          output                          <- createOutput(eBankingContracts, allCards)
        yield output
```

Error Handling:

```scala
    private[v3] def getAccounts(in: In): EngineRunContext ?=> IO[CustomError, Seq[Account]] =
      getAccountsWorker
        .runWorkFromWorkerUnsafe(GetAccounts.In.minimalExample.copy(
          clientKey = in.clientKey
        ))
        .mapError: err =>
          CustomError(
            s"Error while get Accounts: ${err.errorMsg}"
          )
```
Simply use `mapError` to map the error to a `CustomError`.

For more information on _ZIO_ see the [ZIO documentation](https://zio.dev/).

### Mocking
You can also mock a composed Worker like:

```scala
  private[v3] def checkClient(in: In)(using engineContext: EngineRunContext): IO[CustomError, GetClientClientKey.Out] =
    given EngineRunContext = engineContext
      .copy(generalVariables =
        engineContext.generalVariables.copy(
          outputServiceMock = in.loadCustomerMock.map(_.asJson)
        )
      )
    getClientClientKeyWorker
      .runWorkFromWorkerUnsafe(GetClientClientKey.In(in.clientKey))
```
- You see `(using engineContext: EngineRunContext):` is an alternative way to pass the `EngineRunContext` to the function implicitly.
- For mocking you can use the `outputServiceMock` or `outputMock` in the `GeneralVariables`.
- The mock itself must be in the `In` object of the Worker as there is no `InConfig` for a Worker.

### Handled Errors (BpmnError)

There are 2 scenarios where you can handle Errors in a Worker:
1. The worker logic should go on:
   ```scala
    .catchAll:
            case err: ServiceError if err.errorCode == 404 =>
              ZIO.succeed(Seq.empty[Card])
            case err                                       =>
              ZIO.fail:
                CustomError(
                  s"Error while loading cards.",
                  causeError = Some(err)
                )
    ```
   Just handle the error and switch the error to a successful result.

2. The worker logic should stop:
   ```scala
   ..
   .mapError: err =>
        CustomError(
          s"Error while checking client: ${err.errorMsg}",
          generalVariables =
            Some(GeneralVariables(handledErrors = engineContext.generalVariables.handledErrors)),
          causeError = Some(err)
        )
   ```
   Just extend the `CustomError` with the `handledErrors` and `causeError` to handle the error.
   The rest is done by _Camundala_. 
