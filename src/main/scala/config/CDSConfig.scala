package config

import logging.LogCollection
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConversions._

/**
  * Created by localhome on 26/10/2016.
  */
object CDSConfig {
  def load(path:String):CDSConfig = {
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

    //filters out any defs that did not load properly and converts to concrete values
    CDSConfig(logListConfig.filter(x=>x match {
                case Some(g)=>true
                case _=>false
              }).map(x=>x.get)
    )

  }

  def placeholder = CDSConfig(Set())
}

case class CDSConfig(loggers:Set[LoggerConfig]) {
  def getLogCollection:LogCollection = {
    LogCollection.fromConfig(loggers)
  }
  def dump = {
    println("Logger config:")
    loggers.foreach(x=>x.dump)

  }
}