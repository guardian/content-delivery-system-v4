import logging.{LogCollection, LogMessage, Logger}
import CDS.CDSMethod
import config.CDSConfig
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class LoggerCollectionSpec extends FlatSpec with Matchers with MockFactory{
  "A LoggerCollection " should "pass an INFO message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("test message","info",None))

    collection.log("test message",None)
  }

  it should "pass an WARNING message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("test message","warning",None))

    collection.warn("test message",None)
  }

  it should "pass an ERROR message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("test message","error",None))

    collection.error("test message",None)
  }

  it should "pass a DEBUG message to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("test message","debug",None))

    collection.debug("test message",None)
  }

  it should "pass an INFO message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("something happened","info",None))

    val msg = LogMessage.fromString("INFO: something happened",None)
    collection.relayMessage(msg)
  }

  it should "pass a WARNING message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("something bad happened","warning",None))

    val msg = LogMessage.fromString("-WARNING: something bad happened",None)
    collection.relayMessage(msg)
  }

  it should "pass an ERROR message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("something bad happened","error",None))

    val msg = LogMessage.fromString("-ERROR: something bad happened",None)
    collection.relayMessage(msg)
  }

  it should "pass an DEBUG message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("nothing happened","debug",None))

    val msg = LogMessage.fromString("DEBUG: nothing happened",None)
    collection.relayMessage(msg)
  }

  it should "pass a SUCCESS message from string to the log backend" in {
    val lg = mock[Logger]
    val collection = LogCollection(List(lg))

    (lg.relayMessage _:LogMessage=>Unit).expects(LogMessage("something worked","ok",None))

    val msg = LogMessage.fromString("+SUCCESS: something worked",None)
    collection.relayMessage(msg)
  }


}
