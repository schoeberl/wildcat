package wildcat.pipeline

import chisel3._
import wildcat.isasim.Util

/*
 * Copyright (c) 2015-2022, DTU
 * Simplified BSD License
 */

/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * Wildcat will become a simple RISC-V pipeline.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class Wildcat(args: Array[String]) extends Module {

  val (code, start) = Util.getCode(args)

  // Four pipeline stages
  val fetch = Module(new Fetch(code))
  val decode = Module(new Decode())
  val execute = Module(new Execute())
  val writeback = Module(new WriteBack())

  fetch.io.fedec <> decode.io.fedec
  decode.io.decex <> execute.io.decex
  execute.io.exwb <> writeback.io.exwb
  decode.io.wbdec <> writeback.io.wbdec
}
