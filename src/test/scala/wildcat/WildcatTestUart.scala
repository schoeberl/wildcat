package wildcat

import chisel.lib.uart._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import wildcat.Util._
import chisel3._

import scala.sys.process._
import scala.util.Properties

// the ignore parameter make sbt test to ignore the test
class WildcatTestUart(ignore: String) extends AnyFlatSpec with ChiselScalatestTester {

  class UartTest(app: String) extends Module {
    val io = IO(new Bundle {
      val stop = Output(Bool())
      val sendChannel = Flipped(new UartIO())
      val receiveChannel = new UartIO()
    })
    val wild = Module(new WildcatTestTop(app))
    io.stop := wild.io.stop

    val testTx = Module(new BufferedTx(100000000, 115200))
    val testRx = Module(new Rx(100000000, 115200))

    wild.io.rx := testTx.io.txd
    testRx.io.rxd := wild.io.tx
    testTx.io.channel <> io.sendChannel
    testRx.io.channel <> io.receiveChannel
  }

  val progs = List("asm/apps/echo.s")
  progs.foreach(p => {
    println(s"Running test $p")
    s"Program $p" should "pass" in {
      var app = p
      if (p.endsWith(".s")) {
        "rm a.out".!
        s"make app APP=$p".!
        app = "a.out"
      }
      test(new UartTest(app)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) {
        d => {
          val hello = "ABCDEFGHIJK"
          d.clock.setTimeout(0)
          var stop = false
          var pos = 0
          var cnt = 0

          while(!stop && cnt < 500000) {
            d.io.receiveChannel.ready.poke(true.B)
            d.io.sendChannel.valid.poke(false.B)
            if (d.io.receiveChannel.valid.peekBoolean()) {
              print("rcv: " + d.io.receiveChannel.bits.peekInt().toChar + "\n")
            }
            if (pos < hello.length && d.io.sendChannel.ready.peekBoolean()) {
              d.io.sendChannel.valid.poke(true.B)
              d.io.sendChannel.bits.poke(hello(pos).U)
              println("pos: " + pos + " of " + hello.length + "send: " + hello(pos))
              pos += 1
            } else if (pos == hello.length) {
              stop = true
            }
            d.clock.step(1)
            if (d.io.stop.peekBoolean()) {
              stop = true
            }
            cnt += 1
          }
        }
      }
    }
  })
}
