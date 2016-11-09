import logging.{LogCollection, LogMessage, Logger}
import CDS.CDSMethod
import config.CDSConfig
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import scala.concurrent.duration._
import scala.concurrent.Future

class LoggerCollectionSpec extends FlatSpec with Matchers with MockFactory{
  "A LoggerCollection " should "pass an INFO message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("test message","info",None))

    collection.log("test message",None)
  }

  it should "pass an WARNING message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("test message","warning",None))

    collection.warn("test message",None)
  }

  it should "pass an ERROR message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("test message","error",None))

    collection.error("test message",None)
  }

  it should "pass a DEBUG message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("test message","debug",None))

    collection.debug("test message",None)
  }

  it should "pass an INFO message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("something happened","info",None))

    val msg = LogMessage.fromString("INFO: something happened",None)
    collection.relayMessage(msg)
  }

  it should "pass a WARNING message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("something bad happened","warning",None))

    val msg = LogMessage.fromString("-WARNING: something bad happened",None)
    collection.relayMessage(msg)
  }

  it should "pass an ERROR message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("something bad happened","error",None))

    val msg = LogMessage.fromString("-ERROR: something bad happened",None)
    collection.relayMessage(msg)
  }

  it should "pass an DEBUG message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("nothing happened","debug",None))

    val msg = LogMessage.fromString("DEBUG: nothing happened",None)
    collection.relayMessage(msg)
  }

  it should "pass a SUCCESS message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("something worked","ok",None))

    val msg = LogMessage.fromString("+SUCCESS: something worked",None)
    collection.relayMessage(msg)
  }

  it should "pass a Method Starting message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))
    val mthd = CDSMethod("test-method","stringtest",Seq(),Map(),collection,None,CDSConfig.placeholder(Map()))

    (lg.methodStarting _).expects(mthd)

    collection.methodStarting(mthd)
  }

  it should "pass a Method Finished message to the log backend, preserving flags" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))
    val mthd = CDSMethod("test-method","stringtest",Seq(),Map(),collection,None,CDSConfig.placeholder(Map()))

    inAnyOrder {  //since these are called via Futures, they can happen in any order
      (lg.methodFinished _).expects(mthd, true, false)
      (lg.methodFinished _).expects(mthd, true, true)
      (lg.methodFinished _).expects(mthd, false, true)
      (lg.methodFinished _).expects(mthd, false, false)
    }

    collection.methodFinished(mthd,success=true,nonfatal=false)
    collection.methodFinished(mthd,success=true,nonfatal=true)
    collection.methodFinished(mthd,success=false,nonfatal=false)
    collection.methodFinished(mthd,success=false,nonfatal=true)
  }

  it should "allow waiting for log backend processes to complete" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))
    val mthd = CDSMethod("test-method","stringtest",Seq(),Map(),collection,None,CDSConfig.placeholder(Map()))

    inSequence {
      (lg.methodStarting _).expects(mthd)
      (lg.methodFinished _).expects(mthd,true,false)
    }

    collection.waitFor(collection.methodStarting(mthd),timeout=1.seconds)
    collection.methodFinished(mthd,success=true,nonfatal=false)
  }
}
