package logging

import CDS.{CDSMethod, CDSRoute}

import scala.collection.mutable.SynchronizedQueue
import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.ExecutionContext.Implicits.global
import config.LoggerConfig

import scala.concurrent.Future

/**
  * Created by localhome on 21/10/2016.
  */

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
  val DEFAULT_SLEEP_DELAY = 1000 //milliseconds
//  val logQueue = new ConcurrentLinkedQueue[Option[LogMessage]]
//
//  class logProcessor(logQueue:ConcurrentLinkedQueue[Option[LogMessage]]) extends Runnable {
//    override def run(): Unit = {
//      var shouldExit:Boolean = false
//      do {
//        logQueue.poll() match {
//          case null=>Thread.sleep(1000) //no messages matched
//          case Some(msg)=>
//            activeLoggers.foreach(x=>x.relayMessage(msg)) //we got a message
//          case None=>
//            shouldExit=true  //None was sent=>should terminate
//        }
//      } while(!shouldExit)
//    }
//  }

//  def runthread = {
//    val p = new logProcessor(logQueue)
//    p.run()
//  }

  def activeLoggerCount:Integer = activeLoggers.size

  //def relayMessage(msg:String,currentMethod:CDSMethod,severity:String) = activeLoggers.foreach(x=>x.relayMessage(msg,currentMethod,severity))
  //def relayMessage(msg:String, sender:Option[CDSMethod], severity: String) = logQueue.add(Some(LogMessage(msg,severity,sender)))
  //def relayMessage(m:LogMessage) = logQueue.add(Some(m))

  //def relayMessage(msg:LogMessage) = Future { activeLoggers.foreach(x=>x.relayMessage(msg))}
  def relayMessage(msg:LogMessage) = activeLoggers.foreach(x=>x.relayMessage(msg))

  def log(msg:String,curMethod:Option[CDSMethod]) = relayMessage(LogMessage(msg,"info",curMethod))
  def debug(msg:String,curMethod:Option[CDSMethod]) = relayMessage(LogMessage(msg,"debug",curMethod))
  def error(msg:String,curMethod:Option[CDSMethod]) = relayMessage(LogMessage(msg,"error",curMethod))
  def warn(msg:String,curMethod:Option[CDSMethod]) = relayMessage(LogMessage(msg,"warning",curMethod))

  def methodStarting(newMethod: CDSMethod): Unit =
    activeLoggers.foreach(x=>x.methodStarting(newMethod))
  def methodFinished(method: CDSMethod, success: Boolean, nonfatal: Boolean): Unit =
    activeLoggers.foreach(x=>x.methodFinished(method,success,nonfatal))

  def teardown:Unit = {
    //logQueue.add(None)

    activeLoggers.foreach(x=>x.teardown)
  }
}
