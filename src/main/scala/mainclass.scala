/**
  * Created by localhome on 13/10/2016.
  */
import CDS.CDSRoute

import scala.xml.parsing.ConstructingParser
import scala.io.Source
import scala.xml.Node

object mainclass {

  def main(args: Array[String]):Unit = {
    val route = CDSRoute.fromFile("test.xml")
    route.dump

  }
}
