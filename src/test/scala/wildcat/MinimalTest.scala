package wildcat

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.pipeline.Wildcat
class MinimalTest extends AnyFlatSpec with ChiselScalatestTester {

  "Minimal" should "have single cycle timing" in {
    test(new Wildcat()) {
      d => {

      }

    }
  }
}
