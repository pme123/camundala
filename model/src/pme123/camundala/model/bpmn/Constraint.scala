package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._


sealed trait Constraint {
  def name: PropKey

  def config: Option[String]
}

object Constraint {

  case class Custom(name: PropKey, config: Option[String]) extends Constraint

  case object Required extends Constraint {
    val name: PropKey = "required"

    val config: Option[String] = None
  }

  case object Readonly extends Constraint {
    val name: PropKey = "readonly"

    val config: Option[String] = None
  }

  sealed trait MinMax extends Constraint {

    def value: Int

    val config: Option[String] = Some(s"$value")
  }

  case class Minlength(value: Int) extends MinMax {
    val name: PropKey = "minlength"
  }

  case class Maxlength(value: Int) extends MinMax {
    val name: PropKey = "maxlength"
  }

  case class Min(value: Int) extends MinMax {
    val name: PropKey = "min"
  }

  case class Max(value: Int) extends MinMax {
    val name: PropKey = "max"
  }

}
