package logging
import CDS.CDSMethod

case class LogMessage(msg:String, severity: String, sender:CDSMethod) {
  override def toString: String = {
    sender.name + ": " + severity.toLowerCase() match {
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
  }
}