package wildcat.pipeline

import chisel3._

class Execute extends Module {
  val io = IO(new ExecuteIO)

  io.exwb.valid := false.B
  io.exwb.regNr := 0.U
  io.exwb.data := 0.U
}
