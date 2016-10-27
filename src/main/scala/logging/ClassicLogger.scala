package logging
import CDS.{CDSMethod, CDSRoute}
import java.io._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ClassicLogger(params: Map[String, String],routeName:String,routeType:String) extends Logger {
  val safeRouteName = routeName.replaceAll("[^A-Za-z0-9]+","_")
  val format = new java.text.SimpleDateFormat("_yyyy-MM-dd_hhmmss")
  val logdir = params("basepath") + s"/$safeRouteName"
  val filename = s"/cds_$safeRouteName" + format.format(new java.util.Date())
  new File(logdir).mkdirs()
  val printwriter = new PrintWriter(new File(logdir + filename))

  override def init(params: Map[String, String]): Logger = {
    this
  }

  override def methodStarting(newMethod: CDSMethod): Unit = Future {
    printwriter.write("---------------------------------------------------------")
    printwriter.write("CDS: executing " + newMethod.methodType + " " + newMethod.name)
  }

  override def methodFinished(method: CDSMethod, success: Boolean, nonfatal: Boolean): Unit = Future {
    success match {
      case true=>printwriter.write("CDS: " + method.methodType + " " + method.name + " returned successfully")
      case false=>printwriter.write("CDS: " + method.methodType + " " + method.name + " FAILED")
    }
    if(! nonfatal) printwriter.write("<nonfatal/> is set, so continuing.")
  }

  override def relayMessage(msg: String, curMethod: CDSMethod, severity: String): Unit =
    Future { printwriter.write("\t" + curMethod.name + s": $severity: $msg") }

  override def datastoreUpdated(by: CDSMethod, values: Map[String, String]): Unit = {}

  override def teardown: Boolean = true
}
