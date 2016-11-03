name := "content-delivery-system-v4"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies += "org.scala-lang" % "scala-xml" % "2.11.0-M4"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.7"

libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.7"

libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.8.2"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.2"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.2"

// https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.11.2"
