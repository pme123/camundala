package camundala.helper.dev
package company

import camundala.helper.dev.update.{doNotAdjustText, howToResetText}

export camundala.helper.util.DevConfig
export camundala.helper.util.ModuleConfig

private val replaceHelperCompanyCommand ="../helperCompany.scala init"
lazy val helperCompanyDoNotAdjustText = doNotAdjustText(replaceHelperCompanyCommand)
lazy val helperCompanyHowToResetText = howToResetText(replaceHelperCompanyCommand)
