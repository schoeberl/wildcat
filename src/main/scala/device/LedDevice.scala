package device

import chisel3._
import soc._

/**
 * Simple LED device
 */
class LedDevice(n: Int = 16) extends PipeConDevice(32) {
  val io = IO(new Bundle {
    val leds = Output(UInt(n.W))
  })

  assert(n < 32, "Maximum number of LEDs is 32, as the data bus is 32 bits wide")

  val ledsReg = RegInit(0.U(n.W))

  when (cpuPort.wr) {
    ledsReg := cpuPort.wrData(n-1, 0)
  }
  cpuPort.ack := RegNext(cpuPort.rd || cpuPort.wr, false.B)
  cpuPort.rdData := 0.U

  io.leds := ledsReg
}
