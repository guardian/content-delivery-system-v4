package CDS

import logging.LogCollection
import java.nio.file.{Files, Path, Paths}

case class CDSMethod(methodType: String, name:String, requiredFiles: Seq[String], params: Map[String,String]) {
  val METHODS_BASE_PATH = "/usr/local/lib/cds_backend"

  def findFile(log:LogCollection):Option[Path] = {
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

  def execute(log:LogCollection):Boolean = {
    log.log("Executing method " + name + " as " + methodType,this)

    findFile(log) match {
        //fixme: replace param with Option so can use None instead of null
      case None=>log.error("Could not find executable for "+name+" in "+ METHODS_BASE_PATH,null)
      case Some(path)=>
    }
    log.error("This method has not yet been implemented!", this)
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
