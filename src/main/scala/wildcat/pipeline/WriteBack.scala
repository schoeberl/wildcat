package wildcat.pipeline

import chisel3._

/**
 * Does this stage do anything?
 */
class WriteBack extends Module {
  val io = IO(new Bundle {
    val memwb = Input(new MemWb())
    val wbdec = Output(new WbDec())
  })
  // needs stall and maybe flash
  val pipeReg = RegNext(io.memwb, init = 0.U.asTypeOf(new MemWb()))
  io.wbdec.data := Mux(pipeReg.isMem, pipeReg.memData, pipeReg.data)
  io.wbdec.regNr := pipeReg.regNr
  io.wbdec.valid := pipeReg.valid
}
