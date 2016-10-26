package logging

import CDS.CDSMethod

import scala.collection.mutable.SynchronizedQueue
import java.util.concurrent.ConcurrentLinkedQueue

import config.LoggerConfig

/**
  * Created by localhome on 21/10/2016.
  */

object LogCollection {
  def fromConfig(loggerInfo:Set[LoggerConfig]) = {
    val optionInstances = loggerInfo.filter(x=>x.enabled).map(x=>x.makeInstance)

    val validInstances = optionInstances.filter(x=>x match {
                                                  case Some(logger)=>true
                                                  case None=>false
                                                }).map(x=>x.get)
    LogCollection(validInstances.toSeq)
  }
}


case class LogCollection(activeLoggers:Seq[Logger]) {
  val DEFAULT_SLEEP_DELAY = 1000 //milliseconds
  val logQueue = new ConcurrentLinkedQueue[Option[LogMessage]]

  class logProcessor(logQueue:ConcurrentLinkedQueue[Option[LogMessage]]) extends Runnable {
    override def run(): Unit = {
      var shouldExit:Boolean = false
      do {
        logQueue.poll() match {
          case null=>Thread.sleep(1000)
          case Some(msg)=>
            activeLoggers.foreach(x=>x.relayMessage(msg))
          case None=>
            shouldExit=true
        }
      } while(!shouldExit)
    }
  }

  def activeLoggerCount:Integer = activeLoggers.size

  //def relayMessage(msg:String,currentMethod:CDSMethod,severity:String) = activeLoggers.foreach(x=>x.relayMessage(msg,currentMethod,severity))
  def relayMessage(msg:String, sender:CDSMethod, severity: String) = logQueue.add(Some(LogMessage(msg,severity,sender)))
  def relayMessage(m:LogMessage) = logQueue.add(Some(m))

  def log(msg:String,curMethod:CDSMethod) = relayMessage(msg,curMethod,"log")
  def debug(msg:String,curMethod:CDSMethod) = relayMessage(msg,curMethod,"debug")
  def error(msg:String,curMethod:CDSMethod) = relayMessage(msg,curMethod,"error")
  def warn(msg:String,curMethod:CDSMethod) = relayMessage(msg,curMethod,"warn")

  def teardown = activeLoggers.foreach(x=>x.teardown)
}
