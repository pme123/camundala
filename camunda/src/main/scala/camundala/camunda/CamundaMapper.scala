package camundala
package camunda

import bpmn.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import CamundaMapperMacros.*
import scala.compiletime.*
import scala.quoted.{Expr, Quotes}

import scala.annotation.compileTimeOnly

trait CamundaMapper:
  extension [In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema](process: Process[In, Out])

    inline def mapOut[A](inline path: Out => A) = ${ mapImpl('path) }
    inline def mapIn[A](inline path: In => A) = ${ mapImpl('path) }

object CamundaMapper extends CamundaMapper, BpmnDsl,App:

  val p = process(
    "testProcess",
    TestIn(),
    TestOut()
  )
  private val value: Any = p.mapOut(_.t2.each.other)
  println(s"REsult: $value \n" + value.getClass)

case class Mapping[From, To](fromPath: Seq[String])

case class TestIn(name: String = "Peter", t2: T2 = T2())
case class TestOut(hello: String = "Ferry", t2: Option[T2] = Some(T2()))

case class T2(okidoki:String = "???", other: Boolean = true)

trait MapperFunctor[F[_]] {
  def map[A, B](fa: F[A], f: A => B): F[B]
  def each[A](fa: F[A], f: A => A): F[A] = map(fa, f)
  def eachWhere[A](fa: F[A], f: A => A, cond: A => Boolean): F[A] = map(fa, x => if cond(x) then f(x) else x)
}

object MapperFunctor :
  given MapperFunctor[List] with {
    def map[A, B](fa: List[A], f: A => B): List[B] = fa.map(f)
  }

  given MapperFunctor[Seq] with {
    def map[A, B](fa: Seq[A], f: A => B): Seq[B] = fa.map(f)
  }

  given MapperFunctor[Option] with {
    def map[A, B](fa: Option[A], f: A => B): Option[B] = fa.map(f)
  }

extension [F[_]: MapperFunctor, A](fa: F[A])
  @compileTimeOnly("each can only be used as a path component inside mapIn/mapOut")
  def each: A = ???

