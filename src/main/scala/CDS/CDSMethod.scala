package CDS

import java.io.InputStream

import logging.LogCollection
import java.nio.file.{Files, Path, Paths}
import datastore.Datastore
import config.CDSConfig

case class CDSMethod(methodType: String,
                     name:String,
                     requiredFiles: Seq[String],
                     params: Map[String,String],
                     log:LogCollection,
                     store:Option[Datastore])
  extends ExternalCommand {

  val METHODS_BASE_PATH = "/usr/local/lib/cds_backend"

  def findFile:Option[Path] = {
    val extensions = List("",".rb",".pl",".py",".js")

    val filesList = extensions
      .map(xtn=>Paths.get(METHODS_BASE_PATH,name + xtn))
      .filter(path=>Files.exists(path))
    filesList.length match {
      case 0=>None
      case 1=>Some(filesList.head)
      case _=>
        log.warn("Multiple methods found for " + name + ":" + filesList,null)
        Some(filesList.head)
    }
  }

  override def errHandler(input: InputStream): Unit = {

  }
  override def outputHandler(input: InputStream): Unit = {

  }

  def execute:Boolean = {
    log.log("Executing method " + name + " as " + methodType,Some(this))

    findFile match {
      case None=>log.error("Could not find executable for "+name+" in "+ METHODS_BASE_PATH,None)
      case Some(path)=>
        runCommand(path.toString,Seq())
    }
    log.error("This method has not yet been implemented!", Some(this))
    false
  }

  def dump = { /*print out info to stdout */
    println("Method:")
    println("\tName: " + name)
    println("\tType: " + methodType)
    println("\tRequired files: " + requiredFiles)
    println("\tParameter args: " + params)
  }
}
