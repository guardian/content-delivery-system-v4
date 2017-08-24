package CDS

import config.CDSConfig
import datastore.Datastore
import logging.LogCollection

case class CDSMethodInternalCommandline(methodType: String,
                                        name:String,
                                        requiredFiles: Seq[String],
                                        params: Map[String,String],
                                        log:LogCollection,
                                        store:Option[Datastore],
                                        config: CDSConfig,
                                        options: Map[Symbol,String]) extends CDSMethod {
  override def execute(fileCollection: FileCollection): (CDSReturnCode.Value,List[FileCollection]) = {
    try {
      val newfc = FileCollection.fromOptionMap(options, store.get.uri)
      log.log(s"Setting up the following files",Some(this))
      log.log(s"\tMedia: ${newfc.mediaFile}",Some(this))
      log.log(s"\tInMeta: ${newfc.inmetaFile}",Some(this))
      log.log(s"\tMeta: ${newfc.metaFile}",Some(this))
      log.log(s"\tXML: ${newfc.xmlFile}",Some(this))
      (CDSReturnCode.SUCCESS, List(newfc))
    } catch {
      case e:Throwable=>  //log errors out to the log(s), return a failure
        log.error(e.getMessage,Some(this))
        log.error(e.getStackTrace.toString,Some(this))
        (CDSReturnCode.FAILURE,List())
    }
  }
}