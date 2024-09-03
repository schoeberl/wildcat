package wildcat.three

import chisel3._
import wildcat.Util



/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is the common top-level for different implementations.
 * TODO: not now, lets just do some experiments
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class Wildcat(args: Array[String]) extends Module {
  val io = IO(new Bundle {
    val debug = Output(UInt(32.W))
  })

  val (code, start) = Util.getCode(args)
}