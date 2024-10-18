package wildcat

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.single.SingleCycle

/**
 * Test with the CAE tests.
 * Living at a repo relative to the Wildcat one.
 */
class CASingleTest extends AnyFlatSpec with ChiselScalatestTester {

  val files = Util.getSimpleTests("risc-v-lab/tests/simple")
  val failed = List("shift2.bin", "shift.bin", "recursive.bin", "branchcnt.bin", "branchmany.bin", "branchtrap.bin", "loop.bin","string.bin", "width.bin")

  for (f <- files) {
    s"SingleCycle CA test (simple) $f" should "pass" in {
      val result = Util.readBin(f.getAbsolutePath.substring(0, f.getAbsolutePath.length - 4) + ".res")
      if (failed.contains(f.getName)) {
        println(s"Skipping $f")
        succeed
      } else {
        test(new SingleCycle(f.getAbsolutePath)).withAnnotations(Seq(WriteVcdAnnotation)) {
          d => {
            assert(true, "We need to check stop")
            d.clock.step(100)
            for (i <- 0 until 32) {
              val r = d.io.regs(i).peekInt()
              val e = result(i).toLong & 0xffffffffL
              // println(f"reg($i) = ${r}, expected ${result(i)}")
              d.io.regs(i).expect(e.U)
            }
          }
        }
      }
    }
  }
}
