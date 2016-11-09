package logging
import CDS.CDSMethod

object LogMessage {
  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  def fromString(msg:String, sender:Option[CDSMethod]):LogMessage = {
    val severity = msg match {
      case r"\s*INFO"=>"info"
      case r"\s*DEBUG.*"=>"debug"
      case r"\s*[-]*WARN.*"=>"warning" //also gets "WARNING"
      case r"\s*[-]*ERROR.*"=>"error"
      case r"\s*[-]*FATAL.*"=>"fatal"
      case r"\s*[+]*SUCCESS.*"=>"ok"
      case r"\s*[+]*OK.*"=>"ok"
      case _=>"info"
    }
    new LogMessage(msg,severity,sender)
  }
}

case class LogMessage(msg:String, severity: String, sender:Option[CDSMethod]) {
  override def toString: String = {
    val senderName = sender match {
      case Some(method)=>method.name
      case None=>"CDS"
    }

    val lineend = severity.toLowerCase() match {
      case "error"=>
        "-ERROR: " + msg
      case "warn"=>
        "-WARNING: " + msg
      case "warning"=>
        "-WARNING: " + msg
      case "success"=>
        "+OK: " + msg
      case "ok"=>
        "+OK: " + msg
      case "info"=>
        "INFO:" + msg
    }
    s"$senderName: $lineend"
  }
}