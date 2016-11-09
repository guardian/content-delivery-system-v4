import logging.{LogCollection, LogMessage, Logger}
import CDS.CDSMethod
import config.CDSConfig
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class LoggerCollectionSpec extends FlatSpec with Matchers with MockFactory{
  "A logger " should "do stuff" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))
    //collection.init
    //val cfg = CDSConfig.placeholder(Map())
    //val mthd = CDSMethod("test-method","test",Seq(),Map(),collection,None,cfg)

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("test message","info",None))

    collection.log("INFO: test message",None)
  }
}
