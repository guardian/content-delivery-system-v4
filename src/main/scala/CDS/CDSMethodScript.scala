package CDS
import java.io.InputStream

import config.CDSConfig
import datastore.Datastore
import logging.{LogCollection, LogMessage}

import scala.io.Source

case class CDSMethodScript(methodType: String,
                           name:String,
                           requiredFiles: Seq[String],
                           params: Map[String,String],
                           log:LogCollection,
                           store:Option[Datastore],
                           config: CDSConfig)
  extends CDSMethod with ExternalCommand {

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

  def execute(fileCollection: FileCollection):(CDSReturnCode.Value,List[FileCollection]) = {
    log.log("Executing method " + name + " as " + methodType,Some(this))

    findFile match {
      case None=>
        log.error("Could not find executable for "+name+" in "+ METHODS_BASE_PATH,None)
        (CDSReturnCode.NOTFOUND,List(fileCollection))
      case Some(path)=>
        if(! fileCollection.hasFiles(requiredFiles)){
          log.error(s"Unable to execute method, some required files ($requiredFiles) are not available",Some(this))
        }
        val p = runCommand(path.toString,Seq(),params ++ fileCollection.getEnvironmentMap(requiredFiles))
        val fcList = FileCollection.fromTempFile(fileCollection.tempFile, Some(fileCollection), None)
        p.exitValue() match {
          case 0=>
            log.log("Method exited cleanly",Some(this))
            (CDSReturnCode.SUCCESS,fcList)
          case 1=>
            log.warn("Method executed with failure",Some(this))
            (CDSReturnCode.FAILURE,fcList)
          case 2=>
            log.warn("Method failed and signalled to stop block process",Some(this))
            (CDSReturnCode.STOPROUTE,fcList)
          case _=>
            log.error("Method returned a non-standard failure code",Some(this))
            (CDSReturnCode.UNKNOWN,fcList)
        }
    }

  }

}
