package logging
import CDS.CDSMethod

trait Logger {
  def init(params:Map[String,String]):Logger

  def relayMessage(msg:String,curMethod:CDSMethod,severity:String)
  def relayMessage(m: LogMessage): Unit = relayMessage(m.msg,m.sender,m.severity)

  def log(msg:String,curMethod:CDSMethod) = relayMessage(msg,curMethod,"log")
  def debug(msg:String,curMethod:CDSMethod) = relayMessage(msg,curMethod,"debug")
  def error(msg:String,curMethod:CDSMethod) = relayMessage(msg,curMethod,"error")
  def warn(msg:String,curMethod:CDSMethod) = relayMessage(msg,curMethod,"warn")

  def datastoreUpdated(by:CDSMethod,values:Map[String,String])

  def teardown:Boolean
}
