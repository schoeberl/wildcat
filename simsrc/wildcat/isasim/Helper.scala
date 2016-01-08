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

object Helper {

  def genAlu(opc: Int, funct: Int, rs1: Int, rs2: Int, imm: Int, rd: Int) = {
    ((rs1 & 0x1f) << 15) | ((rs2 & 0x1f) << 20) | ((imm & 0x0fff) << 20) |
    ((funct & 0x07) << 12) | ((rd & 0x1f) << 7) | (opc & 0x7f)
  } 
}
