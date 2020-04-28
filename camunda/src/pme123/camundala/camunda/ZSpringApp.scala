package pme123.camundala.camunda

import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import pme123.camundala.model.bpmnRegister.BpmnRegister
import pme123.camundala.model.{Bpmn, bpmnRegister}
import zio._
import zio.console.Console


trait ZSpringApp  extends zio.App {

  protected def registerBpmns(bpmns: Set[Bpmn]):  URIO[BpmnRegister, List[Unit]] =
    ZIO.foreach(bpmns.toSeq)(b => bpmnRegister.registerBpmn(b))

  /**
    * create SpringApplication as a ZManaged Resource.
    */
  protected def managedSpringApp(clazz: Class[_], args: List[String]): ZManaged[Console, Throwable, ConfigurableApplicationContext] =
    ZManaged.make(
      console.putStrLn("Starting Spring Container...") *>
        ZIO.effect(
          SpringApplication.run(clazz, args: _*)
        )
    )(ctx =>
      console.putStrLn("Spring Container Stopping...") *>
        ZIO.effect(
          if (ctx.isActive)
            SpringApplication.exit(ctx)
        ).catchAll(ex =>
          console.putStrLn(s"Problem shutting down the Spring Container.\n${ex.getMessage}")
        )
    )
}
