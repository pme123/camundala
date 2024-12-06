# BPMN DSL

This _DSL_ will bring your domain into your _BPMN-Process_.

As the pattern is always the same, we can setup each _BPMN Element_ in the same way.

```scala
object BpmnElement extends CompanyBpmn<Element>Dsl:

  val processName = "mycompany-myproject-myelement" // depending on the element this can be different
  lazy val descr = "my element..."

  case class In(...)
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec

  case class Out(...)
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec

  lazy val example = <bpmnElement>(
    In(),
    Out(),
  )
end BpmnElement
```

So each BPMN Element has:

- _id_: a unique identifier, it must be unique within a Camunda Instance.
    Depending on the element this is named differently, e.g. `processName`, `messageName` etc.
- _descr_: a description of this element.
- _In_: an input class with the input process variables that we descibed in the [_Domain Specification_](specification.md).
- _Out_: an output class with the output process variables that we descibed in the [_Domain Specification_](specification.md).
- _example_: a method that creates an example of this element.

### Special Case Enums
If you have an _Enum_ as an In or Out class, you need to define the example like this:

```scala

lazy val example = <bpmnElement>(
  In.CaseA(),
  Out.CaseA(),
  ).withEnumInExamples(In.CaseB())
  .withEnumOutExamples(Out.CaseB())
```
- This is needed to document both cases and also to handle them correctly in the _Workers_.

We support the following elements:

## Process

The Process is the main element of a BPMN. 
It is the most complex element and looks like this:

```scala
object MyProcess extends CompanyBpmnProcessDsl:

  val processName = "mycompany-myproject-myprocess"
  lazy val descr = "my process..."
  
  case class In(
    ...
    inConfig: Option[InConfig] = None
  ) extends WithConfig[InConfig]
  
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec
  
  case class InConfig(
    // Process Configuration
    ...
    // Mocks
    ... )
  object InConfig:
    given ApiSchema[InConfig] = deriveApiSchema
    given InOutCodec[InConfig] = deriveInOutCodec
    
  case class InitIn(...)
  object InitIn:
    given ApiSchema[InitIn] = deriveApiSchema
    given InOutCodec[InitIn] = deriveInOutCodec
  
  case class Out(...)
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec
  
  lazy val example = process(
    In(),
    Out(),
    InitIn()
  )
end MyProcess
```
Next to the _In_ and _Out_ classes we have an _InitIn_  and _InConfig_ class.

### InitIn
Each process has an _InitWorker_ that is the first worker that is called when the process is started.

Use this class to:
- init the _Process Variables_ that are needed in the process (e.g. counters, variables used in expressions that must be defined (Camunda 7 restriction)).
- init the _Process Variables_ with default values, that are not provided by the client. 
  So you can be sure that they are always set - from Option to required in the process.

### InConfig
These are technical _Process Variables_, like:
- Control the process flow (e.g. timers).
- Mocking of services and sub-processes.

@:callout(info)
The _InitWorker_ will automatically put these variables on the process.
That means you can override them for example in _Postman_.
@:@

## Business Rule Tasks (Decision DMNs)

We support only Decision DMNs.
The input is always a domain object (each field must be a simple value that matches a column of the dmn).
As **simple values** we support:

- _String_
- _Boolean_
- _Int_
- _Long_
- _Double_
- _java.util.Date_
- _java.time.LocalDateTime_
- _java.time.ZonedDateTime_
- _scala.reflect.Enum_

A **domain object** is a case class as described in the [Specification](specification.md), 
with the exception, that each field must be a _simple value_ that matches a column of the dmn.

Inputs are always _domain objects_.

In the DSL we have an element for each of the four different return types - so you don't mix up the types 😊.

### singleEntry

This is a single result with one _simple value_. 

```scala
singleEntry(
    in = Input("A"),
    out = 1
  )
```

### singleResult

This is a single result with more than one value (_domain object_).

```scala
singleResult(
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
)
```

### collectEntries

This is a list of _simple values_.

```scala
collectEntries(
    in = Input("A"),
    out = Seq(1, 2)
  )
```

### resultList

This is a list of _domain objects_.

```scala
resultList(
    in = Input("A"),
    out = List(ManyOutResult(1, "🤩"), ManyOutResult(2, "😂"))
  )
```

## User Task

A _User Task_ describes its form values that it offers and the values it must be completed with.

```scala
object MyUserTask extends CompanyBpmnUserTaskDsl:

  val name = "mycompany-myproject-myusertask"
  val descr: String = "my user task..."
  
  case class In(...)
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec
  
  case class Out(...)
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec
  
  lazy val example = userTask(
    In(),
    Out()
  )
end MyUserTask
```
- The `name` is the name of the user task, **be aware** at the moment this is only for documentation.
- A _UserTask_ extends _CompanyBpmnUserTaskDsl_.
- The `In` object are the input variables you expect for the UI-Form of the _UserTask_.
- The `Out` object are the process variables, the UI-Form sends, when it completes the _UserTask_.

## Receive Message Event
A _Receive Message Event_ represents a catching message event. 
The input defines the message you expect.
This works only as intermediate event.
As we don't support _throwing Message events_ we can simplify this to _messageEvent_:

```scala
object MyMessageEvent extends CompanyBpmnMessageEventDsl:

  val messageName = "mycompany-myproject-mymessage"
  val descr: String = "my message..."
  
  case class In(...)
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec
  
  lazy val example = messageEvent(In())
end MyMessageEvent
```
- The `messageName` is the name of the message you expect.
  The correlation can be the business key or the process instance id.
  In the _Simulation_ we will use the _processInstanceId_.
- You can send process variables with the `In` object.
- A _MessageEvent_ extends _CompanyBpmnMessageEventDsl_.

## Receive Signal Event
A _Receive Signal Event_ represents a catching signal event.
The input defines the signal you expect.
This works only as intermediate event.
As we don't support _Throwing Signal events_ we can simplify this to _signalEvent_:

```scala
object MySignalEvent extends CompanyBpmnSignalEventDsl:

  val messageName = "mycompany-myproject-mysignal-{processInstanceId}"
  val descr: String = "my signal..."

  case class In(...)
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec
  
  lazy val example = signalEvent(In())
end MySignalEvent
```
- The `messageName` is the name of the signal you expect. 
  To correlate the signal to a certain process instance, you can use the `{processInstanceId}` as a part in the signal name.
  This will be replaced in the _Simulation_ with the actual _processInstanceId_.
- You can send process variables with the `In` object.
- A _SignalEvent_ extends _CompanyBpmnSignalEventDsl_.


