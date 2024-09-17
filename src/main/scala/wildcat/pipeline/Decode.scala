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

  val rs1 = instr(19, 15)
  val rs2 = instr(24, 20)
  val rd = instr(11, 7)
  val (rs1Val, rs2Val, debugReg) = registerFile(rs1, rs2, rd, io.wbdec.data, io.wbdec.valid)

  val decOut = decode(instrReg)

  // TODO: there is no decoding going on here
  // Address calculation for load/store (if 3 or 4 stages pipeline)
  io.decex.pc := pcReg
  io.decex.aluOp := decOut.aluOp
  io.decex.rs1 := instrReg(19, 15) // TODO: this duplication is not nice
  io.decex.rs2 := instrReg(24, 20)
  io.decex.rd := instrReg(11, 7)
  io.decex.rs1Val := rs1Val
  io.decex.rs2Val := rs2Val
  io.decex.imm := decOut.imm
  io.decex.isImm := decOut.isImm
  //printf("%x: instruction: %x rs1: %x rs2: %x rd: %x imm: %x\n", io.decex.pc, io.decex.aluOp, io.decex.rs1, io.decex.rs2, io.decex.rd, io.decex.imm)
}
