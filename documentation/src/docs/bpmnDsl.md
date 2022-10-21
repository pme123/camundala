# BPMN DSL

This _DSL_ will bring your domain into your _BPMN-Process_.

Its elements are more or less constructors with the same structure:

```scala
BPMN_ELEMENT(
  id: String,
  in: Input,
  out: Output,
  descr: Optable[String]
)
```

So each BPMN Element has:

- _id_: a unique identifier, depending on the element it must be unique within its process, or within a Camunda Instance (_process, dmn_).
- _in_: an input object that we descibed in the [_Domain Specification_](specification.md).
- _out_: an output object that we descibed in the [_Domain Specification_](specification.md).
- _descr_: an optional description of this element.

Here is an example:
```scala
process(
  id = InvoiceReceiptPIdent,
  descr = "This starts the Invoice Receipt Process.",
  in = InvoiceReceipt(),
  out = InvoiceReceiptCheck() // just for testing
)
```

The element is a _process_ with its inputs and outputs. As we also want to test its execution, 
we defined also an output, also the process does not have one.

@:callout(info)
If your element has no Input and/or Output, just leave it empty, as this is the default case.

```scala
process(
  id = MyDoItItselfProcess
)
```
@:@

We only support elements you can interact with. The next subchapters describe them with an example.

## Process

We already showed a process example above. Here the sub process _Review Invoice_:

```scala
process(
  id = "ReviewInvoiceP",
  descr = "This starts the Review Invoice Process.",
  in = InvoiceReceipt(),
  out = InvoiceReviewed()
)
```

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
    decisionDefinitionKey = "singleEntry",
    in = Input("A"),
    out = 1
  )
```

### singleResult

This is a single result with more than one value (_domain object_).

```scala
singleResult(
    decisionDefinitionKey = "singleResult",
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
)
```

### collectEntries

This is a list of _simple values_.

```scala
collectEntries(
    decisionDefinitionKey = "collectEntries",
    in = Input("A"),
    out = Seq(1, 2)
  )
```

### resultList

This is a list of _domain objects_.

```scala
resultList(
    decisionDefinitionKey = "resultList",
    in = Input("A"),
    out = List(ManyOutResult(1, "🤩"), ManyOutResult(2, "😂"))
  )
```

## User Tasks
.. TODO