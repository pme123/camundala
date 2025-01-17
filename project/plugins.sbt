addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.0")

// https://github.com/djspiewak/sbt-github-actions
//addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.13.0")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.2")
addSbtPlugin("org.typelevel"  % "laika-sbt"      % "1.3.1")
addSbtPlugin("org.scalameta"  % "sbt-mdoc"       % "2.6.2")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")

addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
