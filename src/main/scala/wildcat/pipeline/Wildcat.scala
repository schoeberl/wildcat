package wildcat.pipeline

import chisel3._
import wildcat.Util

/*
 * Copyright (c) 2015-2023, DTU
 * Simplified BSD License
 */

/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is the common top[-level for different implementations.
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