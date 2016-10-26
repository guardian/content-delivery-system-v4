import config.CDSConfig
import org.scalatest._
import scala.io.Source

class CDSConfigSpec extends FlatSpec with Matchers {
  "A CDSConfig" should "parse a YAML file" in {
    val cfg = CDSConfig.load("src/test/resources/testconfig.yml")
    cfg.dump
  }

  it should "return a LogCollection for the setup loggers" in {
    val cfg = CDSConfig.load("src/test/resources/testconfig.yml")
    cfg.loggers.size should be (2) //this includes the invalid one
    val lc = cfg.getLogCollection
    lc.activeLoggerCount should be (1) //this should not include the invalid one

  }
}
