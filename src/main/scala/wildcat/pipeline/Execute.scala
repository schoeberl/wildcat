package wildcat.pipeline

import chisel3._
import wildcat.pipeline.Common._

class Execute extends Module {
  val io = IO(new ExecuteIO)

  val decReg = RegNext(io.decex)
  val val2 = Mux(decReg.isImm, decReg.imm.asUInt, decReg.rs2)
  val res = alu(decReg.aluOp, decReg.rs1, val2)
  io.exmem.valid := true.B
  io.exmem.regNr := decReg.rd
  io.exmem.data := res
}
