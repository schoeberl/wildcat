package wildcat

import scala.sys.process._
import org.scalatest.flatspec.AnyFlatSpec

import wildcat.isasim._

class SimulatorTest extends AnyFlatSpec {

  // val files = Util.getAsmFiles("rv32ui", ".s")
  val files = Util.getAsmFiles("asm", ".s")
  for (f <- files) {
    s"Simulator $f" should "pass" in {
      s"make app APP=$f".!
      val sim = SimRV.runSimRV("a.bin")
      assert(sim.reg(28) == 1, f"Failed case ${sim.reg(3)}")
    }
  }
}
