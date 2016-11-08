import org.scalatest.{FlatSpec, Matchers}
import CDS.CDSMethod
import config.CDSConfig
import logging.LogCollection
import java.nio.file.Path

import scala.io
import org.scalamock.scalatest.MockFactory

class CDSMethodSpec  extends FlatSpec with Matchers with MockFactory {
  val SCRIPTDIR = "src/test/resources/scripts"

  "A CDS.CDSMethod" should "find an existing script file" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())

    val m = CDSMethod("test-method","simpletest",Seq(),Map(),log,None,cfg)
    m.findFile should be (Some("src/test/resources/scripts/simpletest.sh"))
  }

  it should "return None for a script file that does not exist" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())

    val m = CDSMethod("test-method","notexistingscript",Seq(),Map(),log,None,cfg)
    m.findFile should be (None)
  }
}
