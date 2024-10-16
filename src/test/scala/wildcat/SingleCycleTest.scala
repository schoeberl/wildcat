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
    case None => getAsmFiles("rv32ui") // getAsmFiles() ++ getAsmFiles("risc-v-lab/tests/ripes")  ++ getAsmFiles("risc-v-lab/tests/riscv-tests")
  }
  val failed = List("rv32ui/lwu.s", "lrsc.s", "rv32ui/jalr.s", "rv32ui/sb.s", "rv32ui/sh.s", "rv32ui/lbu.s", "rv32ui/jal.s", "rv32ui/bgeu.s",
    "rv32ui/auipc.s", "rv32ui/lhu.s", "rv32ui/sw.s", "rv32ui/bltu.s", "rv32ui/lw.s", "rv32ui/lb.s", "rv32ui/lh.s")
  //List("risc-v-lab/tests/ripes/memory.s", "risc-v-lab/tests/riscv-tests/jalr.s", "asm/riscv-v1_lw.s")
  val progs = allProgs.filterNot(failed.contains(_))
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
              // tests from Ripes are OK when 0 (risc-v tests OK when 1)
              assert(d.io.regs(28).peekInt() == 1, s"Failed case ${d.io.regs(3).peekInt()}")
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
