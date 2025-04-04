# Workers
The implementation of an **_External Task_** is done by a _**Worker**_.

So for each _External Task_ type we have a DSL for an according _Worker_.

@:callout(info)
In the future, it is possible to have more than the Worker types, that are described here.
@:@

## General
Let's start with functions that are provided for all Workers.

### validate
In every _Worker_, the input `In` is validated automatically (it is decoded from JSON to a `In` object).
However, you can override the `validate` method to add more sophisticated validation logic.

Example:
```scala
  override def validate(in: In): Either[ValidatorError, In] =
    for
      _ <- Try(in.accountTypeFilterAsSeq).toEither.left.map: _ =>
          ValidatorError(
            "accountTypeFilter must be a single Int or a comma separated String of Ints"
          )
      //_ <- moreValidation(in)
    yield in    
```

## Custom Worker
The _Custom Worker_ is a general _Worker_ that can used for any business logic or integration.

You can create:
- business logic that can't be handled by the expression language in the BPMN itself
- complex mappings
- a service integration that is not covered by the _Service Worker_
- whatever you want

Use this [Generator](../development/projectDev.md#customtask) to create a new _Custom Worker_

```scala
import mycompany.myproject.bpmn.myprocess.v1.MyCustomTask.*

@SpringConfiguration
class MyCustomTaskWorker extends CompanyCustomWorkerDsl[In, Out]:

  lazy val customTask = example

  override def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] =
    // your business logic
    ???
```
- `lazy val customTask` is just needed for the compiler to check the correctness of the types (_Prototype_ pattern).
- `runWork` is the method that is called by the _Worker_ to execute the business logic.
- The code can either:
  - complete the task successfully with a result -> `Right[Out]`
  - fail the task with an error -> `Left[CamundalaWorkerError.CustomError]`

Example:

```scala
def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] =
  doSomethingThatCanFail(in)
    .left.map: e => 
      CamundalaWorkerError.CustomError("Problem in Worker: " + e.getMessage)
    
private def doSomethingThatCanFail(in: In): Either[Throwable, In] = ???    
```
`doSomethingThatCanFail` does some mapping or business logic that can fail.
If it fails, it returns a `Left` with an error message, that you wrap with a _CamundalaWorkerError.CustomError_.

@:include(workers_advanced.md)

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

```scala
import mycompany.myproject.bpmn.myprocess.v1.MyProcess.*

@SpringConfiguration
class MyProcessWorker extends CompanyInitWorkerDsl[In, Out, InitIn, InConfig]:

  lazy val inOutExample = example
    
  def customInit(in: In): InitIn =
    ??? // init logic here
```
- `lazy val inOutExample` is just needed for the compiler to check the correctness of the types.
- `customInit` is the method that is called by the _Worker_ to execute the init logic.
- The method sets the process variables according the _InitIn_ object.
  Be aware that this can not fail, as the input variables are already validated.

Examples:

### customInit

```scala
def customInit(in: In): InitIn =
  InitIn(
    currency = in.currency.getOrElse(Currency.EUR), // set optional value with default value
    requestCounter = 0, // init process variables used to control the process flow
    iban = in.person.iban, // simplify process variables
    //...
  )
```

## Service Worker
The _Service Worker_ is a special _Worker_ that is used to call a REST API service.

You can provide:
- the method and the path of the service
- the query parameters
- the headers
- the mapping of the input- or output request body

```scala
import mycompany.myproject.bpmn.myprocess.v1.MyServiceTask.*

@SpringConfiguration
class MyServiceTaskWorker extends CompanyServiceWorkerDsl[In, Out]:

  lazy val serviceTask = example

  def apiUri(in: In) = uri"$serviceBasePath/myService"
  override lazy val method: Method = Method.POST
 
  override def inputHeaders(in: In): Map[String, String] =
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
```
- `lazy val serviceTask` is just needed for the compiler to check the correctness of the types.
- `def apiUri(in: In)` the path of the service, with the path parameters from the `in` object.
  The only required function.
- `override protected lazy val method: Method` is the HTTP method. Default is `Method.GET`.
- `override def querySegments(in: In)` is optional and can be used to add query parameters to the request.
- `override def inputHeaders(in: In)` is optional and can be used to add headers to the request.
- `override def inputMapper(in: In)` is optional and can be used to map the input variables to the request body.
- `override def outputMapper(out: ServiceResponse[ServiceOut], in: In)` is optional and can be used to map the response body and -headers to the output variables.

Examples:

### apiUri
```scala
def apiUri(in: In) = uri"$serviceBasePath/myService/account/${in.accountId}"
```
The only required function. It returns the path of the service, with the path parameters from the `in` object.

### method
```scala
override lazy val method: Method = Method.POST
```
Override the HTTP method. Default is `Method.GET`.

### inputHeaders
```scala
override def inputHeaders(in: In): Map[String, String] =
  Map("Correlation-ID" -> in.userId)
```

### querySegments
We support three ways to provide query parameters:

#### queryKeys
```scala
override def querySegments(in: In) =
  queryKeys("limitSelection", "accountType")
```
A list of optional `In` fields that are mapped to query parameters.
So in this example you need to have `limitSelection` and `accountType` in your `In` object.
```scala
case class In(limitSelection: Option[Int], accountType: Option[String])
```

#### queryKeyValues
```scala
override def querySegments(in: In) =
  queryKeyValues(
    "limitSelection" -> in.limitSelection,
    "accountType" -> adjust(in.accountType)
  )
```
If you need to adjust an `In` value, you can use this way of explicit listing the key-value pairs.

#### queryValues
```scala
override def querySegments(in: In) =
  queryValues(
    s"eq(username,string:${in.user})"
  )
```
If you have a query language, you can use this way to provide the query parameters.

#### a combination of the above
```scala
override def querySegments(in: In) =
  queryKeys("limitSelection") ++
    queryKeyValues("accountType" -> adjust(in.accountType))
```
And you can combine them as you like.

### inputMapper
```scala
override def inputMapper(in: In): Option[ServiceIn] =
  Some(ServiceIn(in.accountType, in.accountId))
```
Mapping the input variables to the request body.

### outputMapper

```scala
override def outputMapper(
                           out: ServiceResponse[ServiceOut],
                           in: In
                         ): Either[ServiceMappingError, Out] =
  out.outputBody
    .collect:
      case b if b.nonEmpty =>
        Right(Out(
          creditCardDetail = b.head,
          creditCardDetails = b
        ))
   .getOrElse(Left(ServiceMappingError("There is at least one CreditCardDetail expected.")))
```

Mapping the response body and -headers to the output variables.

@:callout(info)
As you can see there are only two methods that can fail:
- `validate` -> For the input, use this methode to validate the input and return any possible error.
- `outputMapper` -> For the output, we do not have an extra 'validate' method. 
  So if the service response is not as expected, you can return an error here.
@:@
