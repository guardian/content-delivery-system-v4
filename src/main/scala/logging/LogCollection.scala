package logging

import CDS.{CDSMethod, CDSRoute}
import scala.concurrent.ExecutionContext.Implicits.global
import config.LoggerConfig
import scala.concurrent.duration.Duration

import scala.concurrent.{Await, Future}

object LogCollection {
  def fromConfig(loggerInfo:Set[LoggerConfig],routeName:String,routeType:String) = {
    val optionInstances = loggerInfo.filter(x=>x.enabled).map(x=>x.makeInstance(routeName,routeType))

    val validInstances = optionInstances.filter(x=>x match {
                                                  case Some(logger)=>true
                                                  case None=>false
                                                }).map(x=>x.get)
    LogCollection(validInstances.toSeq)
  }
}


case class LogCollection(activeLoggers:Seq[Logger]) {

  def activeLoggerCount:Integer = activeLoggers.size

  def relayMessage(msg:LogMessage):Seq[Future[Unit]] = activeLoggers.map(x=>x.relayMessage(msg))

  def log(msg:String,curMethod:Option[CDSMethod]) = relayMessage(LogMessage(msg,"info",curMethod))
  def debug(msg:String,curMethod:Option[CDSMethod]) = relayMessage(LogMessage(msg,"debug",curMethod))
  def error(msg:String,curMethod:Option[CDSMethod]) = relayMessage(LogMessage(msg,"error",curMethod))
  def warn(msg:String,curMethod:Option[CDSMethod]) = relayMessage(LogMessage(msg,"warning",curMethod))

  def methodStarting(newMethod: CDSMethod): Seq[Future[Unit]] =
    activeLoggers.map(x=>x.methodStarting(newMethod))
  def methodFinished(method: CDSMethod, success: Boolean, nonfatal: Boolean): Seq[Future[Unit]] =
    activeLoggers.map(x=>x.methodFinished(method,success,nonfatal))

  /* You _can_ call this to wait for the logging backends to finish their output, not recommended but hey */
  def waitFor(futures:Seq[Future[Unit]],timeout:Duration) = Await.ready(Future.sequence(futures),timeout)

  def teardown:Unit = activeLoggers.foreach(x=>x.teardown)

}
