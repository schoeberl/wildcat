package wildcat.single

import chisel3._
import wildcat.Opcode.Branch
import wildcat.Util
import wildcat.pipeline.Functions._

class SingleCycle(file: String) extends Module {
  val io = IO(new Bundle {
    val regs = Output(Vec(32, UInt(32.W)))
    val stop = Output(Bool())
  })

  val (code, start) = Util.getCode(file)
  val rom = VecInit(code.toIndexedSeq.map(_.S(32.W).asUInt))

  val pc = RegInit(start.U(32.W))
  val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val mem = Mem(16, UInt(32.W))

  io.regs := regs
  io.stop := false.B

  val doBranch = WireDefault(false.B)
  val branchTarget = WireDefault(0.U)

  val instruction = rom(pc(31, 2))
  pc := Mux(doBranch, branchTarget, pc + 4.U)

  val rs1 = instruction(19, 15)
  val rs2 = instruction(24, 20)
  val rd = instruction(11, 7)
  val rs1Val = regs(rs1)
  val rs2Val = regs(rs2)

  val decOut = decode(instruction)

  val val2 = Mux(decOut.isImm, decOut.imm.asUInt, rs2Val)
  val res = alu(decOut.aluOp, rs1Val, val2)
  when (decOut.isLui) {
    res := decOut.imm.asUInt
  }
  when (decOut.isAuiPc) {
    res := (pc.asSInt + decOut.imm).asUInt
  }
  when (decOut.isLoad) {
    res := mem(rs1Val + decOut.imm.asUInt)
  }
  when (decOut.isStore) {
    mem(rs1Val + decOut.imm.asUInt) := rs2Val
  }

  // Branching
  branchTarget := (pc.asSInt + decOut.imm).asUInt
  when (decOut.isJalr) {
    branchTarget := res
  }
  doBranch := ((compare(instruction(14, 12), rs1Val, rs2Val) && instruction(6, 0) === Branch.U) || decOut.isJal || decOut.isJalr)

  // write back
  when (decOut.rfWrite && rd =/= 0.U) {
    regs(rd) := res
  }

  io.stop := decOut.isECall
}