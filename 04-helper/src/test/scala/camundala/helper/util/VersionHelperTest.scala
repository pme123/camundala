package camundala.helper.util

import munit.FunSuite

class VersionHelperTest extends FunSuite:

  test("repoSearch".ignore) {
    assertEquals(
      VersionHelper.repoSearch("camundala-api_3", "io.github.pme123"),
      "1.30.24" // This is the latest released version
    )
  }
  test("repoSearch company") {
    assertEquals(
      VersionHelper.repoSearch("valiant-camundala-bpmn_3", "valiant"),
      "0.18.59" // This is the latest released version
    )
  }
  test("repoSearch no result") {
    assertEquals(
      VersionHelper.repoSearch("camundala-apix_3", "io.github.pme123"),
      "VERSION NOT FOUND"
    )
  }
