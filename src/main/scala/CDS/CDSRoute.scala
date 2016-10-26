package CDS

import logging.LogCollection
import config.CDSConfig

import scala.io.Source
import scala.xml.Node
import scala.xml.parsing.ConstructingParser

/**
  * Created by localhome on 21/10/2016.
  */

object CDSRoute {
  def getFileRequirements(n:Node):Seq[String] = {
    val takeFilesContent = (n \ "take-files").text
    takeFilesContent.split("\\|")
  }

  def getMethodParams(n:Node):Map[String,String] = {
    n.child.filter(
      x=>{
        x.label match {
          case "#PCDATA"=>false
          case "take-files"=>false
          case _=>true
        }
      }
    ).map (x=>{x.label->x.text}).toMap
  }

  def getMethodAttrib(n:Node,attName:String):String = {
    n.attribute(attName) match {
      case Some(nodeseq) => nodeseq.head.text
      case _ => "(noname)"
    }
  }

  def getMethodName(n:Node):String = {
    getMethodAttrib(n,"name")
  }

  def readRoute(x: Node,config:CDSConfig):CDSRoute = {
    val logCollection = config.getLogCollection
    val methodList:Seq[CDSMethod] = x.nonEmptyChildren.
      filter(z=>{!z.isAtom}).
      map(y =>{
        CDSMethod(y.label,getMethodName(y),getFileRequirements(y),getMethodParams(y),logCollection)
      })
    CDSRoute(getMethodName(x),getMethodAttrib(x,"type"),methodList,config)
  }

  def fromFile(filename:String,config:CDSConfig) = {
    val src = Source.fromFile(filename,"utf8")
    val parser = new ConstructingParser(src,true)
    parser.initialize
    val doc = parser.document()

    readRoute(doc.children.head,config)
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
