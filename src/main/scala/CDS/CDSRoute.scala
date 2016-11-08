package CDS

import logging.LogCollection
import config.CDSConfig

import scala.io.Source
import scala.xml.{Elem, Node}
import scala.xml.parsing.ConstructingParser

/**
  * Created by localhome on 21/10/2016.
  */

object CDSRoute {
  def getFileRequirements(n:Node):Seq[String] = {
    val takeFilesContent = (n \ "take-files").text
    takeFilesContent.split("\\|")
  }

  def getMethodParams(n:Node):Map[String,String] =
  /*
  collect allows us to run a different mapping function depending on the data type incoming
  here we iterate all child nodes, but only take the ones that are Elements (Elem) (thus filtering out #PCDATA)
  and map it to a list of tuples label->text.  we then convert this to a String-String map.
   */
    n.child.collect {
      case e: Elem if e.label != "take-files" =>
        e.label -> e.text
    }.toMap

  def getMethodAttrib(n:Node,attName:String):Option[String] =
    n \@ attName match {
      case "" => None
      case attr => Some(attr)
    }

  def getMethodName(n:Node):String = {
    getMethodAttrib(n,"name").getOrElse("(no name)")
  }

  def readRoute(x: Node,config:CDSConfig):CDSRoute = {
    /*
    this call is effectively a filter chain
    the first line in the block after for is what to iterate on
    each following line is a filter if it starts with "if" or a map otherwise
    then the yield is evaluated for the output of the filter chain.
     */
    val methodList = for {
      child <- x.nonEmptyChildren
      if !child.isAtom
    } yield CDSMethod(
      child.label,getMethodName(child),
      getFileRequirements(child),
      getMethodParams(child),
      /* using .get() here should be OK provided that routes are validated against the XSD before this method is called*/
      config.getLogCollection(getMethodAttrib(x,"name").getOrElse("(no name)"), getMethodAttrib(x,"type").get),
      config.datastore,
      config
    )

    CDSRoute(getMethodAttrib(x,"name").getOrElse("(no name)"),
      getMethodAttrib(x,"type").get,
      methodList,
      config)
  }

  def fromFile(filename:String,config:CDSConfig) = {
    val src = Source.fromFile(filename,"utf8")
    val parser = ConstructingParser.fromSource(src,true)

    readRoute(parser.document().docElem,config)
  }
}

case class CDSRoute(name: String,routetype:String,methods:Seq[CDSMethod],config:CDSConfig) {
  def dump = {
    println("Got route name " + name + " (" + routetype + ")")
    methods.foreach(x=>{x.dump})
  }

  def runRoute(loggerCollection:LogCollection) = {
    methods.foreach(curMethod=>{curMethod.execute})
  }
}
