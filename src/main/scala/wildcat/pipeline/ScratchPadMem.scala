package wildcat.pipeline

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

/**
 * On-chip memory with one clock cycle read timing and write forwarding
 */
class ScratchPadMem(data: Array[Int], nrBytes: Int = 4096) extends Module {
  val io = IO(Flipped(new MemIO()))

  val mem = SyncReadMem(nrBytes/4, UInt(32.W), SyncReadMem.WriteFirst)

  val dataHex = data.map(_.toHexString).mkString("\n")
  val file = new java.io.PrintWriter("data.hex")
  file.write(dataHex)
  file.close()
  loadMemoryFromFile(mem, "data.hex")

  val idx = log2Up(nrBytes/4)
  io.rdData := mem.read(io.rdAddress(idx+2, 2))
  when(io.wrEnable === 15.U) {
    mem.write(io.wrAddress(idx+2, 2), io.wrData)
  }
  io.stall := false.B
}
