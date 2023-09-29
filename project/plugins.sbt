addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.19")

// https://github.com/djspiewak/sbt-github-actions
//addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.13.0")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")
addSbtPlugin("org.planet42" % "laika-sbt" % "0.19.1")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")

addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
