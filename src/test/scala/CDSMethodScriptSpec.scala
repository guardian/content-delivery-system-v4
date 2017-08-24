import org.scalatest.{FlatSpec, Matchers}
import CDS.{FileCollection, CDSMethodScript, CDSReturnCode}
import config.CDSConfig
import logging.{LogCollection, LogMessage, Logger}
import org.scalamock.scalatest.MockFactory
import java.io.File
import scala.concurrent.Future

class CDSMethodScriptSpec  extends FlatSpec with Matchers with MockFactory {
  val SCRIPTDIR = "src/test/resources/scripts"

  "A CDS.CDSMethodScript" should "find an existing script file" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethodScript("test-method","simpletest",Seq(),Map(),log,None,cfg)
    m.findFile should be (Some("src/test/resources/scripts/simpletest.sh"))

    val n = CDSMethodScript("test-method","simpletestp",Seq(),Map(),log,None,cfg)
    n.findFile should be (Some("src/test/resources/scripts/simpletestp.pl"))

    val o = CDSMethodScript("test-method","simpletestr",Seq(),Map(),log,None,cfg)
    o.findFile should be (Some("src/test/resources/scripts/simpletestr.rb"))
  }

  it should "return None for a script file that does not exist" in {
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())

    val m = CDSMethodScript("test-method","notexistingscript",Seq(),Map(),log,None,cfg)
    m.findFile should be (None)
  }

  it should "run a script and return success on status code of 0" in {
    val tempfile = File.createTempFile("cds_","_testfile.tmp")
    tempfile.createNewFile()
    tempfile.deleteOnExit()
    val fc = FileCollection("","","","",new java.net.URI("file:///none"),tempfile.getAbsolutePath)
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethodScript("test-method","simpletest",Seq(),Map(),log,None,cfg)
    m.findFile should be (Some("src/test/resources/scripts/simpletest.sh"))
    m.execute(fc) should be (CDSReturnCode.SUCCESS,List(fc))
  }

  it should "run a script and return error on status code of 1" in {
    val tempfile = File.createTempFile("cds_","_testfile.tmp")
    tempfile.createNewFile()
    tempfile.deleteOnExit()
    val fc = FileCollection("","","","",new java.net.URI("file:///none"),tempfile.getAbsolutePath)
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethodScript("test-method","testfailure",Seq(),Map(),log,None,cfg)
    m.execute(fc) should be (CDSReturnCode.FAILURE,List(fc))
  }

  it should "run a script and return stop-route on status code of 2" in {
    val tempfile = File.createTempFile("cds_","_testfile.tmp")
    tempfile.createNewFile()
    tempfile.deleteOnExit()
    val fc = FileCollection("","","","",new java.net.URI("file:///none"),tempfile.getAbsolutePath)
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethodScript("test-method","testfatal",Seq(),Map(),log,None,cfg)
    m.execute(fc) should be (CDSReturnCode.STOPROUTE,List(fc))
  }

  it should "run a script and return unknown on other status code" in {
    val tempfile = File.createTempFile("cds_","_testfile.tmp")
    tempfile.createNewFile()
    tempfile.deleteOnExit()
    val fc = FileCollection("","","","",new java.net.URI("file:///none"),tempfile.getAbsolutePath)
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val log = LogCollection(Seq())  //use an empty LogCollection

    val m = CDSMethodScript("test-method","testunknown",Seq(),Map(),log,None,cfg)
    m.execute(fc) should be (CDSReturnCode.UNKNOWN,List(fc))
  }

  it should "set environment variables according to params for the method, and test script should echo them back" in {
    val tempfile = File.createTempFile("cds_","_testfile.tmp")
    tempfile.createNewFile()
    tempfile.deleteOnExit()
    val fc = FileCollection("","","","",new java.net.URI("file:///none"),tempfile.getAbsolutePath)
    val cfg = CDSConfig.placeholder(Map("methods"->SCRIPTDIR))
    val lg = mock[Logger]
    val log = LogCollection(Seq(lg))

    val m = CDSMethodScript("test-method","envtest",Seq(),Map("key1"->"value1","key2"->"value2"),log,None,cfg)
    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("Executing method envtest as test-method","info",Some(m)))
    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("Testing passed environment","info",Some(m)))
    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("key1 => value1","info",Some(m)))
    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("key2 => value2","info",Some(m)))
    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("keyInval => ","info",Some(m)))
    (lg.relayMessage _:LogMessage=>Future[Unit]).expects(LogMessage("Method exited cleanly","info",Some(m)))
    m.execute(fc) should be (CDSReturnCode.SUCCESS,List(fc))

  }

}
