package CDS
import scala.sys.process._

/**
  * Created by localhome on 26/10/2016.
  */
trait ExternalCommand {
  def outputHandler(input: java.io.InputStream) = {}
  def errHandler(input: java.io.InputStream) = {}

  def runCommand(scriptpath:String,args:Seq[String]):Process = {
    val cmd:Seq[String]= scriptpath +: args

    val process = Process(scriptpath)
    process run new ProcessIO(_.close(),outputHandler,errHandler)
  }

}
