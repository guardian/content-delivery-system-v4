package CDS
import scala.sys.process._

/**
  * Created by localhome on 26/10/2016.
  */
trait ExternalCommand {
  def outputHandler(input: java.io.InputStream) = {}
  def errHandler(input: java.io.InputStream) = {}

  def runCommand(scriptpath:String,args:Seq[String],params:Map[String,String]):Process = {
    val cmd:Seq[String]= scriptpath +: args

    //middle parameter is PWD. Last parameter is a varargs style set of (String,String) tuples.
    val process = Process(scriptpath,None,params.toArray: _*)
    process run new ProcessIO(_.close(),outputHandler,errHandler)
  }

}
