package wildcat

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.Util._
import wildcat.single.SingleCycle

import scala.sys.process._
import scala.util.Properties

class SingleCycleTest() extends AnyFlatSpec with ChiselScalatestTester {

  val allProgs = Properties.envOrNone("test") match {
    case Some(t) => List(t)
    case None => getAllTests()
  }
  val failed = List("risc-v-lab/tests/riscv-tests/bltu.s", "risc-v-lab/tests/riscv-tests/jalr.s", "risc-v-lab/tests/riscv-tests/bgeu.s")
  // val progs = allProgs.filterNot(failed.contains(_))
  val progs = List("risc-v-lab/tests/ripes/or.s")
  progs.foreach(p => {
    println(s"Running test $p")
    s"Program $p" should "pass" in {
      "rm a.out".!
      s"make app APP=$p".!
      //    test(new FiveCats(Array("a.out"))).withAnnotations(Seq(WriteVcdAnnotation)) {
      test(new SingleCycle("a.out")).withAnnotations(Seq(WriteVcdAnnotation)) {
        d => {
          var stop = false
          var cnt = 0
          while(!stop && cnt < 999) {
            d.clock.step(1)
            if (d.io.stop.peekBoolean()) {
              stop = true
              assert(d.io.regs(10).peekInt() == 0, s"Failed case ${d.io.regs(3).peekInt()}")
            }
            cnt += 1
          }
          assert(stop, "Timeout")
          /*
          for(i <- 0 until 32) {
            val r = d.io.regFile(i).peekInt()
            println(f"reg($i) = ${r}")
          }
           */
        }
      }
    }
  })
}
