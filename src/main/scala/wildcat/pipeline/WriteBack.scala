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
  // io.wbdec <> io.memwb

  // dummy connection, Mux is missing
  io.wbdec.data := RegNext(io.memwb.data)
  io.wbdec.regNr := RegNext(io.memwb.regNr)
  io.wbdec.valid := RegNext(io.memwb.valid)
}
