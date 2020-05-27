package pme123.camundala.camunda

import org.camunda.bpm.engine.ProcessEngine
import pme123.camundala.model.bpmn.{CamundalaException, Group, User}
import zio.logging.{Logger, Logging}
import zio.{Has, Task, ZLayer}
object userManagement {
  type UserManagement = Has[Service]

  trait Service {
    def initGroupsAndUsers(groups: Seq[Group], users: Seq[User]): Task[Unit]

    def createGroup(group: Group): Task[Group]

    def createUser(user: User): Task[User]
  }

  type UserManagementDeps = Has[() => ProcessEngine] with Logging

  lazy val live: ZLayer[UserManagementDeps, Throwable, UserManagement] =
    ZLayer.fromServices[() => ProcessEngine, Logger[String], Service] {
      (processEngine, log) =>

        new Service {
          def createGroup(group: Group): Task[Group] = {
            lazy val create =
              for {
                identityService <- Task(processEngine().getIdentityService)
                groupEntity <- Task(identityService.newGroup(group.id.value))
                _ <- Task {
                  group.maybeName.foreach(groupEntity.setName)
                  groupEntity.setType(group.`type`.value)
                }
                _ <- Task(identityService.saveGroup(groupEntity))
                _ <- log.debug(s"Created Group ${group.id}")
              } yield ()

            (for {
              identityService <- Task(processEngine().getIdentityService)
              existing <- Task(identityService.createGroupQuery().groupId(group.id.value).count())
              _ <- if (existing == 0)
                create
              else
                log.debug(s"Group ${group.id} exists")
            } yield group)
              .mapError(ex => UserManagementException(s"Problem Creating Group $group", Some(ex)))
          }

          def createUser(user: User): Task[User] = {
            lazy val create = for {
              identityService <- Task(processEngine().getIdentityService)
              userEntity <- Task(identityService.newUser(user.username.value))
              _ <- Task {
                user.maybeName.foreach(userEntity.setLastName)
                user.maybeFirstName.foreach(userEntity.setFirstName)
                user.maybeEmail.foreach(e => userEntity.setEmail(e.value))
                userEntity.setPassword(userEntity.getId)
              }
              _ <- Task(identityService.saveUser(userEntity))
              _ <- log.debug(s"Created User ${user.username}")
            } yield ()

            for {
              identityService <- Task(processEngine().getIdentityService)
              query = identityService.createUserQuery().userId(user.username.value)
              existing <- Task(query.count())
              _ <- if (existing == 0)
                create
              else
                log.debug(s"User ${user.username} exists")
              _ <- Task.foreach(user.groups)(group =>
                if (query.memberOfGroup(group.id.value).count() == 0)
                Task(identityService.createMembership(user.username.value, group.id.value)) *>
                  log.debug(s"Membership ${user.username} > ${group.id} created")
              else log.debug(s"Membership ${user.username} > ${group.id} exists"))
            } yield user
          }.mapError(ex => UserManagementException(s"Problem Creating User $user", Some(ex)))


          def initGroupsAndUsers(groups: Seq[Group], users: Seq[User]): Task[Unit] =
            for {
              _ <- Task.foreach(groups)(createGroup)
              _ <- Task.foreach(users)(createUser)
            } yield ()
        }
    }

  case class UserManagementException(msg: String,
                                     override val cause: Option[Throwable] = None)
    extends CamundalaException

}
