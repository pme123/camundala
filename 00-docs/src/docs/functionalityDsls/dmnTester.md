# DMN Tester

You can integrate the DMN Tester in your project pretty simple. 

## Why

The _DMN Tester_ lets you easily validate your DMNs, that you create or get from the business analysts.

The _DMN Tester_ gives you a UI, to configure a test for a DMN. 
As there is already some information in your domain model, we must only define the rest.
And so we can directly run the tests, without configure them manually in the UI.

See [Github](https://github.com/camunda-community-hub/camunda-dmn-tester) for more information
on what the _DMN Tester_ is all about.

## Get Started
The _DMN Tester DSL_ use the DMNs you created - in this context I refer to the [Bpmn DSL](../bpmnDsl.md#business-rule-tasks-decision-dmns)

Let's start with a basic example:
```scala
// put your dmns in the dmn package of your project (main)
package camundala.examples.invoice.dmn
// import the projects bpmns (DMNs)
import camundala.examples.invoice.bpmn.*

object ProjectDmnTester 
  extends CompanyDmnTester:
      
      startDmnTester()

      createDmnConfigs(
          InvoiceAssignApproverDMN
            .testValues(_.amount, 249, 250, 999, 1000, 1001),
            .dmnPath("invoiceBusinessDecisions")
          // for demonstration - created unit test - acceptMissingRules just for demo
          InvoiceAssignApproverDmnUnit
            .acceptMissingRules
            .testUnit
            .dmnPath("invoiceBusinessDecisions")
            .inTestMode
        )

end ProjectDmnTester
```

### Run the DMN Tester
In your _sbt-console_:
 
  `dmn/run`

This starts the Docker container and makes the whole process pretty nice and fast. 
The following steps are done:

- Check if the Container is already running.
- If so - it checks if it is running for this project.
- If it is running for another project - it stops the Container.
- If not - the Container is started.

## createDmnConfigs
A DSL to create the DMN Tester configurations.

You start from the DMN, that you defined, here an example:

```scala
  lazy val InvoiceAssignApproverDMN = collectEntries(
    in = SelectApproverGroup(),
    out = Seq(ApproverGroup.management),
  )
```

Now you can add the following:

### .testValues
Define the input values for the DMN you want to test. 

For the following types this is done automatically:

- `boolean` -> `true` & `false`
- `enum` -> all values of this enumeration.

If an input attribute is **optional** (_Option_) it also will have a _null_ as a test input.

That said, you only need to define the rest of your inputs, like

```scala
  InvoiceAssignApproverDMN
    .testValues(_.amount, 249, 250, 999, 1000, 1001)
```

It starts with the name of the input (`_.amount`) and is followed by all test values with the according type.

@:callout(info)
The underline in `_.amount` is the input of the DMN (_for the coder: it is a function:_ `In => DmnValueType`). 
This makes sure the compiler checks if there is such an attribute.

```log
[error] -- [E008] Not Found Error: /Users/mpa/dev/Github/pme123/camundala/examples/invoice/camunda7/src/main/scala/camundala/examples/invoice/dmn/InvoiceDmnTesterConfigCreator.scala:27:20 
[error] 27 |      .testValues(_.amounts, 249, 250, 999, 1000, 1001),
[error]    |                  ^^^^^^^^^
[error]    |value amounts is not a member of camundala.examples.invoice.domain.SelectApproverGroup - did you mean _$1.amount?
```
@:@

### .testUnit
By default, a DMN Test is integrated - meaning that it will take all dependent inputs into account.

So if you have complex set of dependent DMN Tables you can test them separately, like:
```scala
.testUnit
```

### .dmnPath
To support different naming schemes, you can adjust the DMN file name the following way:

- Nothing to do, if the file name is `dmnBasePath / s"${decisionId.replace("mycompany-", "")}.dmn"`. (see configuration)
- The creation of the default path can be overridden: 
    ```scala
      protected def defaultDmnPath(dmnName: String): os.Path =
        dmnBasePath / s"$decisionId.dmn"
    ```
- A different name, but with the same _defaultDmnPath_:
    ```scala
      .dmnPath("invoiceBusinessDecisions")
    ``` 
- An entirely different path (using _os.Path_):
    ```scala
      .dmnPath(os.pwd / "mySpecial.dmn")
    ``` 
  
### .acceptMissingRules
Sometimes you have a lot of rules that you don't want to test all.
Adding `.acceptMissingRules` will allow missing rules in your test.

### .inTestMode
When you validated a test result, you can create Test Cases.
If you do so, you must add `.inTestMode`, 
otherwise the configuration will be overridden, when running the DMN Tester the next time.

## Variables
If you have dynamic content in your DMN (input or output), you need to add them as well.

To distinguish them from testing inputs, we wrap them in a _DmnVariable_ class.

@:callout(info)
Camunda DMN Engine handles Variables and Test Inputs exactly the same.

We distinguish them, because Variables are not important for the matching process.
So we do not need to have different values for them.

We recommend not to use dynamic values in inputs of rules. 
If you do the variable will rather be a test input.

@:@

Example:

```scala
  case class Input(letters: String = "A_dynamic_2",
                   inputVariable: DmnVariable[String] = DmnVariable("dynamic"),
                   outputVariable: DmnVariable[String] = DmnVariable("dynamicOut")
                  )
```

![Variables DMN](images/dmnTester_variables.png)

In this example the input must be `A_dynamic_2` to match the first rule. 
So it is a corner case if this is rather a test input.

The output variable can be whatever you want.

@:callout(info)
Be aware that you must run the DMN Tester again, whenever you made changes (`sbt dmn/run`).
@:@

## Configuration
See [03-dmn].

## Problem Handling

The DMN Tester is run on Docker.
So to find problems, you have:

- For the server: the Docker Console
- For the client: the Browser Console

If you are stuck, or find a problem, please create an issue on Github.





