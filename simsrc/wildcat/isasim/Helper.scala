/*
 * Helper and debug functions for the ISA simulator of RISC-V.
 * Not needed for the core simulator.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 */

package wildcat.isasim

object Helper {

  def genAddi(rs: Int, imm: Int, rd: Int) = {
    // TODO masking? Better do it... Or live with some fun
    (rs << 15) | (imm << 20) | (rd << 7) | 0x13
  } 
}
