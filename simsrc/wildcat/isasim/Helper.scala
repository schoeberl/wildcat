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

import Opcode._
import AluFunct._
object Helper {

  def genAlu(opc: Int, funct: Int, rs1: Int, rs2: Int, imm: Int, rd: Int) = {
    ((rs1 & 0x1f) << 15) | ((rs2 & 0x1f) << 20) | ((imm & 0x0fff) << 20) |
      ((funct & 0x07) << 12) | ((rd & 0x1f) << 7) | (opc & 0x7f)
  }

  val code = Array(
    Helper.genAlu(AluImm, ADD_SUB, 0, 0, 0x0f, 0),
    Helper.genAlu(AluImm, ADD_SUB, 0, 0, 0x11, 1),
    Helper.genAlu(AluImm, ADD_SUB, 1, 0, 0x22, 2),
    Helper.genAlu(Alu, ADD_SUB, 1, 2, 0, 3))
}
