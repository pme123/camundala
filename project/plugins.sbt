addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")

// https://github.com/djspiewak/sbt-github-actions
//addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.13.0")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")
addSbtPlugin("org.typelevel" % "laika-sbt" % "1.2.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")

addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
