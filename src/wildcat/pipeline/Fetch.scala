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

class Fetch extends Module {
  val io = new FetchIO()

  val pcReg = Reg(init = UInt(0, 32))

  pcReg := pcReg + UInt(4)

  io.fedec.pc := pcReg
}