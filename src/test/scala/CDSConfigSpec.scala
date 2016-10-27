import CDS.CDSRoute
import config.CDSConfig
import org.scalatest._

import scala.io.Source

class CDSConfigSpec extends FlatSpec with Matchers {
  "A CDSConfig" should "parse a YAML file" in {
    val cfg = CDSConfig.load("src/test/resources/testconfig.yml")
    cfg.dump
  }

  it should "return a LogCollection containing only valid loggers" in {
    val cfg = CDSConfig.load("src/test/resources/testconfig.yml")
    cfg.loggers.size should be (3) //this includes the invalid one
    val lc = cfg.getLogCollection("test route","test")
    lc.activeLoggerCount should be (2) //this should not include the invalid one

  }
}
