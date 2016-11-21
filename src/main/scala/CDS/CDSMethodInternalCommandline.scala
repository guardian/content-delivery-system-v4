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
                                        config: CDSConfig) extends CDSMethod {
  override def execute(fileCollection: FileCollection): CDSReturnCode.Value = {
    CDSReturnCode.UNKNOWN
  }
}