package pme123.camundala.examples.playground

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.{Group, User}

object UsersAndGroups {
  val worker: Group =
    Group("worker")
      .name("Worker")
  val guest: Group =
    Group("guest")
      .name("Guest")

  val hans: User =
    User("hans")
      .name("Müller")
      .firstName("Hans")
      .email("hans@mueller.ch")
      .group(worker)
  val heidi: User =
    User("heidi")
      .name("Meier")
      .firstName("Heidi")
      .email("heidi@meier.ch")
      .group(guest)
  val peter: User =
    User("peter")
      .name("Arnold")
      .firstName("Peter")
      .email("peter@arnold.ch")
      .group(guest)
      .group(worker)

  val kermit: User =
    User("kermit")
      .group(guest)
      .group(worker)

  val adminGroup: Group =
    Group("admin")
      .name("admin")
      .groupType("SYSTEM")

  val userGroup: Group =
    Group("user")
      .name("user")
      .groupType("BPF")

  val adminUser: User =
    User("adminUser")
      .firstName("Admin")
      .name("User")
      .group(guest)
      .group(worker)
      .group(adminGroup)
      .group(userGroup)
}
