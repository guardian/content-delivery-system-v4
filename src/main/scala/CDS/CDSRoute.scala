package CDS

import logging.LogCollection

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
    (n.child.collect {
      case e: Elem if e.label != "take-files" =>
        e.label -> e.text
    }).toMap

  def getMethodAttrib(n:Node,attName:String):Option[String] =
    n \@ attName match {
      case "" => None
      case attr => Some(attr)
    }

  def getMethodName(n:Node):String = {
    getMethodAttrib(n,"name").getOrElse("(no name)")
  }

  def readRoute(x: Node):CDSRoute = {
    val methodList = for {
      child <- x.nonEmptyChildren
      if !child.isAtom
    } yield CDSMethod(
      child.label,getMethodName(child),getFileRequirements(child),getMethodParams(child)
    )

    CDSRoute(getMethodAttrib(x,"name").getOrElse("(no name)"),
      getMethodAttrib(x,"type").get,
      methodList)
  }

  def fromFile(filename:String) = {
    val src = Source.fromFile(filename,"utf8")
    val parser = ConstructingParser.fromSource(src,true)

    readRoute(parser.document().docElem)
  }
}

case class CDSRoute(name: String,routetype:String,methods:Seq[CDSMethod]) {
  def dump = {
    println("Got route name " + name + " (" + routetype + ")")
    methods.foreach(x=>{x.dump})
  }

  def runRoute(loggerCollection:LogCollection) = {
    methods.foreach(curMethod=>{curMethod.execute(loggerCollection)})
  }
}
