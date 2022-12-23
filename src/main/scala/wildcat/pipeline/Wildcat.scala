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

  val code = Array(0x00200093, //	addi x1 x0 2
    0x00300113, //	addi x2 x0 3
    0x002081b3 // add x3 x1 x2
  )

  val fetch = Module(new Fetch(code))
  val decode = Module(new Decode())

  fetch.io.fedec <> decode.io.fedec
}
