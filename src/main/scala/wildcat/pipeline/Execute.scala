package wildcat.pipeline

import chisel3._
import wildcat.pipeline.Common._

class Execute extends Module {
  val io = IO(new Bundle {
    val decex = Input(new DecEx())
    val exmem = Output(new ExMem())
    val stall = Input(Bool())
  })

  val decexReg = RegNext(io.decex)
  val val2 = Mux(decexReg.isImm, decexReg.imm.asUInt, decexReg.rs2Val)
  val res = alu(decexReg.aluOp, decexReg.rs1Val, val2)
  io.exmem.valid := true.B
  io.exmem.regNr := decexReg.rd
  io.exmem.data := res

  // dummy connections
  io.exmem.addr := 0.U
  io.exmem.ena := false.B
}
