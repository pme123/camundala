package camundala.dsl

import camundala.dsl.GeneratedForm.{booleanField, textField}
import camundala.dsl.TestData.groups.{GroupOne, GroupTwo}
import eu.timepit.refined.auto._


//noinspection TypeAnnotation
object TestData {

  // fluent
  val fluentBpmn =
    bpmn("FluentBpmnExample") --- (
      process("FluentProcess")
        .canStart(
          user("FluentUser")
            .firstName("Peter")
            .name("Meier")
        )
        .canStart(
          group("FluentGroup")
            .groupType("TestGroup")
            .name("Fancy Name")
        ) ---
        startEvent("MyStart")
        --- (
        userTask("UserTask1")
          .canEdit(user("UserONE")
            .firstName("Peter")
            .name("Meier")
            .isInGroups(GroupOne, GroupTwo))
          .embeddedForm("myEmbededForm"),
        userTask("UserTask2")
          .canEdit(group("MyGroupOne")
            .groupType("TestGroup")
            .name("Fancy Name"))
          .form(generatedForm()
            .fields(
              textField("id"),
              booleanField("isItTrue")
                .required,
              textField("helloThere")
                .label("Hello There!")
                .default("Hoi")
                .minlength(12)
                .maxlength(123)
                .prop("width", "12")
            ))
          .prop("color", "blue")
          .prop("size", "huge")
          .inputText("textField", s"$${hello}")
          .inputGroovy("groovyInline", "println('Groovy rocks')")
          .inputGroovyRef("groovyFile", "println('Groovy rocks again')")
      ) --- (
        serviceTask("ServiceTask1")
          .prop("color", "red")
          .prop("size", "small")
          .inputText("textField", s"$${hello}")
          .inputGroovy("groovyInline", "println('Groovy rocks')")
          .inputGroovyRef("groovyFile", "println('Groovy rocks again')")
        ) --- (
        sendTask("SendTask1")
          .prop("color", "red")
        ) --- (
        businessRuleTask("BusinessRuleTask2")
          .prop("color", "blue")
        )
      )

  // separated
  object bpmns {

    import processes._

    val SeparatedBpmn1 =
      bpmn("BpmnExample1")
        .processes(
          SeparatedProcess1,
          SeparatedProcess2
        )

    val SeparatedBpmn2 =
      bpmn("BpmnExample2")
        .processes(
          SeparatedProcess1
        )
  }

  object processes {

    import businessRuleTasks._
    import callActivities._
    import groups._
    import sendTasks._
    import serviceTasks._
    import startEvents._
    import endEvents._
    import userTasks._
    import users._
    import sequenceFlows._

    val SeparatedProcess1 =
      process("SeparatedProcess1")
        .canStart(
          UserOne)
        .canStart(
          GroupTwo)
        .starts(
          StartEvent1)
        .ends(
          EndEvent1)
        .userTasks(
          UserTask1,
          UserTask2)
        .serviceTasks(
          ServiceTask1,
          ServiceTask2)
        .callActivities(
          CallActivity1,
          CallActivity2
        )

    val SeparatedProcess2 =
      process("SeparatedProcess2")
        .canStart(
          UserTwo
        ) --- (
        StartEvent2
        ) --- (
        EndEvent2
        ) --- (
        UserTask1,
        UserTask2
      ) --- (
        ServiceTask1,
        ServiceTask2
      ) --- (
        SendTask1,
        SendTask2
      ) --- (
        BusinessRuleTask1,
        BusinessRuleTask2
      ) --- (
        CallActivity1,
        CallActivity2
      )
  }

  object groups {

    val GroupOne =
      group("MyGroupOne")
        .groupType("TestGroup")
        .name("Fancy Name")
    val GroupTwo =
      group("MyGroupTwo")
  }

  object users {

    import groups._

    val UserOne =
      user("UserONE")
        .firstName("Peter")
        .name("Meier")
        .isInGroups(GroupOne, GroupTwo)

    val UserTwo =
      user("UserTWO")
        .firstName("Heidi")
        .name("Müller")
        .email("heiri@mueller.ch")
        .isInGroups(GroupOne, GroupTwo)
  }

  object userTasks {

    import forms._
    import groups._
    import users._

    val UserTask1 =
      userTask("UserTask1")
        .canEdit(UserOne)
        .canEdit(GroupTwo)
        .form(EmbeddedForm1)
        .prop("color", "red")
        .prop("size", "small")
        .inputText("textField", s"$${hello}")
        .inputGroovy("groovyInline", "println('Groovy rocks')")
        .inputGroovyRef("groovyFile", "println('Groovy rocks again')")

    val UserTask2 =
      userTask("UserTask2")
        .canEdit(GroupTwo, GroupOne)
        .form(GeneratedForm1)
        .prop("color", "blue")
        .prop("size", "huge")
        .outputText("textField", s"$${hello}")
        .outputGroovy("groovyInline", "println('Groovy rocks')")
        .outputGroovyRef("groovyFile", "println('Groovy rocks again')")

  }

  object serviceTasks {

    val ServiceTask1 =
      serviceTask("ServiceTask1")
        .external("MyTopic")
        .prop("color", "red")
        .prop("size", "small")
        .inputText("textField", s"$${hello}")
        .inputGroovy("groovyInline", "println('Groovy rocks')")
        .inputGroovyRef("groovyFile", "println('Groovy rocks again')")

    val ServiceTask2 =
      serviceTask("ServiceTask2")
        .delegate("#MyService")
        .prop("color", "blue")
        .prop("size", "huge")
        .outputText("textField", s"$${hello}")
        .outputGroovy("groovyInline", "println('Groovy rocks')")
        .outputGroovyRef("groovyFile", "println('Groovy rocks again')")

  }

  object sendTasks {

    val SendTask1 =
      sendTask("SendTask1")
        .expression(s"$${send}")
        .prop("color", "red")
        .prop("size", "small")
        .inputText("textField", s"$${hello}")
        .inputGroovy("groovyInline", "println('Groovy rocks')")
        .inputGroovyRef("groovyFile", "println('Groovy rocks again')")

    val SendTask2 =
      sendTask("SendTask2")
        .external("SendIt")
        .prop("color", "blue")
        .prop("size", "huge")
        .outputText("textField", s"$${hello}")
        .outputGroovy("groovyInline", "println('Groovy rocks')")
        .outputGroovyRef("groovyFile", "println('Groovy rocks again')")

  }

  object businessRuleTasks {

    val BusinessRuleTask1 =
      businessRuleTask("BusinessRuleTask1")
        .dmn("MyDmnTable.dmn")
        .prop("color", "red")
        .prop("size", "small")
        .inputText("textField", s"$${hello}")
        .inputGroovy("groovyInline", "println('Groovy rocks')")
        .inputGroovyRef("groovyFile", "println('Groovy rocks again')")

    val BusinessRuleTask2 =
      businessRuleTask("BusinessRuleTask2")
        .prop("color", "blue")
        .prop("size", "huge")
        .outputText("textField", s"$${hello}")
        .outputGroovy("groovyInline", "println('Groovy rocks')")
        .outputGroovyRef("groovyFile", "println('Groovy rocks again')")

  }

  object callActivities {
    val CallActivity1 =
      callActivity("CallActivity1")
        .prop("size", "huge")
        .outputText("textField", s"$${hello}")

    val CallActivity2 =
      callActivity("CallActivity2")
        .prop("size", "small")
        .inputText("textField", s"$${hello}")
  }

  object startEvents {

    import forms._

    val StartEvent1 = startEvent("StartMyProcess")
      .embeddedForm("InlinedForm")
      .prop("color", "blue")
      .prop("size", "huge")

    val StartEvent2 =
      startEvent("LetsGo")
        .form(GeneratedForm1)

  }

  object endEvents {

    val EndEvent1 = endEvent("Done")
      .prop("color", "blue")
      .prop("size", "huge")

    val EndEvent2 =
      endEvent("Did it")
        .inputText("ok", "not so much")
  }

  object sequenceFlows {

    val SequenceFlow1 = sequenceFlow("SequenceFlow1")
      .groovy("count > 3")
      .prop("color", "blue")
      .prop("size", "huge")

    val SequenceFlow2 =
      sequenceFlow("SequenceFlow2")

  }

  object forms {

    val EmbeddedForm1 =
      embeddedForm("myEmbededForm")

    import GeneratedForm._

    val GeneratedForm1 =
      generatedForm()
        .fields(
          textField("id"),
          booleanField("isItTrue")
            .required,
          textField("helloThere")
            .label("Hello There!")
            .default("Hoi")
            .minlength(12)
            .maxlength(123)
            .prop("width", "12")

        )
  }

}
