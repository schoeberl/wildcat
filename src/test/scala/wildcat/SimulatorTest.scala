package wildcat

import scala.sys.process._
import org.scalatest.flatspec.AnyFlatSpec

import wildcat.isasim._

class SimulatorTest extends AnyFlatSpec {
  "Simulator" should "pass" in {
    "make app".!
    SimRV.main(Array("a.bin"))
  }

}
