package wildcat

import org.scalatest.flatspec.AnyFlatSpec
import wildcat.isasim._

import scala.sys.process._

class SimulatorSingleTest extends AnyFlatSpec {

  val sim = SimRV.runSimRV("a.out")
  assert(sim.reg(10) == 0, f"Failed case ${sim.reg(3)}")
}
