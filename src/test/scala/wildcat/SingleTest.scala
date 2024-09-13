package wildcat

import scala.util.Properties

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.pipeline.FiveCats
import wildcat.three.ThreeTop

import scala.sys.process._

class SingleTest() extends AnyFlatSpec with ChiselScalatestTester {


  val f = Properties.envOrElse("test", "asm/app/blink.s")
  println(s"Running test $f")
  // val f = "asm/app/blink.s"
  // val f = "asm/riscv-v1_addi.s"
  s"Single $f" should "pass" in {
    s"make app APP=$f".!
//    test(new FiveCats(Array("a.bin"))).withAnnotations(Seq(WriteVcdAnnotation)) {
    test(new ThreeTop(Array("a.bin"))).withAnnotations(Seq(WriteVcdAnnotation)) {
      d => {
        d.clock.step(100)
      }
    }
  }
}
