package wildcat.explore

import chisel3._
import wildcat.pipeline.Functions._

class ForwardingSpeed extends Module {
  val io = IO(new Bundle {
    val instr = Input(UInt(32.W))
    val wbDest = Input(UInt(5.W))
    val wbData = Input(UInt(32.W))
    val wrEna = Input(Bool())
  })
  val out = IO(new Bundle{
    val outData = Output(UInt(32.W))
  })

  val instrReg = RegNext(io.instr, 0x00000033.U) // nop on reset

  val rs1 = io.instr(19, 15)
  val rs2 = io.instr(24, 20)
  val rd = io.instr(11, 7)
  val (rs1Val, rs2Val, debugRegs) = registerFile(rs1, rs2, RegNext(io.wbDest), RegNext(io.wbData), RegNext(io.wrEna), true)



  out.outData := RegNext(rs1Val + rs2Val)
}

object ForwardingSpeed extends App {
  emitVerilog(new ForwardingSpeed, Array("--target-dir", "generated"))
}
