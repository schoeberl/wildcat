package wildcat

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.pipeline.Wildcat

import scala.sys.process._

class SingleTest extends AnyFlatSpec with ChiselScalatestTester {

  val f = "asm/test.s"
  s"Single $f" should "pass" in {
    s"make app APP=$f".!
    test(new Wildcat(Array("a.bin"))) {
      d => {
        d.clock.step(20)
      }
    }
  }
}