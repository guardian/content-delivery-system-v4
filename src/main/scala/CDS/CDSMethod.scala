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

trait CDSMethod {
  val methodType:String
  val name:String
  val requiredFiles: Seq[String]
  val params: Map[String,String]
  val log:LogCollection
  val store: Option[Datastore]
  val config:CDSConfig

  val METHODS_BASE_PATH = config.paths.get("methods") match {
    case Some(path)=>path
    case None=>"/usr/local/lib/cds_backend"
  }

  def findFile:Option[String] = {
    val extensions = List("",".rb",".pl",".py",".js",".sh")

    val filesList = extensions
      .map(xtn=>Paths.get(METHODS_BASE_PATH,name + xtn))
      .filter(path=>Files.exists(path))
    filesList.length match {
      case 0=>None
      case 1=>Some(filesList.head.toString)
      case _=>
        log.warn("Multiple methods found for " + name + ":" + filesList,null)
        Some(filesList.head.toString)
    }
  }

  def execute(fileCollection: FileCollection):(CDSReturnCode.Value,List[FileCollection])

  def dump = { /*print out info to stdout */
    println("Method:")
    println("\tName: " + name)
    println("\tType: " + methodType)
    println("\tRequired files: " + requiredFiles)
    println("\tParameter args: " + params)
  }
}

object CDSMethodFactory {
  def newCDSMethod(methodType: String,
    name:String,
    requiredFiles: Seq[String],
    params: Map[String,String],
    log:LogCollection,
    store:Option[Datastore],
    config: CDSConfig,
                   options:Map[Symbol,String]) = {

    /*simple check from internally implemented pseudo-methods.*/
    name match {
      case "commandline"=>CDSMethodInternalCommandline(methodType,name,requiredFiles,params,log,store,config,options)
      case _=>CDSMethodScript(methodType,name,requiredFiles,params,log,store,config)
    }
  }
}