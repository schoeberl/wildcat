package wildcat.pipeline

import chisel3._
import wildcat.pipeline.Common._

class Execute extends Module {
  val io = IO(new ExecuteIO)

  val res = alu(io.decex.aluOp, io.decex.rs1, io.decex.rs2)
  io.exwb.valid := false.B
  io.exwb.regNr := 0.U
  io.exwb.data := 0.U
}
