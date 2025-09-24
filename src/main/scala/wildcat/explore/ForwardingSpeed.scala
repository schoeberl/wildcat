package wildcat.explore

import chisel3._
    import circt.stage.ChiselStage
    import circt.stage.FirtoolOption

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


  val rs1 = io.instr(19, 15)
  val rs2 = io.instr(24, 20)
  val rd = io.instr(11, 7)

  val (rs1Val, rs2Val, debugRegs) = registerFile(rs1, rs2, RegNext(io.wbDest), RegNext(io.wbData), RegNext(io.wrEna), true)

  val instrReg = RegNext(io.instr, 0x00000033.U) // nop on reset
  val dummyOp = instrReg(3, 0)
  val rs1Reg = instrReg(19, 15)
  val rs2Reg = instrReg(24, 20)
  val rdReg = instrReg(11, 7)

  class DecEx extends Bundle {
    val dummyOp = UInt(4.W)
    val rs1 = UInt(5.W)
    val rs2 = UInt(5.W)
    val rs1Val = UInt(32.W)
    val rs2Val = UInt(32.W)
    val rd = UInt(5.W)
  }

  class ExMem extends Bundle {
    val aluRes = UInt(32.W)
    val rd = UInt(5.W)
  }

  val decExReg = RegInit(0.U.asTypeOf(new DecEx))
  decExReg.dummyOp := dummyOp
  decExReg.rs1 := rs1Reg
  decExReg.rs2 := rs2Reg
  decExReg.rd := rdReg

  val exForwarding = false

  val res = Wire(UInt(32.W))
  val exMemReg = RegInit(0.U.asTypeOf(new ExMem))

  if (exForwarding) {
    decExReg.rs1Val := rs1Val
    decExReg.rs2Val := rs2Val

    res := alu(decExReg.dummyOp,
                Mux(decExReg.rs1 === exMemReg.rd, exMemReg.aluRes, decExReg.rs1Val),
                Mux(decExReg.rs2 === exMemReg.rd, exMemReg.aluRes, decExReg.rs2Val)
               )
  } else {

    decExReg.rs1Val := Mux(rs1Reg === decExReg.rd, res, rs1Val)
    decExReg.rs2Val := Mux(rs2Reg === decExReg.rd, res, rs2Val)

    res := alu(decExReg.dummyOp, decExReg.rs1Val, decExReg.rs2Val)
  }

  exMemReg.aluRes := res
  exMemReg.rd := decExReg.rd

  out.outData := exMemReg.aluRes
}

object ForwardingSpeed extends App {
  // emitVerilog(new ForwardingSpeed, Array("--target-dir", "generated")) 
  ChiselStage.emitSystemVerilogFile(new ForwardingSpeed, args = Array("--target-dir", "generated"), firtoolOpts = Array("--lowering-options=disallowLocalVariables,disallowPackedArrays"))
}
