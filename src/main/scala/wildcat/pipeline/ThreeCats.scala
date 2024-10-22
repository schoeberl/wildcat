package wildcat.pipeline

import chisel3._
import chisel3.util._
import wildcat.Opcode._
import wildcat.LoadStoreFunct._
import wildcat.pipeline.Functions._

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
class ThreeCats() extends Wildcat() {

  // some forward declarations
  val stall = WireDefault(false.B)
  val wbData = Wire(UInt(32.W))
  val wbDest = Wire(UInt(5.W))
  val wrEna = WireDefault(true.B)

  val doBranch = WireDefault(false.B)
  val branchTarget = WireDefault(0.U)


  // PC generation
  // The ROM has a register that is reset to 0, therefore clock cycle 1 is the first instruction.
  // Needed if we want to start from a different address.
  // val pcReg = RegInit(-4.S(32.W).asUInt)
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
  val (rs1Val, rs2Val, debugRegs) = registerFile(rs1, rs2, wbDest, wbData, wrEna, true)

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

  // Execute
  val decExReg = RegInit(0.U.asTypeOf(decEx))
  decExReg := decEx

  // Forwarding register
  val exFwd = new Bundle() {
    val valid = Bool()
    val wbDest = UInt(5.W)
    val wbData = UInt(32.W)
  }
  val exFwdReg = RegInit(0.U.asTypeOf(exFwd))

  // Forwarding to memory
  val address = Mux(wrEna && (wbDest =/= 0.U) && wbDest === decEx.rs1, wbData, rs1Val)
  val data = Mux(wrEna && (wbDest =/= 0.U) && wbDest === decEx.rs2, wbData, rs2Val)

  val memAddress = (address.asSInt + decOut.imm).asUInt


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
  when(decExReg.decOut.isLoad) {
    res := io.dmem.rdData
    switch(decExReg.func3) {
      is(LB.U) {
        switch(RegNext(memAddress(1, 0))) {
          is(0.U) {
            res := Fill(24, io.dmem.rdData(7)) ## io.dmem.rdData(7, 0)
          }
          is(1.U) {
            res := Fill(24, io.dmem.rdData(15)) ## io.dmem.rdData(15, 8)
          }
          is(2.U) {
            res := Fill(24, io.dmem.rdData(23)) ## io.dmem.rdData(23, 16)

          }
          is(3.U) {
            res := Fill(24, io.dmem.rdData(31)) ## io.dmem.rdData(31, 24)
          }
        }
      }
      is(LH.U) {
        switch(RegNext(memAddress(1, 0))) {
          is(0.U) {
            res := Fill(16, io.dmem.rdData(15)) ## io.dmem.rdData(15, 0)
          }
          is(2.U) {
            res := Fill(16, io.dmem.rdData(31)) ## io.dmem.rdData(31, 16)
          }
        }
      }
      is(LBU.U) {
        switch(RegNext(memAddress(1, 0))) {
          is(0.U) {
            res := io.dmem.rdData(7, 0)
          }
          is(1.U) {
            res := io.dmem.rdData(15, 8)
          }
          is(2.U) {
            res := io.dmem.rdData(23, 16)
          }
          is(3.U) {
            res := io.dmem.rdData(31, 24)
          }
        }
      }
      is(LHU.U) {
        switch(RegNext(memAddress(1, 0))) {
          is(0.U) {
            res := io.dmem.rdData(15, 0)
          }
          is(2.U) {
            res := io.dmem.rdData(31, 16)
          }
        }
      }
    }
  }

  wbDest := decExReg.rd
  wbData := res
  when(decExReg.decOut.isJal || decExReg.decOut.isJalr) {
    wbData := decExReg.pc + 4.U
  }
  // Branching
  branchTarget := (decExReg.pc.asSInt + decExReg.decOut.imm).asUInt
  when(decExReg.decOut.isJalr) {
    branchTarget := res
  }
  doBranch := ((compare(decExReg.func3, v1, v2) && decExReg.decOut.isBranch) || decExReg.decOut.isJal || decExReg.decOut.isJalr) && decExReg.valid
  wrEna := decExReg.valid && decExReg.decOut.rfWrite

  // Memory access
  io.dmem.rdAddress := memAddress
  io.dmem.wrAddress := memAddress
  io.dmem.wrData := data
  io.dmem.wrEnable := VecInit(Seq.fill(4)(false.B))
  when(decOut.isStore) {
    switch(decEx.func3) {
      is(SB.U) {
        io.dmem.wrData := data(7, 0) ## data(7, 0) ## data(7, 0) ## data(7, 0)
        io.dmem.wrEnable(memAddress(1,0)) := true.B
      }
      is(SH.U) {
        io.dmem.wrData := data(15, 0) ## data(15, 0)
        switch(memAddress(1, 0)) {
          is(0.U) {
            io.dmem.wrEnable(0) := true.B
            io.dmem.wrEnable(1) := true.B
          }
          is(2.U) {
            io.dmem.wrEnable(2) := true.B
            io.dmem.wrEnable(3) := true.B
          }
        }
      }
      is(SW.U) {
        io.dmem.wrEnable := VecInit(Seq.fill(4)(true.B))
      }
    }
  }

  // Forwarding register values to ALU
  exFwdReg.valid := wrEna && (wbDest =/= 0.U)
  exFwdReg.wbDest := wbDest
  exFwdReg.wbData := wbData

  // Just to exit tests
  val stop = decExReg.decOut.isECall
}