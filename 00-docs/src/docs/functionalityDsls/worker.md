# Workers
The implementation of an **_External Task_** is done by a _**Worker**_.

So for each _External Task_ type we have a DSL for an according _Worker_.

## Custom Worker
The _Custom Worker_ is a general _Worker_ that can used for any business logic or integration.

It automatically does:
- validate the input variables

You can create:
- business logic that can't be handled by the expression language in the BPMN itself
- a service integration that is not covered by the _Service Worker_
- whatever you want

```scala
import mycompany.myproject.bpmn.myprocess.v1.MyCustomTask.*

@Configuration
class MyCustomTaskWorker extends CompanyCustomWorkerDsl[In, Out]:

  lazy val customTask = example

  def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] =
    // your business logic
    ???
  override def validate(in: In): Either[CamundalaWorkerError.ValidatorError, In] =
    ??? // custom validation logic here  
```
- `lazy val customTask` is just needed for the compiler to check the correctness of the types.
- `runWork` is the method that is called by the _Worker_ to execute the business logic.
- The code can either:
  - complete the task successfully with a result -> `Right[Out]`
  - fail the task with an error -> `Left[CamundalaWorkerError.CustomError]`
- `override def validate(in: In)` is optional and can be used to add more sophisticated validation logic.
  If the validation fails, the process will fail.

Examples:

### runWork
TODO
### validate
This is the same in every worker type.
TODO

```scala
## Init Process Worker
The _Init Process Worker_ is a special _Worker_ that is used to start a process.

It automatically does:
- validate the input variables
- merge the _inConfig_ variables with manual overrides

You can:
- init input process variables with default values.
- init process variables used to control the process flow, 
  like counters, variables that may not be on the process.
- create simplified process variables to simplify the process flow.
- validate the input variables, if you need more sophisticated validation logic, 
  than you can do with the type definition.

```scala
import mycompany.myproject.bpmn.myprocess.v1.MyProcess.*

@Configuration
class MyProcessWorker extends CompanyInitWorkerDsl[In, Out, InitIn, InConfig]:

  lazy val inOutExample = example
    
  def customInit(in: In): InitIn =
    ??? // init logic here
  override def validate(in: In): Either[CamundalaWorkerError.ValidatorError, In] =
    ??? // custom validation logic here
```
- `lazy val inOutExample` is just needed for the compiler to check the correctness of the types.
- `customInit` is the method that is called by the _Worker_ to execute the init logic.
- The method sets the process variables according the _InitIn_ object.
  Be aware that this can not fail, as the input variables are already validated.
- `override def validate(in: In)` is optional and can be used to add more sophisticated validation logic.
  If the validation fails, the process will fail.

Examples:

### customInit
TODO

## Service Worker
The _Service Worker_ is a special _Worker_ that is used to call a REST API service.

It automatically does:
- validate the input variables

You can provide:
- the method and the path of the service
- the query parameters
- the headers
- the mapping of the input- or output request body

```scala
import mycompany.myproject.bpmn.myprocess.v1.MyServiceTask.*

@Configuration
class MyServiceTaskWorker extends CompanyServiceWorkerDsl[In, Out]:

  lazy val serviceTask = example

  def apiUri(in: In) = uri"$serviceBasePath/myService"
  override protected lazy val method: Method = Method.POST
 
  override protected def inputHeaders(in: In): Map[String, String] =
  ??? // map the input variables to the headers
  override def querySegments(in: In) =
    ??? // map the input variables to the query parameters  
  override def inputMapper(in: In): Option[ServiceIn] =
    ??? // map the input variables to the service request body
  override def outputMapper(
                           out: ServiceResponse[ServiceOut],
                           in: In
                         ): Either[ServiceMappingError, Out] =
    ??? // map the service response body and header to the output variables
  override def validate(in: In): Either[CamundalaWorkerError.ValidatorError, In] =
    ??? // custom validation logic here
```
- `lazy val serviceTask` is just needed for the compiler to check the correctness of the types.
- `def apiUri(in: In)` the path of the service, with the path parameters from the `in` object.
  The only required function.
- `override protected lazy val method: Method` is the HTTP method. Default is `Method.GET`.
- `override def querySegments(in: In)` is optional and can be used to add query parameters to the request.
- `override def inputHeaders(in: In)` is optional and can be used to add headers to the request.
- `override def inputMapper(in: In)` is optional and can be used to map the input variables to the request body.
- `override def outputMapper(out: ServiceResponse[ServiceOut], in: In)` is optional and can be used to map the response body and -headers to the output variables.
- `override def validate(in: In)` is optional and can be used to add more sophisticated validation logic.
  If the validation fails, the process will fail.

Examples:

### apiUri
TODO
### querySegments
TODO
### inputHeaders
TODO
### inputMapper
TODO
### outputMapper
