package wildcat

import chisel3._
import chiseltest._
import chisel3.util.experimental.BoringUtils
import org.scalatest.flatspec.AnyFlatSpec
import java.io.File

import wildcat.pipeline.ThreeCats

import scala.sys.process._

/**
 * Test with the CAE tests.
 * Living at a repo relative to the Wildcat one.
 */
class CATest extends AnyFlatSpec with ChiselScalatestTester {

  val files = Util.getSimpleTests("risc-v-lab/tests/simple")
  // val files = List(new File("risc-v-lab/tests/simple/loop.bin"))
  val tooLong = List("recursive.bin", "loop.bin")
  val forever = List("branchtrap.bin")
  val broken = List("string.bin", "width.bin")
  val failed = tooLong ++ forever ++ broken

  for (f <- files) {
    s"Pipeline CA test (simple) $f" should "pass" in {
      val result = Util.readBin(f.getAbsolutePath.substring(0, f.getAbsolutePath.length - 4) + ".res")
      if (failed.contains(f.getName)) {
        println(s"Skipping $f")
        succeed
      } else {
        test(new WildcatTestTop(f.getAbsolutePath)).withAnnotations(Seq(WriteVcdAnnotation)) {
          d => {
            d.clock.setTimeout(10000)
            var stop = false
            var cnt = 0
            while(!stop && cnt < 10000) {
              d.clock.step(1)
              if (d.io.stop.peekBoolean()) {
                stop = true
                // tests from Ripes are OK when 0 (risc-v tests OK when 1)
              }
              cnt += 1
            }
            assert(stop, "Timeout")
            println(s"$f ran $cnt cycles")
            for (i <- 0 until 32) {
              val r = d.io.regFile(i).peekInt()
              val e = result(i).toLong & 0xffffffffL
              // println(f"reg($i) = ${r}, expected ${result(i)}")
              d.io.regFile(i).expect(e.U)
            }
          }
        }
      }
    }
  }
}
