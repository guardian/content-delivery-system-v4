package logging
import CDS.{CDSMethod, CDSReturnCode}
import org.apache.logging.log4j.LogManager

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Simple logger implementation with log4j
  */
class Log4jLogger(params:Map[String,String],routeName:String,routeType:String) extends Logger {
  val l4j = LogManager.getLogger("route")

  override def init(params:Map[String,String]):Logger = this

  override def relayMessage(msg: String, curMethod: Option[CDSMethod], severity: String) = Future {
      val methodName = curMethod match {
        case Some(method)=>method.name
        case None=>"CDS"
      }
      val logstring = s"$methodName: $msg"
      severity match {
        case "log" => l4j.info(logstring)
        case "error" => l4j.error(logstring)
        case "warn" => l4j.warn(logstring)
        case "debug" => l4j.debug(logstring)
        case _ => l4j.debug(logstring)
      }
    }


  override def teardown: Boolean = true

  override def datastoreUpdated(by: CDSMethod, values: Map[String, String]) = Future {}

  override def methodStarting(newMethod: CDSMethod)= Future {}

  override def methodFinished(method: CDSMethod, success: CDSReturnCode.Value, nonfatal: Boolean) = Future {}
}
