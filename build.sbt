name := "content-delivery-system-v4"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

val scalaMockVersion = "3.6.0"
// https://mvnrepository.com/artifact/org.scalamock/scalamock-core
libraryDependencies += "org.scalamock" %% "scalamock-core" % scalaMockVersion

// https://mvnrepository.com/artifact/org.scalamock/scalamock-scalatest-support_2.11
libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % scalaMockVersion

libraryDependencies += "org.scala-lang" % "scala-xml" % "2.11.0-M4"

val log4jVersion = "2.11.1"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % log4jVersion

libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % log4jVersion


val jacksonVersion = "2.9.6"
//update vulnerable jackson-databind
// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion

libraryDependencies += "org.yaml" % "snakeyaml" % "1.17"
// https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.23.1"
