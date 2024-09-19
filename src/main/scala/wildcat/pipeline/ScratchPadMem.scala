package wildcat.pipeline

import chisel3._
import chisel3.util._

/**
 * On-chip memory with one clock cycle read timing and write forwarding
 */
class ScratchPadMem(nrBytes: Int = 4096) extends Module {
  val io = IO(Flipped(new MemIO()))

  val mem = SyncReadMem(nrBytes/4, UInt(32.W), SyncReadMem.WriteFirst)

  val idx = log2Up(nrBytes/4)
  io.rdData := mem.read(io.rdAddress(idx+2, 2))
  when(io.wrEnable === 15.U) {
    mem.write(io.wrAddress(idx+2, 2), io.wrData)
  }
  io.stall := false.B
}
