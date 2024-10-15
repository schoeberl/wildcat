package wildcat

import scala.sys.process._
import org.scalatest.flatspec.AnyFlatSpec

import wildcat.isasim._

class SimulatorTest extends AnyFlatSpec {

  // val files = Util.getAllTests()
  // val files = List("rv32ui/lbu.s")
  val files = List("rv32ui/add.s")
  for (f <- files) {
    s"Simulator $f" should "pass" in {
      s"make app APP=$f".!
      val sim = SimRV.runSimRV("a.out")
      assert(sim.reg(28) == 1, f"Failed case ${sim.reg(3)}")
    }
  }
}
