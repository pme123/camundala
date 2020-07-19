package pme123.camundala.model

import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean._
import eu.timepit.refined.collection.{MinSize, NonEmpty}
import eu.timepit.refined.string.{MatchesRegex, Trimmed}
import zio.ZIO

package object bpmn {

  // NCName see https://stackoverflow.com/questions/1631396/what-is-an-xsncname-type-and-when-should-it-be-used
  type IdRegex = MatchesRegex["""^[a-zA-Z_][\w\-\.]+$"""]
  type FileNameRegex = IdRegex
  type FilePathRegex = MatchesRegex["""^[a-zA-Z_]+[\w\-\.\/]+$"""]
  type PathElemRegex = MatchesRegex["""^[%\w_\.\$]+[\w\-\.\/\$]*$"""]
  type GroupsAndUsersRegex = MatchesRegex["""^[\w]+[\w\-,]*$"""]
  type EmailRegex = MatchesRegex["""(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"""]

  type ProcessId = String Refined IdRegex
  type BpmnNodeId = String Refined IdRegex
  type BpmnId = String Refined IdRegex
  type FileName = String Refined FileNameRegex
  type FilePath = String Refined FilePathRegex
  type PathElem = String Refined PathElemRegex
  type PropKey = String Refined IdRegex
  type Identifier = String Refined IdRegex
  type JsonPath = Seq[String] //Refined  collection.MinSize[2]

  type DeployId = String Refined IdRegex
  val DeployId: DeployId = "default"
  type ProjectName = String Refined IdRegex

  type Url = String Refined string.Url
  type Port = Int Refined numeric.Greater[0]

  //TODO problem in zio-config with And
  type TrimmedNonEmpty = /*Trimmed And*/ NonEmpty
  type Username = String Refined TrimmedNonEmpty
  type Password = String Refined TrimmedNonEmpty
  type Email = String Refined EmailRegex

  val KeyDelimeter = "__"

  def bpmnIdFromFilePath(fileName: FilePath): ZIO[Any, ModelException, BpmnId] =
    bpmnIdFromStr(fileName.value)

  def bpmnIdFromStr(str: String): ZIO[Any, ModelException, BpmnId] =
    ZIO.fromEither(refineV[IdRegex](str.split("/").last))
      .mapError(ex => ModelException(s"Could not map $str to BpmnId:\n $ex"))

  def bpmnNodeIdFromStr(str: String): ZIO[Any, ModelException, BpmnNodeId] =
    ZIO.fromEither(refineV[IdRegex](str))
      .mapError(ex => ModelException(s"Could not map $str to BpmnNodeId:\n $ex"))

  def processIdFromStr(str: String): ZIO[Any, ModelException, ProcessId] =
    ZIO.fromEither(refineV[IdRegex](str))
      .mapError(ex => ModelException(s"Could not map $str to ProcessId:\n $ex"))

  def deployIdFromStr(str: String): ZIO[Any, ModelException, DeployId] =
    ZIO.fromEither(refineV[IdRegex](str))
      .mapError(ex => ModelException(s"Could not map $str to DeployId:\n $ex"))

  def filePathFromBpmnId(bpmnId: BpmnId): ZIO[Any, ModelException, FilePath] =
    filePathFromStr(bpmnId.value)

  def filePathFromStr(filePath: String): ZIO[Any, ModelException, FilePath] =
    ZIO.fromEither(refineV[FilePathRegex](filePath))
      .mapError(ex => ModelException(s"Could not create FilePath $filePath:\n $ex"))

  def pathElemFromStr(pathElem: String): ZIO[Any, ModelException, PathElem] =
    ZIO.fromEither(refineV[PathElemRegex](pathElem))
      .mapError(ex => ModelException(s"Could not create PathElem $pathElem:\n $ex"))

  def propKeyFromStr(propKey: String): ZIO[Any, ModelException, PropKey] =
    ZIO.fromEither(refineV[IdRegex](propKey))
      .mapError(ex => ModelException(s"Could not create PropKey $propKey:\n $ex"))

  def urlFromStr(url: String): ZIO[Any, ModelException, Url] =
    ZIO.fromEither(refineV[string.Url](url))
      .mapError(ex => ModelException(s"Could not create Url $url:\n $ex"))

  def passwordFromStr(password: String): ZIO[Any, ModelException, Password] =
    ZIO.fromEither(refineV[TrimmedNonEmpty](password))
      .mapError(ex => ModelException(s"Could not create Password $password:\n $ex"))

  def usernameFromStr(username: String): ZIO[Any, ModelException, Username] =
    ZIO.fromEither(refineV[TrimmedNonEmpty](username))
      .mapError(ex => ModelException(s"Could not create Username $username:\n $ex"))

  def idAsVal(id: String): String =
    id.split("""[_.-]""")
      .toList match {
      case Nil => ""
      case head :: Nil => head
      case head :: tail => head +
        tail.map(str => str.head.toUpper +: str.drop(1)).mkString
    }

  def generateTitle(text: String): String =
    s"""
       |/${"*" * 50}
       | * $text
       | ${"*" * 50}/
       | """.stripMargin
}
