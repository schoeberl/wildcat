package wildcat.common

import chisel3._

/**
 * On-chip memory with one clock cycle read timing and write forwarding
 */
class ScratchPadMem extends Module {
  val io = IO(Flipped(new MemIO()))

  // does forwarding due to WriteFirst
  val mem = SyncReadMem(32, UInt(32.W), SyncReadMem.WriteFirst)

  // TODO: use the mask
  io.rdData := mem.read(io.rdAddress)
  when(io.wrEnable === 15.U) {
    mem.write(io.wrAddress, io.wrData)
  }
  io.stall := false.B
}
