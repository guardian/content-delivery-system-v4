import CDS.CDSRoute
import config.CDSConfig
import org.scalatest._

import scala.io.Source
import scala.xml.parsing.ConstructingParser

/**
  * Created by localhome on 21/10/2016.
  */
class CDSRouteSpec extends FlatSpec with Matchers {

  "A CDS.CDSRoute" should "retrieve route name" in {
    val src = Source.fromFile("src/test/resources/routes/test.xml","utf8")
    val parser = new ConstructingParser(src,true)
    parser.initialize
    val doc = parser.document()

    CDSRoute.getMethodName(doc.head) should be ("test")
  }

  it should "build CDS.CDSMethod objects for present stanzas" in {
    val config = CDSConfig.placeholder(Map())
    val route = CDSRoute.fromFile("src/test/resources/routes/test.xml",config,Map())

    route.name should be ("test")
    route.routetype should be ("active")
    route.methods.length should be (2)

    //route.methods(0).dump
    route.methods(0).name should be ("commandline")
    route.methods(0).methodType should be ("input-method")
    route.methods(0).requiredFiles.length should be (1)

    //route.methods(1).dump
    route.methods(1).name should be ("something")
    route.methods(1).methodType should be ("input-method")
    route.methods(1).requiredFiles.length should be (2)
    route.methods(1).requiredFiles(0) should be ("media")
    route.methods(1).requiredFiles(1) should be ("inmeta")
    route.methods(1).params("hostname") should be ("robert")
    route.methods(1).params("username") should be ("kenneth")
    route.methods(1).params("password") should be ("rabbit")

  }

  it should "execute a sequence of methods" in {

  }

  it should "throw a runtime exception if a method attempts to enter batch mode" in {

  }
}
