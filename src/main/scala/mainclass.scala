/**
  * Created by localhome on 13/10/2016.
  */
import CDS.CDSRoute
import config.CDSConfig

import scala.xml.parsing.ConstructingParser
import scala.io.Source
import scala.xml.Node

object mainclass {
val usage =
  """
    |Usage: cds_run --route {routename} [--input-inmeta /path/to/inmeta.xml] [--input-meta /path/to/meta.xml] [--input-media /path/to/mediafile] [--input-xml /path/to/xmlfile]
    |Runs the Content Delivery System
    |You must specify a route to run, these are by default XML files located in /etc/cds_backend/routes.
    |Other parameters are optional, and used if you specify an input-method of commandline in the route.
  """.stripMargin

  def main(args: Array[String]):Unit = {
    val config = CDSConfig.load("src/test/resources/testconfig.yml")
    type OptionMap = Map[Symbol, String]

    //see http://stackoverflow.com/questions/2315912/scala-best-way-to-parse-command-line-parameters-cli
    def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
      def isSwitch(s : String) = (s(0) == '-')
      list match {
        case Nil => map
        case "--route" :: value :: tail =>
          nextOption(map ++ Map('routename -> value.toString), tail)
        case "--input-inmeta" :: value :: tail =>
          nextOption(map ++ Map('inmeta -> value.toString), tail)
        case "--input-meta" :: value :: tail =>
          nextOption(map ++ Map('meta -> value.toString), tail)
        case "--input-xml" :: value :: tail =>
          nextOption(map ++ Map('xml -> value.toString), tail)
        case "--input-media" :: value :: tail =>
          nextOption(map ++ Map('media -> value.toString), tail)
        case option :: tail => println("-WARNING: Unknown option "+option)
          map
      }
    }
    val options = nextOption(Map(),args.toList)

    val route=CDSRoute.fromFile(options('routename),config)
    route.dump
  }
}
