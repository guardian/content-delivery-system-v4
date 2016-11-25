import logging.LogMessage
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class LogMessageSpec extends FlatSpec with Matchers {
  /*
  def fromString(msg:String, sender:Option[CDSMethod]):LogMessage = {
val severity = msg match {
  case r"^\s*[+-]*INFO"=>"info"
  case r"^\s*[+-]*DEBUG"=>"debug"
  case r"^\s*[+-]*WARN"=>"warning" //also gets "WARNING"
  case r"^\s*[+-]*ERROR"=>"error"
  case r"^\s*[+-]*FATAL"=>"fatal"
  case r"^\s*[+-]*SUCCESS"=>"ok"
  case r"^\s*[+-]*OK"=>"ok"
  case _=>"info"
}
new LogMessage(msg,severity,sender)
 */
  "A logging.LogMessage" should "correctly match INFO line prefix" in {
    val m=LogMessage.fromString("INFO: blahblahblah",None)
    m.severity should be ("info")
    m.msg should be ("blahblahblah")
  }

  it should "correctly match DEBUG line prefix" in {
    val m=LogMessage.fromString("DEBUG: blahblahblah",None)
    m.severity should be ("debug")
    m.msg should be ("blahblahblah")
  }

  it should "correctly match WARN line prefix" in {
    val m=LogMessage.fromString("-WARN: blahblahblah",None)
    m.severity should be ("warning")
    m.msg should be ("blahblahblah")
  }

  it should "correctly match ERROR line prefix" in {
    val m=LogMessage.fromString("-ERROR: blahblahblah",None)
    m.severity should be ("error")
    m.msg should be ("blahblahblah")
  }

  it should "correctly match FATAL line prefix" in {
    val m=LogMessage.fromString("-FATAL: blahblahblah",None)
    m.severity should be ("fatal")
    m.msg should be ("blahblahblah")
  }

  it should "correctly match SUCCESS line prefix" in {
    val m=LogMessage.fromString("+SUCCESS: blahblahblah",None)
    m.severity should be ("ok")
    m.msg should be ("blahblahblah")
  }

  it should "correctly match OK line prefix" in {
    val m=LogMessage.fromString("OK: blahblahblah",None)
    m.severity should be ("ok")
    m.msg should be ("blahblahblah")
  }

  it should "correctly match absence of line prefix" in {
    val m=LogMessage.fromString("blahblahblah",None)
    m.severity should be ("info")
    m.msg should be ("blahblahblah")
  }
}
