package wildcat.pipeline

import chisel3._

/**
 * Does this stage do anything?
 */
class WriteBack extends Module {
  val io = IO(new Bundle {
    val memwb = Input(new MemWb())
    val wbdec = Output(new WbDec())
    val stall = Input(Bool())
  })
  // needs stall and maybe flash
  val memwbReg = RegNext(io.memwb, init = 0.U.asTypeOf(new MemWb()))
  io.wbdec.data := Mux(memwbReg.isMem, memwbReg.memData, memwbReg.data)
  io.wbdec.regNr := memwbReg.regNr
  io.wbdec.valid := memwbReg.valid
}
