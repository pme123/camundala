package camundala

import camundala.tools.behaviors.GeneratesDsl

package object tools {


  implicit class GeneratesDslOps[A: GeneratesDsl](a: A) {

    def generate(): String =
      GeneratesDsl[A].generate(a)

    def generateChildren(): String =
      GeneratesDsl[A].generateChildren(a)
  }
}
