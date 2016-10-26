import config.CDSConfig
import org.scalatest._
import scala.io.Source

class CDSConfigSpec extends FlatSpec with Matchers {
  "A CDSConfig" should "parse a YAML file" in {
    val cfg = CDSConfig.load("src/test/resources/testconfig.yml")
    cfg.dump
  }
}
