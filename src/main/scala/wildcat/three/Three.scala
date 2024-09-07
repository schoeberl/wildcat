package wildcat.three

import chisel3._
import wildcat.common._

/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is a three stage pipeline.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class Three() extends Wildcat() {

  // TODO: is this really needed?
  // The ROM has a register that is reset to 0, therefore clock cycle 1 is the first instruction.
  // Probably needed if we want to start from a different address.
  val pcReg = RegInit(-4.S(32.W).asUInt)
  val pcNext = pcReg + 4.U
  pcReg := pcNext

  val stall = false.B

  val instr = io.imem.data
  // pipe registers
  val instrReg = RegInit(0x00000033.U) // nop on reset
  when (!stall) {
    instrReg := instr
  }

  // dummy connections for now
  io.dmem.rdAddress := 0.U
  io.dmem.wrAddress := 0.U
  io.dmem.wrData := 0.U
  io.dmem.wrEnable := 0.U
  io.imem.address := pcNext
}