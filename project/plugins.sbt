addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.4")

// https://github.com/djspiewak/sbt-github-actions
//addSbtPlugin("com.codecommit" % "sbt-github-actions" % "0.13.0")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.0")
addSbtPlugin("org.typelevel"  % "laika-sbt"      % "1.3.0")
addSbtPlugin("org.scalameta"  % "sbt-mdoc"       % "2.6.2")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")

addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
