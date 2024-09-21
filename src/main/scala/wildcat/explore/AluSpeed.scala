package wildcat.explore

import chisel3._
import wildcat.pipeline.Functions._

class InData extends Bundle {
  val valid = Bool()
  val wbDest = UInt(5.W)
  val rs1 = UInt(5.W)
  val rs1Val = UInt(32.W)
  val rs2 = UInt(5.W)
  val rs2Val = UInt(32.W)
  val wbData = UInt(32.W)
  val isImm = Bool()
  val imm = SInt(32.W)
  val aluOp = UInt(4.W)
  val isLui = Bool()
  val isAuiPc = Bool()
  val isLoad = Bool()
  val isJal = Bool()
  val isJalr = Bool()
  val rdData = UInt(32.W)
  val pc = UInt(32.W)
}

class AluSpeed extends Module {
  val io = IO(new Bundle {
    val in = Input(new InData())
  })
  val out = IO(new Bundle{
    val outData = Output(UInt(32.W))
  })

  val inReg = RegNext(io.in)

  val v1 = Mux(inReg.valid && inReg.wbDest === inReg.rs1, inReg.wbData, inReg.rs1Val)
  val v2 = Mux(inReg.valid && inReg.wbDest === inReg.rs2, inReg.wbData, inReg.rs2Val)

  val res = Wire(UInt(32.W))
  val val2 = Mux(inReg.isImm, inReg.imm.asUInt, v2)
  res := alu(inReg.aluOp, v1, val2)
  when (inReg.isLui) {
    res := inReg.imm.asUInt
  }
  when (inReg.isAuiPc) {
    res := (inReg.pc.asSInt + inReg.imm).asUInt
  }
  when (inReg.isLoad) {
    res := inReg.rdData
  }

  val wbData = res
  when (inReg.isJal || inReg.isJalr) {
    wbData := inReg.pc + 4.U
  }

  out.outData := RegNext(wbData)
}

object AluSpeed extends App {
  emitVerilog(new AluSpeed, Array("--target-dir", "generated"))
}
