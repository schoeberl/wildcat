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
 * 0. PC generation
 * 1. Fetch
 * 2. Decode, register read
 * 3. Execute, memory access
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



  // The ROM has a register that is reset to 0, therefore clock cycle 1 is the first instruction.
  // Needed if we want to start from a different address.
  // PC generation
//  val pcReg = RegInit(-4.S(32.W).asUInt)
  val pcReg = RegInit(0.S(32.W).asUInt) // keep it simpler for now for the waveform viewing

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
  val (rs1Val, rs2Val, debugRegs) = registerFile(rs1, rs2, dest, res, wrEna, false)

  val decOut = decode(instrReg)

  val decEx = Wire(new Bundle() {
    val decOut = new DecodedInstr()
    val valid = Bool()
    val pc = UInt(32.W)
    val rs1 = UInt(5.W)
    val rs2 = UInt(5.W)
    val rd = UInt(5.W)
    val rs1Val = UInt(32.W)
    val rs2Val = UInt(32.W)
    val func3 = UInt(3.W)
    val branchInstr = Bool()
  })
  decEx.decOut := decOut
  decEx.valid := !doBranch
  decEx.pc := pcRegReg
  decEx.rs1 := instrReg(19, 15)
  decEx.rs2 := instrReg(24, 20)
  decEx.rd := instrReg(11, 7)
  decEx.rs1Val := rs1Val
  decEx.rs2Val := rs2Val
  decEx.func3 := instrReg(14, 12)
  decEx.branchInstr := instrReg(6, 0) === Branch.U

  // Execute
  val decExReg = RegInit(0.U.asTypeOf(decEx))
  decExReg := decEx
  // Forwarding register
  val exFwd = new Bundle() {
    val valid = Bool()
    val dest = UInt(5.W)
    val res = UInt(32.W)
  }
  val exFwdReg = RegInit(0.U.asTypeOf(exFwd))

  // Forwarding
  val v1 = Mux(exFwdReg.valid && exFwdReg.dest === decExReg.rs1, exFwdReg.res, decExReg.rs1Val)
  val v2 = Mux(exFwdReg.valid && exFwdReg.dest === decExReg.rs2, exFwdReg.res, decExReg.rs2Val)

  val val2 = Mux(decExReg.decOut.isImm, decExReg.decOut.imm.asUInt, v2)
  res := alu(decExReg.decOut.aluOp, v1, val2)
  when (decExReg.decOut.isLui) {
    res := decExReg.decOut.imm.asUInt
  }
  when (decExReg.decOut.isAuiPc) {
    res := (decExReg.pc.asSInt + decExReg.decOut.imm).asUInt
  }

  dest := decExReg.rd

  branchTarget := (decExReg.pc.asSInt + decExReg.decOut.imm).asUInt
  doBranch := compare(decExReg.func3, v1, v2) && decExReg.branchInstr && decExReg.valid
  wrEna := decExReg.valid && decExReg.decOut.rfWrite && !doBranch

  // Forwarding register values
  exFwdReg.valid := wrEna && (dest =/= 0.U)
  exFwdReg.dest := dest
  exFwdReg.res := res

  // Just for testing
  val stop = decExReg.decOut.isECall

  // almost dummy connections for now
  io.dmem.rdAddress := 0.U
  io.dmem.wrAddress := 0.U
  io.dmem.wrData := decExReg.rs2Val
  io.dmem.wrEnable := Mux(decExReg.decOut.isStore, 15.U, 0.U)
}