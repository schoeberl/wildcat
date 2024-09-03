package wildcat.common

import chisel3._
import wildcat.Util


/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is the common top-level for different implementations.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class Wildcat() extends Module {
  val io = IO(new Bundle {
    val imem = new InstrIO()
    val dmem = new MemIO()
  })
}