package wildcat

import scala.util.Properties

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.pipeline.FiveCats
import wildcat.three.ThreeTop

import scala.sys.process._

class SingleTest() extends AnyFlatSpec with ChiselScalatestTester {


  val f = Properties.envOrElse("test", "asm/apps/blink.s")
  println(s"Running test $f")
  // val f = "asm/apps/blink.s"
  // val f = "asm/riscv-v1_addi.s"
  s"Single $f" should "pass" in {
    s"make app APP=$f".!
//    test(new FiveCats(Array("a.bin"))).withAnnotations(Seq(WriteVcdAnnotation)) {
    test(new ThreeTestTop(Array("a.bin"))).withAnnotations(Seq(WriteVcdAnnotation)) {
      d => {
        d.clock.step(100)
        for(i <- 0 until 32) {
          val r = d.io.regFile(i).peekInt()
          println(f"reg($i) = ${r}")
        }
      }
    }
  }
}
