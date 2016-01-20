/*
 * Copyright (c) 2015-2016, DTU
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

package wildcat.pipeline

import Chisel._
import Node._

/**
 * Fetch stage.
 */

class Execute extends Module {
  val io = new ExecuteIO()

  val pipeReg = Reg(next = io.decex)

  // simply forward
  io.exmem.pc <> pipeReg.pc
}