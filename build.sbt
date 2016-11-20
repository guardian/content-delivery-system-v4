name := "content-delivery-system-v4"

version := "4.0"

scalaVersion := "2.11.8"

packAutoSettings
// mainClass in assembly := Some("mainclass")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

// https://mvnrepository.com/artifact/org.scalamock/scalamock-core_2.11
libraryDependencies += "org.scalamock" % "scalamock-core_2.11" % "3.3.0"

// https://mvnrepository.com/artifact/org.scalamock/scalamock-scalatest-support_2.11
libraryDependencies += "org.scalamock" % "scalamock-scalatest-support_2.11" % "3.3.0"

libraryDependencies += "org.scala-lang" % "scala-xml" % "2.11.0-M4"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.7"

libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.7"

libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.8.2"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.2"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.2"

libraryDependencies += "org.yaml" % "snakeyaml" % "1.17"
// https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.11.2"
