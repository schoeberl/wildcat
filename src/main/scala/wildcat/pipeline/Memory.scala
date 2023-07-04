package wildcat.pipeline

import chisel3._
import wildcat.pipeline.Common._

class Memory extends Module {
  val io = IO(new Bundle {
    val exmem = Input(new ExMem())
    val memwb = Output(new MemWb())
  })

  val mem = SyncReadMem(32, UInt(32.W), SyncReadMem.WriteFirst)

  io.memwb.data := mem.read(io.exmem.addr)
  when(io.exmem.ena) {
    mem.write(io.exmem.addr, io.exmem.data)
  }

  // not exactly how I would like it
  // needs stall and flash
  io.memwb.regNr := RegNext(io.exmem.regNr)
  io.memwb.data := RegNext(io.exmem.data)
  io.memwb.valid := RegNext(io.exmem.valid)

  // io.exmem <> io.memwb
}
