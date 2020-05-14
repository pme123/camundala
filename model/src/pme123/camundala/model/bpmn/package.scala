package pme123.camundala.model

import eu.timepit.refined.{W, refineV}
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

  def bpmnIdFromFileName(fileName: FileName): ZIO[Any, ModelException, BpmnId] =
    bpmnIdFromStr(fileName.value)

  def bpmnIdFromStr(str: String): ZIO[Any, ModelException, BpmnId] =
    ZIO.fromEither(refineV[IdRegex](str.split("/").last))
      .mapError(ex => ModelException(s"Could not map $str to BpmnId:\n $ex"))

  def bpmnNodeIdFromStr(str: String): ZIO[Any, ModelException, BpmnNodeId] =
    ZIO.fromEither(refineV[IdRegex](str))
      .mapError(ex => ModelException(s"Could not map $str to BpmnNodeId:\n $ex"))

  def fileNameFromBpmnId(bpmnId: BpmnId): ZIO[Any, ModelException, FileName] =
    fileNameFromStr(bpmnId.value)

  def fileNameFromStr(fileName: String): ZIO[Any, ModelException, FileName] =
    ZIO.fromEither(refineV[FileNameRegex](fileName))
      .mapError(ex => ModelException(s"Could not create FileName $fileName:\n $ex"))
}
