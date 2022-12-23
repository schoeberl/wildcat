package wildcat.pipeline

import chisel3._

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
class Wildcat extends Module {

  val fetch = Module(new Fetch())
  val decode = Module(new Decode())

  fetch.io.fedec <> decode.io.fedec
}
