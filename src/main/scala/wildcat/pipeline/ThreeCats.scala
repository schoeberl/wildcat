package wildcat.pipeline

import chisel3._
import wildcat.pipeline.Functions._

/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is a three-stage pipeline.
 *
 * 0. PC generation
 * 1. Fetch
 * 2. Decode, register read, memory address computation, and write
 * 3. Execute, memory read
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class ThreeCats() extends Wildcat() {

  // some forward declarations
  val stall = WireDefault(false.B)
  val wbData = Wire(UInt(32.W))
  val wbDest = Wire(UInt(5.W))
  val wrEna = WireDefault(true.B)

  val doBranch = WireDefault(false.B)
  val branchTarget = WireDefault(0.U)

  // Forwarding data and register
  val exFwd = new Bundle() {
    val valid = Bool()
    val wbDest = UInt(5.W)
    val wbData = UInt(32.W)
  }
  val exFwdReg = RegInit(0.U.asTypeOf(exFwd))

  // PC generation, first (invalid) instruction will stall, so init with 0 is OK
  val pcReg = RegInit(0.U(32.W))
  val pcNext = WireDefault(Mux(doBranch, branchTarget, pcReg + 4.U))
  pcReg := pcNext
  io.imem.address := pcNext

  // Fetch
  val instr = WireDefault(io.imem.data)
  when (io.imem.stall) {
    instr := 0x00000033.U
    pcNext := pcReg
  }

  // Decode, register read, and memory access
  val pcRegReg = RegNext(pcReg)
  val instrReg = RegInit(0x00000033.U) // nop on reset
  instrReg := Mux(doBranch, 0x00000033.U, instr)
  val rs1 = instr(19, 15)
  val rs2 = instr(24, 20)
  val rd = instr(11, 7)
  val (rs1Val, rs2Val, debugRegs) = registerFile(rs1, rs2, wbDest, wbData, wrEna, true)

  val csr = Module(new Csr())
  csr.io.address := instrReg(31, 20)
  val csrVal = csr.io.data

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
    val csrVal = UInt(32.W)
    val func3 = UInt(3.W)
    val memLow = UInt(2.W)
  })
  decEx.decOut := decOut
  decEx.valid := !doBranch
  decEx.pc := pcRegReg
  decEx.rs1 := instrReg(19, 15)
  decEx.rs2 := instrReg(24, 20)
  decEx.rd := instrReg(11, 7)
  decEx.rs1Val := rs1Val
  decEx.rs2Val := rs2Val
  decEx.csrVal := csrVal
  decEx.func3 := instrReg(14, 12)

  // Forwarding to memory
  val address = Mux(wrEna && (wbDest =/= 0.U) && wbDest === decEx.rs1, wbData, rs1Val)
  val data = Mux(wrEna && (wbDest =/= 0.U) && wbDest === decEx.rs2, wbData, rs2Val)

  val memAddress = (address.asSInt + decOut.imm).asUInt
  decEx.memLow := memAddress(1, 0)

  io.dmem.rdAddress := memAddress
  io.dmem.rdEnable := false.B
  io.dmem.wrAddress := memAddress
  io.dmem.wrData := data
  io.dmem.wrEnable := VecInit(Seq.fill(4)(false.B))
  when(decOut.isLoad && !doBranch) {
    io.dmem.rdEnable := true.B
  }
  when(decOut.isStore && !doBranch) {
    val (wrd, wre) = getWriteData(data, decEx.func3, memAddress(1, 0))
    io.dmem.wrData := wrd
    io.dmem.wrEnable := wre
  }


  // Execute
  val decExReg = RegInit(0.U.asTypeOf(decEx))
  decExReg := decEx

  // Forwarding
  val v1 = Mux(exFwdReg.valid && exFwdReg.wbDest === decExReg.rs1, exFwdReg.wbData, decExReg.rs1Val)
  val v2 = Mux(exFwdReg.valid && exFwdReg.wbDest === decExReg.rs2, exFwdReg.wbData, decExReg.rs2Val)

  val res = Wire(UInt(32.W))
  val val2 = Mux(decExReg.decOut.isImm, decExReg.decOut.imm.asUInt, v2)
  res := alu(decExReg.decOut.aluOp, v1, val2)
  when(decExReg.decOut.isLui) {
    res := decExReg.decOut.imm.asUInt
  }
  when(decExReg.decOut.isAuiPc) {
    res := (decExReg.pc.asSInt + decExReg.decOut.imm).asUInt
  }
  when(decExReg.decOut.isCssrw) {
    res := decExReg.csrVal
  }

  wbDest := decExReg.rd
  wbData := res
  when(decExReg.decOut.isJal || decExReg.decOut.isJalr) {
    wbData := decExReg.pc + 4.U
  }
  // Branching and jumping
  branchTarget := (decExReg.pc.asSInt + decExReg.decOut.imm).asUInt
  when(decExReg.decOut.isJalr) {
    branchTarget := res
  }
  doBranch := ((compare(decExReg.func3, v1, v2) && decExReg.decOut.isBranch) || decExReg.decOut.isJal || decExReg.decOut.isJalr) && decExReg.valid
  wrEna := decExReg.valid && decExReg.decOut.rfWrite

  // Memory read access
  when(decExReg.decOut.isLoad && !doBranch) {
    res := selectLoadData(io.dmem.rdData, decExReg.func3, decExReg.memLow)
  }


  // Forwarding register values to ALU
  exFwdReg.valid := wrEna && (wbDest =/= 0.U)
  exFwdReg.wbDest := wbDest
  exFwdReg.wbData := wbData

  // Just to exit tests
  val stop = decExReg.decOut.isECall
}