package logging
import CDS.CDSMethod

object LogMessage {
  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }

  def fromString(msg:String, sender:Option[CDSMethod]):LogMessage = {
    val (severity:String,msgtext:String) = msg match {
      case r"\s*INFO: (.*)$m"=>("info",m)
      case r"\s*DEBUG: (.*)$m"=>("debug",m)
      case r"\s*[-]*WARN: (.*)$m"=>("warning",m)
      case r"\s*[-]*WARNING: (.*)$m"=>("warning",m)
      case r"\s*[-]*ERROR: (.*)$m"=>("error",m)
      case r"\s*[-]*FATAL: (.*)$m"=>("fatal",m)
      case r"\s*[+]*SUCCESS: (.*)$m"=>("ok",m)
      case r"\s*[+]*OK: (.*)$m"=>("ok",m)
      case r"(.*)$m"=>("info",m)
    }
    new LogMessage(msgtext,severity,sender)
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
      case "warning"=>
        "-WARNING: " + msg
      case "success"=>
        "+OK: " + msg
      case "ok"=>
        "+OK: " + msg
      case "info"=>
        "INFO:" + msg
    }
    //s"$senderName: $lineend"
    msg
  }
}