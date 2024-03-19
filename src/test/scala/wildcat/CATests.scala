package wildcat

import chisel3._
import chiseltest._
import chisel3.util.experimental.BoringUtils

import org.scalatest.flatspec.AnyFlatSpec
import wildcat.pipeline.FiveCats

import scala.sys.process._

/**
 * Test with the CAE tests.
 * Living at a repo relative to the Wildcat one.
 */
class CATests extends AnyFlatSpec with ChiselScalatestTester {

  val files = Util.getSimpleTests("../risc-v-lab/tests/simple")

  class TestTop(args: Array[String]) extends Module {
    val io = IO(new Bundle {
      val regFile = Output(Vec(32,UInt(32.W)))
    })
    val wc = Module(new FiveCats(args))
    io.regFile := DontCare
    BoringUtils.bore(wc.decode.regs, Seq(io.regFile))
  }

  for (f <- files) {
    s"CA test (simple) $f" should "pass" in {
      val result = Util.readBin(f.getAbsolutePath.substring(0, f.getAbsolutePath.length - 4) + ".res")
      test(new TestTop(Array(f.getAbsolutePath))) {
        d => {
          d.clock.step(20)
          for (i <- 0 until 32) {
            val r = d.io.regFile(i).peekInt()
            val e = result(i).toLong & 0xffffffffL
            println(f"reg($i) = ${r}, expected ${result(i)}")
            d.io.regFile(i).expect(e.U)
          }
        }
      }
    }
  }

}
