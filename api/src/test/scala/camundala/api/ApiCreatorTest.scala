package camundala.api

import org.junit.*
import org.junit.Assert.*

class ApiCreatorTest extends APICreator {

  override def jiraUrls: Map[String, String] = Map(
    "MAP" -> "https://myJira.ch/browse",
    "BPF" -> "https://finnovaJira.ch/browse",
  )
  def title: String = ???
  def version: String = ???
  protected def docProjectUrl(project: String): String = ???

  @Test
  def testReplaceJira(): Unit =
    assertEquals("[MAP-123](https://myJira.ch/browse/MAP-123): My test ticket.",
      replaceJira("MAP-123: My test ticket.", jiraUrls))

  @Test
  def testReplaceJiraMany(): Unit =
    assertEquals("[MAP-123](https://myJira.ch/browse/MAP-123): My test ticket with [MAP-456](https://myJira.ch/browse/MAP-456).",
      replaceJira("MAP-123: My test ticket with MAP-456.", jiraUrls))

  @Test
  def testReplaceJiraMulti(): Unit =
    assertEquals("[MAP-123](https://myJira.ch/browse/MAP-123): My test ticket with [BPF-456](https://finnovaJira.ch/browse/BPF-456).",
      replaceJira("MAP-123: My test ticket with BPF-456.", jiraUrls))
}
