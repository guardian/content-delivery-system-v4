package logging
import CDS.CDSMethod

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
    }
    s"$senderName: $lineend"
  }
}