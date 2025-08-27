package wildcat.pipeline

import chisel3._
import chisel3.util._
import soc._

/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is the common top-level for different implementations.
 * Interface is to instruction memory and data memory.
 * All SPMs, caches, and IOs shall be in a SoC top level
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
abstract class Wildcat() extends Module {
  val io = IO(new Bundle {
    val imem = Flipped(PipeCon(32))
    val dmem = Flipped(PipeCon(32))
  })
}