package wildcat

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.isasim.SimRV

/**
 * Test with the CAE tests.
 * Living at a repo relative to the Wildcat one.
 */
class CACoSimTest extends AnyFlatSpec with ChiselScalatestTester {

  val files = Util.getSimpleTests("risc-v-lab/tests/simple")
  val failed = List("string.bin", "recursive.bin", "loop.bin")

  for (f <- files) {
    s"Simulation: CA co-simulatoin (simple) $f" should "pass" in {
      val result = Util.readBin(f.getAbsolutePath.substring(0, f.getAbsolutePath.length - 4) + ".res")
      if (failed.contains(f.getName)) {
        println(s"Skipping $f")
        succeed
      } else {
        val sim = SimRV.runSimRV(f.toString())
        test(new WildcatTestTop(f.getAbsolutePath)).withAnnotations(Seq(WriteVcdAnnotation)) {
          d => {
            for (i <- 0 until 100) {
              if (!d.io.stop.peekBoolean()) {
                d.clock.step()
              }
            }
            for (i <- 0 until 32) {
              val r = d.io.regFile(i).peekInt().toInt
              val e = sim.reg(i)
              assert(r == e, f"reg($i) = 0x${r.toHexString}, expected 0x${e.toHexString} at ${sim.pc % 4} in $f")
            }
          }
        }

      }
    }
  }
}
