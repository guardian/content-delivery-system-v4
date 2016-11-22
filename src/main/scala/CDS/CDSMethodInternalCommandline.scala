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
      (CDSReturnCode.SUCCESS, List(FileCollection.fromOptionMap(options, store.get.uri)))
    } catch {
      case e:Throwable=>
        (CDSReturnCode.FAILURE,List())
    }
  }
}