package camundala.dsl


import eu.timepit.refined.auto._

sealed trait Constraint {
  def name: Identifier

  def config: Option[String]
}

object Constraint {

  case class Custom(name: Identifier, config: Option[String]) extends Constraint

  case object Required extends Constraint {
    val name: Identifier = "required"

    val config: Option[String] = None
  }

  case object Readonly extends Constraint {
    val name: Identifier = "readonly"

    val config: Option[String] = None
  }

  sealed trait MinMax extends Constraint {

    def value: Int

    val config: Option[String] = Some(s"$value")
  }

  case class Minlength(value: Int) extends MinMax {
    val name: Identifier = "minlength"
  }

  case class Maxlength(value: Int) extends MinMax {
    val name: Identifier = "maxlength"
  }

  case class Min(value: Int) extends MinMax {
    val name: Identifier = "min"
  }

  case class Max(value: Int) extends MinMax {
    val name: Identifier = "max"
  }

}



