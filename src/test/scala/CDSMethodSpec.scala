import org.scalatest.{FlatSpec, Matchers}
import CDS.{CDSMethod,CDSReturnCode}
import config.CDSConfig
import logging.LogCollection
import java.nio.file.Path

import scala.io
import org.scalamock.scalatest.MockFactory

class CDSMethodSpec  extends FlatSpec with Matchers with MockFactory {
  val SCRIPTDIR = "src/test/resources/scripts"

  "A CDS.CDSMethod" should "find an existing script file" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethod("test-method","simpletest",Seq(),Map(),log,None,cfg)
    m.findFile should be (Some("src/test/resources/scripts/simpletest.sh"))

    val n = CDSMethod("test-method","simpletestp",Seq(),Map(),log,None,cfg)
    n.findFile should be (Some("src/test/resources/scripts/simpletestp.pl"))

    val o = CDSMethod("test-method","simpletestr",Seq(),Map(),log,None,cfg)
    o.findFile should be (Some("src/test/resources/scripts/simpletestr.rb"))
  }

  it should "return None for a script file that does not exist" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())

    val m = CDSMethod("test-method","notexistingscript",Seq(),Map(),log,None,cfg)
    m.findFile should be (None)
  }

  it should "run a script and return success on status code of 0" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethod("test-method","simpletest",Seq(),Map(),log,None,cfg)
    m.findFile should be (Some("src/test/resources/scripts/simpletest.sh"))
    m.execute should be (CDSReturnCode.SUCCESS)
  }

  it should "run a script and return error on status code of 1" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethod("test-method","testfailure",Seq(),Map(),log,None,cfg)
    m.execute should be (CDSReturnCode.FAILURE)
  }

  it should "run a script and return stop-route on status code of 2" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethod("test-method","testfatal",Seq(),Map(),log,None,cfg)
    m.execute should be (CDSReturnCode.STOPROUTE)
  }

  it should "run a script and return unknown on other status code" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethod("test-method","testunknown",Seq(),Map(),log,None,cfg)
    m.execute should be (CDSReturnCode.UNKNOWN)
  }
}
