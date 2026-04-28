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
class OpenRAMMem(data: Array[Int], nrBytes: Int = 1024) extends Module {
  val io = IO(PipeCon(32))

  val mem = Module(new SramMacro)
  mem.io.clk0 := clock
  mem.io.csb0 := false.B
  mem.io.web0 := !io.wr
  mem.io.wmask0 := io.wrMask
  mem.io.addr0 := io.address(9, 2)
  mem.io.din0 := io.wrData
  io.rdData := mem.io.dout0

  mem.io.clk1 := clock
  mem.io.csb1 := false.B
  mem.io.addr1 := 0.U
  // io.dout := mem.io.dout1

  io.ack := RegNext(io.rd || io.wr, false.B)
}
