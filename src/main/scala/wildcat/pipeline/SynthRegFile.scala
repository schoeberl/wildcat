package wildcat.pipeline

import chisel3._
import wildcat.pipeline.Functions.registerFile

// Only for synthesis tests leave all connections open
// Maybe add registers for fmax
class SynthRegFile() extends Module {

  val io = IO(new Bundle {
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val wbDest = Input(UInt(5.W))
    val wbData = Input(UInt(32.W))
    val wrEna = Input(Bool())
    val rs1Val = Output(UInt(32.W))
    val rs2Val = Output(UInt(32.W))
  })

  val (rs1Val, rs2Val, debugRegs) = registerFile(io.rs1, io.rs2, io.wbDest, io.wbData, io.wrEna, true)
  io.rs1Val := rs1Val
  io.rs2Val := rs2Val
}

object SynthRegFile extends App {
  emitVerilog(new SynthRegFile(), Array("--target-dir", "generated"))
}