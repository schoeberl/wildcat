/*
 * Copyright (c) 2016, DTU
 * Simplified BSD License
 */

/*
 * Helper and debug functions for the ISA simulator of RISC-V.
 * Not needed for the core simulator.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * 
 */

package wildcat.isasim

import wildcat.Opcode._
import wildcat.AluFunct3._
object Helper {

  def genAlu(opc: Int, funct: Int, rs1: Int, rs2: Int, imm: Int, rd: Int) = {
    ((rs1 & 0x1f) << 15) | ((rs2 & 0x1f) << 20) | ((imm & 0x0fff) << 20) |
      ((funct & 0x07) << 12) | ((rd & 0x1f) << 7) | (opc & 0x7f)
  }

  val code = Array(
    Helper.genAlu(AluImm, F3_ADD_SUB, 0, 0, 0x0f, 0),
    Helper.genAlu(AluImm, F3_ADD_SUB, 0, 0, 0x11, 1),
    Helper.genAlu(AluImm, F3_ADD_SUB, 1, 0, 0x22, 2),
    Helper.genAlu(Alu, F3_ADD_SUB, 1, 2, 0, 3))

  // This would be immediate generation more for a programming language than
  // for hardware where shift has no cost, but multiplexing.
  //
  //    // immediate is more tricky - probably the main overhead in simulation
  //    // maybe compute it within the function and only when used - do benchmark
  //    // this first, before obscuring readability
  //    val immi = (instr & 0xfff00000) >> 20
  //    val imms = ((instr & 0xfe00000) >> (25 - 5)) | ((opcode & 0x0f80) >> 7)
  //    val immu = (instr & 0xfffff000) >>> 12
  //    val immb = (instr & 0x80000000) >> 19 | (instr & 0x0080) << 4 |
  //      (instr & 0x7e000000) >>> 20 | (instr & 0x0f00) >>> 7
  //    val boff = immb >> 2 // now in words
  //    // TODO: there is one additional version of immediate
}
