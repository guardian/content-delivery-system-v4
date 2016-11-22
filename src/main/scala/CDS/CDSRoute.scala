package CDS

import java.util.concurrent.TimeoutException

import logging.LogCollection
import config.CDSConfig

import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.xml.{Elem, Node}
import scala.xml.parsing.ConstructingParser
import scala.concurrent.duration._

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

  def readRoute(x: Node,config:CDSConfig,options:Map[Symbol,String]):CDSRoute = {
    /*
    this call is effectively a filter chain
    the first line in the block after for is what to iterate on
    each following line is a filter if it starts with "if" or a map otherwise
    then the yield is evaluated for the output of the filter chain.
     */
//    val methodList = for {
//      child <- x.nonEmptyChildren
//      if !child.isAtom
//    } yield CDSMethodScript(
//      child.label,getMethodName(child),
//      getFileRequirements(child),
//      getMethodParams(child),
//      /* using .get() here should be OK provided that routes are validated against the XSD before this method is called*/
//      config.getLogCollection(getMethodAttrib(x,"name").getOrElse("(no name)"), getMethodAttrib(x,"type").get),
//      config.datastore,
//      config
//    )

    val methodList = for {
      child <- x.nonEmptyChildren
      if !child.isAtom
    } yield CDSMethodFactory.newCDSMethod(
      child.label, //method type
      getMethodName(child), //method name
      getFileRequirements(child), //file requirements
      getMethodParams(child), //method parameters from route body
      /* using .get() here should be OK provided that routes are validated against the XSD before this method is called*/
      config.getLogCollection(getMethodAttrib(x,"name").getOrElse("(no name)"), getMethodAttrib(x,"type").get), //logger
      config.datastore, //datastore
      config, //configuration object
      options
    )

    CDSRoute(getMethodAttrib(x,"name").getOrElse("(no name)"),
      getMethodAttrib(x,"type").get,
      methodList.toList,
      config)
  }

  def fromFile(filename:String,config:CDSConfig,options:Map[Symbol,String]) = {
    val src = Source.fromFile(filename,"utf8")
    val parser = ConstructingParser.fromSource(src,true)

    readRoute(parser.document().docElem,config,options)
  }
}

case class BatchModeException(methodRef:CDSMethod,fileCollection: FileCollection,code: CDSReturnCode.Value) extends RuntimeException

case class CDSRoute(name: String,routetype:String,methods:List[CDSMethod],config:CDSConfig) {
  def dump = {
    println("Got route name " + name + " (" + routetype + ")")
    methods.foreach(x=>{x.dump})
  }

  def runRoute(optionMap:Map[Symbol,String]):Unit = {
    val datastore = config.datastore.get
    val loggerCollection = config.getLogCollection(name,routetype)

    try {
      Await.ready(datastore.createNewDatastore(Map()), 1.seconds)
    } catch {
      case e:TimeoutException=>
        loggerCollection.error("Unable to set up datastore - create operation timed out",None)
        return
    }

    def runNextMethod(methodRef:CDSMethod,reminaingMethods:List[CDSMethod],previousFileCollection:FileCollection,shouldFail:Boolean):(CDSReturnCode.Value,Option[FileCollection]) = {
      val fc=previousFileCollection
      val nonfatal = methodRef.params.contains("nonfatal")

      loggerCollection.methodStarting(methodRef)

      val rtuple = methodRef.execute(fc)
      val r=rtuple._1
      val fcList = rtuple._2
      loggerCollection.methodFinished(methodRef,r,nonfatal)
      r match {
        case CDSReturnCode.NOTFOUND=>
          loggerCollection.error(s"Method ${methodRef.name} was not found",None)
          if(shouldFail && !nonfatal) return (CDSReturnCode.NOTFOUND,Some(fc))
        case CDSReturnCode.FAILURE=>
          loggerCollection.error(s"Method ${methodRef.name} failed",None)
          if(shouldFail && !nonfatal) return (CDSReturnCode.FAILURE,Some(fc))
        case CDSReturnCode.STOPROUTE=>
          loggerCollection.error(s"Method ${methodRef.name} failed and stopped the route",None)
          if(shouldFail && !nonfatal) return (CDSReturnCode.STOPROUTE,Some(fc))
        case CDSReturnCode.UNKNOWN=>
          loggerCollection.error(s"Method ${methodRef.name} returned an unknown status. Treating it as a failure.",None)
          if(shouldFail && !nonfatal) return (CDSReturnCode.UNKNOWN,Some(fc))
        case _=>

      }

      if(reminaingMethods.nonEmpty) {
        /*f(fcList.length==1)
          runNextMethod(reminaingMethods.head, reminaingMethods.tail, fcList.head, shouldFail)
        else
          loggerCollection.error("Batch mode is not supported yet I'm afraid",None)
          return (CDSReturnCode.STOPROUTE,Some(fc))*/
        try {
          fcList.foreach(fc => {
            val (r,nextfc) = runNextMethod(reminaingMethods.head, reminaingMethods.tail, fc, shouldFail)
            if(r==CDSReturnCode.STOPROUTE)
                /* this is the only way i can think of to break out of this loop if the method signifies stoproute */
                throw BatchModeException(reminaingMethods.head, fc, CDSReturnCode.STOPROUTE)
          })
        } catch {
          case e:BatchModeException=>
            return (e.code,Some(e.fileCollection))
        }
      }
      (r,Some(fcList.head))
    }

    val successMethods = methods.filter(_.methodType match {
      case "success-method"=>true
      case _=>false
    })
    val failMethods = methods.filter(_.methodType match {
      case "fail-method"=>true
      case _=>false
    })
    val otherMethods = methods.filter(_.methodType match {
      case "success-method"=>false
      case "fail-method"=>false
      case _=>true
    })

    runNextMethod(otherMethods.head,otherMethods.tail,FileCollection.fromOptionMap(optionMap,datastore.uri),shouldFail = true) match {
      case (CDSReturnCode.FAILURE,Some(fc)) =>
        loggerCollection.error("Route failed. Executing failure methods",None)
        if(failMethods.nonEmpty)
          runNextMethod(failMethods.head,failMethods.tail,fc,shouldFail = false)
      case (CDSReturnCode.UNKNOWN,Some(fc))=>
        loggerCollection.error("Route failed. Executing failure methods",None)
        if(failMethods.nonEmpty)
          runNextMethod(failMethods.head,failMethods.tail,fc,shouldFail = false)
      case (CDSReturnCode.STOPROUTE,Some(fc))=>
        loggerCollection.error("Route failed. Executing failure methods",None)
        if(failMethods.nonEmpty)
          runNextMethod(failMethods.head,failMethods.tail,fc,shouldFail = false)
      case (CDSReturnCode.NOTFOUND,Some(fc))=>
        loggerCollection.error("Route failed. Executing failure methods",None)
        if(failMethods.nonEmpty)
          runNextMethod(failMethods.head,failMethods.tail,fc,shouldFail = false)
      case (CDSReturnCode.SUCCESS,Some(fc))=>
        loggerCollection.log("Route succeeded. Executing success methods",None)
        if(successMethods.nonEmpty)
          runNextMethod(successMethods.head,successMethods.tail,fc,shouldFail = false)

    }

    loggerCollection.teardown
    //methods.foreach(curMethod=>{curMethod.execute})
  }
}
