package pme123.camundala.model

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex
import zio.ZIO

package object bpmn {

  // NCName see https://stackoverflow.com/questions/1631396/what-is-an-xsncname-type-and-when-should-it-be-used
  type IdRegex = MatchesRegex["""^[a-zA-Z_][\w|\-|\.]+$"""]
  type FileNameRegex = IdRegex
  type FilePathRegex = MatchesRegex["""^[a-zA-Z_]+[\w\-\.\/]+$"""]

  type ProcessId = String Refined IdRegex
  type BpmnNodeId = String Refined IdRegex
  type BpmnId = String Refined IdRegex
  type FileName = String Refined FileNameRegex
  type FilePath = String Refined FilePathRegex
  type PropKey = String Refined IdRegex

  case class Sensitive(value: String) {
    override def toString: String = "*" * 10
  }

  def bpmnIdFromFilePath(fileName: FilePath): ZIO[Any, ModelException, BpmnId] =
    bpmnIdFromStr(fileName.value)

  def bpmnIdFromStr(str: String): ZIO[Any, ModelException, BpmnId] =
    ZIO.fromEither(refineV[IdRegex](str.split("/").last))
      .mapError(ex => ModelException(s"Could not map $str to BpmnId:\n $ex"))

  def bpmnNodeIdFromStr(str: String): ZIO[Any, ModelException, BpmnNodeId] =
    ZIO.fromEither(refineV[IdRegex](str))
      .mapError(ex => ModelException(s"Could not map $str to BpmnNodeId:\n $ex"))

  def filePathFromBpmnId(bpmnId: BpmnId): ZIO[Any, ModelException, FilePath] =
    filePathFromStr(bpmnId.value)

  def filePathFromStr(filePath: String): ZIO[Any, ModelException, FilePath] =
    ZIO.fromEither(refineV[FilePathRegex](filePath))
      .mapError(ex => ModelException(s"Could not create FilePath $filePath:\n $ex"))
}
