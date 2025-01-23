# PoC Worker C7/C8
## Setup
```scala
              03-worker
         ^                 ^
  04-worker-c7zio   04-worker-c8zio
         ^                 ^
   05-examples / demos / 03-worker
```
### 03-worker
Interfaces like:

#### WorkerApp
Register the Workers in the configured WorkerRegistries. 
There is a registry for each Worker Implementation (different Camunda versions).

```scala
trait WorkerApp extends ZIOAppDefault:
  // a list of registries for each worker implementation
  def workerRegistries: Seq[WorkerRegistry[?]]
  // function that registers all the workers
  def workers(dWorkers: (WorkerDsl[?, ?] | Seq[WorkerDsl[?, ?]])*): Unit = ...
  ...
```
#### WorkerRegistry
Interface for a registry to register Workers for a certain Worker implementation.

```scala
trait WorkerRegistry[T <: WorkerDsl[?, ?]]:
    def register(workers: Set[WorkerDsl[?, ?]]) =
      logInfo(s"Registering Workers for ${getClass.getSimpleName}") *>
        registerWorkers(workers.collect { case w: T => w })
    
    protected def registerWorkers(workers: Set[T]): ZIO[Any, Any, Any]
```
#### WorkerDsl[In, Out]
Interface for a Worker that does the work.

```scala
trait WorkerDsl[In <: Product: InOutCodec, Out <: Product: InOutCodec]:
    // needed that it can be called from CSubscriptionPostProcessor
    def worker: Worker[In, Out, ?]
    def topic: String = worker.topic
    ...
```

### 04-worker-c7zio / 04-worker-c8zio
Implementations like:

#### C7WorkerRegistry / C8WorkerRegistry
```scala
class C7WorkerRegistry(client: C7Client)
    extends WorkerRegistry[C7Worker[?, ?]]:

  def registerWorkers(workers: Set[C7Worker[?, ?]]): ZIO[Any, Any, Any] = ???
  ...
```
#### C7Worker[In, Out] / C8Worker[In, Out]
Implementation of the Worker Client of the BPMN Engine. In this case, Camunda 7 or 8.

```scala
trait C7Worker[In <: Product: InOutCodec, Out <: Product: InOutCodec]
    extends WorkerDsl[In, Out], camunda.ExternalTaskHandler:

  protected def c7Context: C7Context
  
  def logger: WorkerLogger = Slf4JLogger.logger(getClass.getName)

  override def execute(
      externalTask: camunda.ExternalTask,
      externalTaskService: camunda.ExternalTaskService
  ): Unit = ??? 
    ...
```
### 05-examples / demos / 03-worker
Example for the new Worker implementation.

### Usage
- Run twitter-auto.bpmn on Camunda 8 -> I use Saas community account.
- Run twitter-auto-c7.bpmn on Camunda 7 -> I use _DemosExampleApplication_.
- Run the TestWorkerApp.

You find the test data in _testdataC7.json_ and _testdataC8.json_.
