/*
 * Copyright (c) 2015-2016, DTU
 * Simplified BSD License
 */

/*
 * This file is part of the RISC-V processor Wildcat.
 * 
 * Definitions of IO connections.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * 
 */

package wildcat.pipeline

import Chisel._
import Node._

class FeDec() extends Bundle() {
  val instr = Bits(width = 32)
  val pc = UInt(width = 32)
}

class DecEx() extends Bundle() {
  val instr = Bits(width = 32)
  val pc = UInt(width = 32)
}

class ExMem() extends Bundle() {
  val instr = Bits(width = 32)
  val pc = UInt(width = 32)
}
class MemWB() extends Bundle() {
  val instr = Bits(width = 32)
  val pc = UInt(width = 32)
}

class FetchIO extends Bundle() {
  val ena = Bool(INPUT)
  val fedec = new FeDec().asOutput
}

class DecodeIO extends Bundle() {
  val ena = Bool(INPUT)
  val fedec = new FeDec().asInput
  val decex = new DecEx().asOutput
}

class ExecuteIO extends Bundle() {
  val ena = Bool(INPUT)
  val decex = new DecEx().asInput
  val exmem = new ExMem().asOutput
}

class MemoryIO extends Bundle() {
  val ena = Bool(INPUT)
  val exmem = new ExMem().asInput
  val memwb = new MemWB().asOutput
}

class WriteBackIO extends Bundle() {
  val ena = Bool(INPUT)
  val memwb = new MemWB().asInput
}