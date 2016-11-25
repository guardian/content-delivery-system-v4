package CDS

import java.io.InputStream

import logging.LogMessage
import logging.LogCollection
import java.nio.file.{Files, Path, Paths}

import datastore.Datastore
import config.CDSConfig

import scala.io.Source

object CDSReturnCode extends Enumeration {
  val SUCCESS, NOTFOUND, FAILURE, STOPROUTE, UNKNOWN = Value
}

case class CDSMethod(methodType: String,
                     name:String,
                     requiredFiles: Seq[String],
                     params: Map[String,String],
                     log:LogCollection,
                     store:Option[Datastore],
                     config: CDSConfig)
    extends ExternalCommand {

  val METHODS_BASE_PATH = config.paths.getOrElse("methods","/usr/local/lib/cds_backend")

  def findFile:Option[String] = {
    val extensions = List("",".rb",".pl",".py",".js",".sh")

    val filesList = extensions
      .map(xtn=>Paths.get(METHODS_BASE_PATH,name + xtn))
      .filter(path=>Files.exists(path))

    if(filesList.length>1) log.warn("Multiple methods found for " + name + ":" + filesList,null)
    filesList.headOption.map(_.toString) //headOption gives an option with Some(value) if has item or None if empty
  }

  override def errHandler(input: InputStream): Unit = {
    for(line <- Source.fromInputStream(input).getLines()){
      log.relayMessage(LogMessage.fromString(line,Some(this)))
    }
  }

  override def outputHandler(input: InputStream): Unit = {
    for(line <- Source.fromInputStream(input).getLines()){
      log.relayMessage(LogMessage.fromString(line,Some(this)))
    }
  }

  def execute:CDSReturnCode.Value = {
    log.log("Executing method " + name + " as " + methodType,Some(this))

    findFile match {
      case None=>
        log.error("Could not find executable for "+name+" in "+ METHODS_BASE_PATH,None)
        CDSReturnCode.NOTFOUND
      case Some(path)=>
        val p = runCommand(path.toString,Seq(),params)
        p.exitValue() match {
          case 0=>
            log.log("Method exited cleanly",Some(this))
            CDSReturnCode.SUCCESS
          case 1=>
            log.warn("Method executed with failure",Some(this))
            CDSReturnCode.FAILURE
          case 2=>
            log.warn("Method failed and signalled to stop block process",Some(this))
            CDSReturnCode.STOPROUTE
          case _=>
            log.error("Method returned a non-standard failure code",Some(this))
            CDSReturnCode.UNKNOWN
        }
    }

  }

  def dump = { /*print out info to stdout */
    println("Method:")
    println("\tName: " + name)
    println("\tType: " + methodType)
    println("\tRequired files: " + requiredFiles)
    println("\tParameter args: " + params)
  }
}
