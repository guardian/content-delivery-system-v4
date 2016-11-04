package logging
import CDS.CDSMethod

trait Logger {
  def init(params:Map[String,String]):Logger = this

  def relayMessage(msg:String,curMethod:Option[CDSMethod],severity:String)
  def relayMessage(m: LogMessage): Unit = relayMessage(m.msg,m.sender,m.severity)

  def methodStarting(newMethod:CDSMethod)

  def methodFinished(method:CDSMethod,success:Boolean,nonfatal:Boolean)

  def log(msg:String,curMethod:Option[CDSMethod]) = relayMessage(msg,curMethod,"log")
  def debug(msg:String,curMethod:Option[CDSMethod]) = relayMessage(msg,curMethod,"debug")
  def error(msg:String,curMethod:Option[CDSMethod]) = relayMessage(msg,curMethod,"error")
  def warn(msg:String,curMethod:Option[CDSMethod]) = relayMessage(msg,curMethod,"warn")

  def datastoreUpdated(by:CDSMethod,values:Map[String,String])

  def teardown:Boolean
}
