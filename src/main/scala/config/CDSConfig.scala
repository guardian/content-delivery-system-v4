package config

import CDS.CDSRoute
import datastore.Datastore
import logging.{LogCollection, Logger}
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConversions._

/**
  * Created by localhome on 26/10/2016.
  */
object CDSConfig {
  def load(path:String,routename:String):CDSConfig = {
    val source = scala.io.Source.fromFile(path)
    val lines = try source.mkString finally source.close()

    val configdata=new Yaml().load(lines).asInstanceOf[java.util.Map[String, Any]]

    val loggerlist = configdata.get("loggers").asInstanceOf[java.util.ArrayList[java.util.Map[String,String]]].toSet

    val logListConfig:Set[Option[LoggerConfig]] = loggerlist.map(x=>{
      try {
        Some(LoggerConfig.newLoggerConfig(x))
      } catch {
        case e:java.util.NoSuchElementException=>
          println("ERROR: Logging definition " + x + " is not valid: " + e.getMessage)
          None
      }
    })

    val datastoreConfig = configdata.get("datastore").asInstanceOf[java.util.Map[String,String]].toMap

    println("INFO: Attempting to initialise datastore " + datastoreConfig.get("class"))
    val datastoreImpl = try {
      val cstrct = Class.forName(datastoreConfig.getOrElse("class","(no class provided)")).getConstructors

      Some(cstrct(0).newInstance(datastoreConfig ++ Map("routename"->routename)).asInstanceOf[Datastore])
    } catch {
      case e:ClassNotFoundException=>
        println("-ERROR: Logger " +
          datastoreConfig.getOrElse("class","(no class provided)") +
          " could not be loaded (class not found).  Please check your config file."
        )
        None
    }

    //filters out any defs that did not load properly and converts to concrete values
    CDSConfig(logListConfig.filter(x=>x match {
                case Some(g)=>true
                case _=>false
              }).map(x=>x.get),
      datastoreImpl
    )

  }

  def placeholder = CDSConfig(Set(),None)
}

case class CDSConfig(loggers:Set[LoggerConfig],datastore:Option[Datastore]) {
  def getLogCollection(routeName:String,routeType:String):LogCollection = {
    LogCollection.fromConfig(loggers,routeName,routeType)
  }


  def dump = {
    println("Logger config:")
    loggers.foreach(x=>x.dump)

  }
}