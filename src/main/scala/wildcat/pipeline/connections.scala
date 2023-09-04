package wildcat.pipeline

import chisel3._

class FeDec extends Bundle {
  val instr = UInt(32.W)
  val pc = UInt(32.W)
}

class DecEx extends Bundle {
  // val instr = UInt(32.W)
  val pc = UInt(32.W)
  val rs1 = UInt(5.W)
  val rs2 = UInt(5.W)
  val rd = UInt(5.W)
  val rs1Val = UInt(32.W)
  val rs2Val = UInt(32.W)
  val imm = SInt(32.W)
  val aluOp = UInt()
  val isImm = Bool()
}

class ExMem extends Bundle {

  val data = UInt(32.W)
  val addr =UInt(32.W)
  val ena = Bool()
  val regNr = UInt(5.W)
  val valid = Bool()
}

class MemWb extends Bundle {
  val data = UInt(32.W)
  val regNr = UInt(5.W)
  val valid = Bool()
  val memData = UInt(32.W)
  val isMem = Bool()
}
class WbDec extends Bundle {

  val data = UInt(32.W)
  val regNr = UInt(5.W)
  val valid = Bool()
}