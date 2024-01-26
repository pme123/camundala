package camundala.api

import org.junit.*
import org.junit.Assert.*

class ApiCreatorTest extends munit.FunSuite, DefaultApiCreator:

  lazy val companyName = "MyCompany"
  
  lazy val projectName = "ApiCreatorTest"

  def jiraUrls: Map[String, String] = Map(
    "MAP" -> "https://myJira.ch/browse",
    "BPF" -> "https://finnovaJira.ch/browse"
  )
  def title: String = ???
  def version: String = ???

  test("testReplaceJira") {
    assertEquals(
      "[MAP-123](https://myJira.ch/browse/MAP-123): My test ticket.",
      replaceJira("MAP-123: My test ticket.", jiraUrls)
    )
  }

  test("testReplaceJiraMany") {
    assertEquals(
      "[MAP-123](https://myJira.ch/browse/MAP-123): My test ticket with [MAP-456](https://myJira.ch/browse/MAP-456).",
      replaceJira("MAP-123: My test ticket with MAP-456.", jiraUrls)
    )
  }
  test("testReplaceJiraMulti") {
    assertEquals(
      "[MAP-123](https://myJira.ch/browse/MAP-123): My test ticket with [BPF-456](https://finnovaJira.ch/browse/BPF-456).",
      replaceJira("MAP-123: My test ticket with BPF-456.", jiraUrls)
    )
  }
end ApiCreatorTest
