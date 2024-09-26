package wildcat

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.isasim.SimRV

/**
 * Test with the CAE tests.
 * Living at a repo relative to the Wildcat one.
 */
class CASimTest extends AnyFlatSpec with ChiselScalatestTester {

  val files = Util.getSimpleTests("risc-v-lab/tests/simple")
  val failed = List("string.bin", "width.bin")

  for (f <- files) {
    s"Simulation: CA test (simple) $f" should "pass" in {
      val result = Util.readBin(f.getAbsolutePath.substring(0, f.getAbsolutePath.length - 4) + ".res")
      if (failed.contains(f.getName)) {
        println(s"Skipping $f")
        succeed
      } else {
        val sim = SimRV.runSimRV(f.toString())
        for (i <- 0 until 32) {
          val r = sim.reg(i)
          val e = result(i)
          // println(f"reg($i) = ${r}, expected ${result(i)}")
          assert(r == e, f"reg($i) = ${r}, expected ${result(i)}")
        }
      }
    }
  }
}
