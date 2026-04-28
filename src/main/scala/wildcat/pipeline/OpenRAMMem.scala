package wildcat.pipeline

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import soc._
import memory.SramMacro

/**
 * On-chip memory with one clock cycle read timing and write forwarding.
 * Wrapper for OpenRAM, which is used in the tape-out.
 */
class OpenRAMMem(data: Array[Int], nrBytes: Int = 1024) extends PipeConDevice(32) {

  val mem = Module(new SramMacro)
  mem.io.clk0 := clock
  mem.io.csb0 := false.B
  mem.io.web0 := !cpuPort.wr
  mem.io.wmask0 := cpuPort.wrMask
  mem.io.addr0 := cpuPort.address(9, 2)
  mem.io.din0 := cpuPort.wrData
  cpuPort.rdData := mem.io.dout0

  mem.io.clk1 := clock
  mem.io.csb1 := false.B
  mem.io.addr1 := 0.U
  // io.dout := mem.io.dout1

  cpuPort.ack := RegNext(cpuPort.rd || cpuPort.wr, false.B)
}
