package wildcat

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.pipeline.FiveCats
import wildcat.three.ThreeTop

import scala.sys.process._

class SingleTest extends AnyFlatSpec with ChiselScalatestTester {

  val f = "asm/test.s"
  s"Single $f" should "pass" in {
    s"make app APP=$f".!
//    test(new FiveCats(Array("a.bin"))).withAnnotations(Seq(WriteVcdAnnotation)) {
    test(new ThreeTop(Array("a.bin"))).withAnnotations(Seq(WriteVcdAnnotation)) {
      d => {
        d.clock.step(20)
      }
    }
  }
}
