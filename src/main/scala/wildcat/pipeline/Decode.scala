package wildcat.pipeline

import chisel3._
import wildcat.common.Functions._

class Decode extends Module {
  val io = IO(new Bundle{
    val fedec = Input(new FeDec())
    val decex = Output(new DecEx())
    val wbdec = Input(new WbDec())
    val stall = Input(Bool())
  })


  val instr = io.fedec.instr
  // pipe registers
  val instrReg = RegInit(0x00000033.U) // nop on reset
  val pcReg = RegInit(0.U)
  when (!io.stall) {
    instrReg := instr
    pcReg := io.fedec.pc
  }

  // Register file
  val regMem = SyncReadMem(32, UInt(32.W), SyncReadMem.WriteFirst)

  val rs1 = instr(19, 15)
  val rs2 = instr(24, 20)
  val rd = instr(11, 7)

  /*
  val rs1Val = Mux(rs1 =/= 0.U, regMem.read(rs1), 0.U)
  val rs2Val = Mux(rs2 =/= 0.U, regMem.read(rs2), 0.U)
  when(io.wbdec.valid) {
    regMem.write(io.wbdec.regNr, io.wbdec.data)
  }
  */

  // The register version needs an input pipe register
  val rs1Reg = RegNext(rs1)
  val rs2Reg = RegNext(rs2)
  val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val rs1Val = Mux(rs1Reg =/= 0.U, regs(rs1Reg), 0.U)
  val rs2Val = Mux(rs2Reg =/= 0.U, regs(rs2Reg), 0.U)
  when(io.wbdec.valid) {
    regs(io.wbdec.regNr) := io.wbdec.data
  }
  for (i <- 0 until 5) {
    // printf("reg %d: %x ", i.U, regs(i))
  }
  printf("\n")

  val (instrType, isImm) = getInstrType(instrReg)
  val imm = getImm(instrReg, instrType)
  val aluOp = getAluOp(instrReg)

  // Address calculation for load/store (if 3 or 4 stages pipeline)
  io.decex.pc := pcReg
  io.decex.aluOp := aluOp
  io.decex.rs1 := instrReg(19, 15)
  io.decex.rs2 := instrReg(24, 20)
  io.decex.rd := instrReg(11, 7)
  io.decex.rs1Val := rs1Val
  io.decex.rs2Val := rs2Val
  io.decex.imm := imm
  io.decex.isImm := isImm
  //printf("%x: instruction: %x rs1: %x rs2: %x rd: %x imm: %x\n", io.decex.pc, io.decex.aluOp, io.decex.rs1, io.decex.rs2, io.decex.rd, io.decex.imm)
}
