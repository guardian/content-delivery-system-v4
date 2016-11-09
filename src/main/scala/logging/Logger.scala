package logging
import CDS.CDSMethod

import scala.concurrent.Future

trait Logger {
  def init(params:Map[String,String]):Logger = this

  def relayMessage(msg:String,curMethod:Option[CDSMethod],severity:String):Future[Unit]
  def relayMessage(m: LogMessage): Future[Unit] = relayMessage(m.msg,m.sender,m.severity)

  def methodStarting(newMethod:CDSMethod):Future[Unit]

  def methodFinished(method:CDSMethod,success:Boolean,nonfatal:Boolean):Future[Unit]

  def log(msg:String,curMethod:Option[CDSMethod]) = relayMessage(msg,curMethod,"log")
  def debug(msg:String,curMethod:Option[CDSMethod]) = relayMessage(msg,curMethod,"debug")
  def error(msg:String,curMethod:Option[CDSMethod]) = relayMessage(msg,curMethod,"error")
  def warn(msg:String,curMethod:Option[CDSMethod]) = relayMessage(msg,curMethod,"warn")

  def datastoreUpdated(by:CDSMethod,values:Map[String,String]):Future[Unit]

  def teardown:Boolean
}
