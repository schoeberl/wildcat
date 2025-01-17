package wildcat

import wildcat.Util._
import scala.util.Properties

import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import scala.sys.process._

class WildcatTest() extends AnyFlatSpec with ChiselScalatestTester {

  val allProgs = Properties.envOrNone("test") match {
    case Some(t) => List(t)
    case None => getAllTests()
  }
  val failed = List("")
  val progs = allProgs.filterNot(failed.contains(_))
  progs.foreach(p => {
    println(s"Running test $p")
    s"Program $p" should "pass" in {
      var app = p
      if (p.endsWith(".s")) {
        "rm a.out".!
        s"make app APP=$p".!
        app = "a.out"
      }
      //    test(new FiveCats(Array("a.out"))).withAnnotations(Seq(WriteVcdAnnotation)) {
      test(new WildcatTestTop(app)).withAnnotations(Seq(WriteVcdAnnotation)) {
        d => {
          var stop = false
          var cnt = 0
          d.clock.setTimeout(0)
          while(!stop && cnt < 2000) {
            d.clock.step(1)
            if (d.io.stop.peekBoolean()) {
              stop = true
              assert(d.io.regFile(10).peekInt() == 0, s"Failed test case ${d.io.regFile(3).peekInt()}")
            }
            if (false) {
              for(i <- 0 until 16) {
                val r = d.io.regFile(i).peekInt()
                print(f"reg($i) = ${r} ")
              }
              println()
            }
            cnt += 1
          }
          assert(stop, "Timeout")

          if (false) {
            for(i <- 0 until 32) {
              val r = d.io.regFile(i).peekInt()
              println(f"reg($i) = ${r}")
            }
          }

        }
      }
    }
  })
}
