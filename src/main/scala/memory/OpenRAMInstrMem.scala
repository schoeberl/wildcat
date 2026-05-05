package memory

import chisel3._
import soc._

/**
 * On-chip memory with one clock cycle read timing.
 * Wrapper for OpenRAM, which is used in the tape-out.
 */
class OpenRAMInstrMem(nrBytes: Int = 1024) extends PipeConDevice(32) {

  val writePort = IO(new PipeCon(10))

  val mem = Module(new SramMacro)

  // read port, fetch instructions
  mem.io.clk1 := clock
  mem.io.csb1 := false.B
  mem.io.addr1 := cpuPort.address(9, 2)
  cpuPort.rdData := mem.io.dout0

  cpuPort.ack := RegNext(cpuPort.rd || cpuPort.wr, false.B)

  // write port, for writing instructions
  mem.io.clk0 := clock
  mem.io.csb0 := false.B
  mem.io.web0 := !writePort.wr
  mem.io.wmask0 := writePort.wrMask
  mem.io.addr0 := writePort.address(9, 2)
  mem.io.din0 := writePort.wrData
  writePort.rdData := mem.io.dout0

  writePort.ack := RegNext(writePort.rd || writePort.wr, false.B)
}
