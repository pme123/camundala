package camundala.helper.util

import munit.FunSuite

class VersionHelperTest extends FunSuite:

  test("repoSearch".ignore) {
    assertEquals(
      VersionHelper.repoSearch("camundala-api_3", "io.github.pme123"),
      "1.30.25" // This is the latest released version
    )
  }
  test("repoSearch no result") {
    assertEquals(
      VersionHelper.repoSearch("camundala-apix_3", "io.github.pme123"),
      "VERSION NOT FOUND"
    )
  }
