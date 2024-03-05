
import mainargs._
@

import $ivy.`io.github.pme123:camundala-helper_3:1.29.0-SNAPSHOT compat`, camundala.helper.setup._


/**
 * Usage see `valiant.camundala.helper.UpdateHelper`
 */
@main(doc =
  """> Creates the directories and generic files for the company BPMN Projects
   """)
def create(
    @arg(doc = "The company name - should be generated automatically after creation.")
    companyName: String,
): Unit =
  SetupCompanyCreator(companyName).create


