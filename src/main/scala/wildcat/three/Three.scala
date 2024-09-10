package wildcat.three

import chisel3._
import wildcat.common._
import wildcat.common.Functions._
import wildcat.Opcode._

/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is a three stage pipeline.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class Three() extends Wildcat() {

  // some forward declarations
  val stall = WireDefault(false.B)
  val res = Wire(UInt(32.W))
  val dest = Wire(UInt(5.W))
  val wrEna = WireDefault(true.B)

  val doBranch = WireDefault(false.B)
  val branchTarget = WireDefault(0.U)

  // Let's do following pipeline stages:
  // 0. PC generation
  // 1. Fetch
  // 2. Decode, register read
  // 3. Execute

  // The ROM has a register that is reset to 0, therefore clock cycle 1 is the first instruction.
  // Needed if we want to start from a different address.
  // PC generation
  val pcReg = RegInit(-4.S(32.W).asUInt)

  val pcNext = Mux(doBranch, branchTarget, pcReg + 4.U)
  pcReg := pcNext
  io.imem.address := pcNext

  // Fetch
  val instr = io.imem.data

  // Decode and register read
  val pcRegReg = RegNext(pcReg)
  val instrReg = RegInit(0x00000033.U) // nop on reset
  instrReg := Mux(doBranch, 0x00000033.U, instr)
  val rs1 = instr(19, 15)
  val rs2 = instr(24, 20)
  val rd = instr(11, 7)
  val (rs1Val, rs2Val) = registerFile(rs1, rs2, dest, res, wrEna, false)

  val (instrType, isImm) = getInstrType(instrReg)
  val imm = getImm(instrReg, instrType)
  val aluOp = getAluOp(instrReg)
  val val2 = Mux(isImm, imm.asUInt, rs2Val)

  val decEx = Wire(new Bundle() {
    val valid = Bool()
    val pc = UInt(32.W)
    val aluOp = UInt(4.W)
    val rs1 = UInt(5.W)
    val rs2 = UInt(5.W)
    val rd = UInt(5.W)
    val rs1Val = UInt(32.W)
    val val2 = UInt(32.W)
    val branchInstr = Bool()
  })
  decEx.valid := !doBranch
  decEx.pc := pcRegReg
  decEx.aluOp := aluOp
  decEx.rs1 := instrReg(19, 15)
  decEx.rs2 := instrReg(24, 20)
  decEx.rd := instrReg(11, 7)
  decEx.rs1Val := rs1Val
  decEx.val2 := val2
  decEx.branchInstr := instrReg(6, 0) === Branch.U

  // Execute
  val decExReg = RegInit(0.U.asTypeOf(decEx))
  decExReg := decEx

  res := alu(decExReg.aluOp, decExReg.rs1Val, decExReg.val2)
  dest := decExReg.rd

  branchTarget := (decExReg.pc.asSInt + decExReg.val2.asSInt).asUInt
  doBranch := compare(instrReg(14, 12), rs1Val, rs2Val) && decExReg.branchInstr && decExReg.valid
  wrEna := decExReg.valid && !doBranch // and some more conditions

  // dummy connections for now
  io.dmem.rdAddress := 0.U
  io.dmem.wrAddress := 0.U
  io.dmem.wrData := RegNext(res) // to avoid optimizing everything away
  io.dmem.wrEnable := 0.U
}