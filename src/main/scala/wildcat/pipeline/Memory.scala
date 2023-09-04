package wildcat.pipeline

import chisel3._
import wildcat.pipeline.Common._

class Memory extends Module {
  val io = IO(new Bundle {
    val exmem = Input(new ExMem())
    val memwb = Output(new MemWb())
    val stall = Input(Bool())
  })

  // needs stall and maybe flash
  val exmemReg = RegNext(io.exmem, init = 0.U.asTypeOf(new ExMem()))
  val mem = SyncReadMem(32, UInt(32.W), SyncReadMem.WriteFirst)

  val memRead = mem.read(io.exmem.addr)
  when(io.exmem.ena) {
    mem.write(io.exmem.addr, io.exmem.data)
  }

  io.memwb.isMem := false.B
  io.memwb.memData := memRead
  io.memwb.regNr := exmemReg.regNr
  io.memwb.data := exmemReg.data
  io.memwb.valid := exmemReg.valid
}
