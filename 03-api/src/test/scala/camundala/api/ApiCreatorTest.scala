package camundala.api

import org.junit.*
import org.junit.Assert.*

class ApiCreatorTest extends munit.FunSuite, DefaultApiCreator:

  lazy val apiConfig = ApiConfig("DemoConfig")

  def jiraUrls: Map[String, String] = Map(
    "MAP" -> "https://myJira.ch/browse",
    "OTHER" -> "https://otherJira.ch/browse"
  )
  def title: String = ???
  def version: String = ???

  lazy val companyProjectVersion: String = "0.1.0"
  lazy val projectDescr: String = ""

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
      "[MAP-123](https://myJira.ch/browse/MAP-123): My test ticket with [OTHER-456](https://otherJira.ch/browse/OTHER-456).",
      replaceJira("MAP-123: My test ticket with OTHER-456.", jiraUrls)
    )
  }
end ApiCreatorTest
