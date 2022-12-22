package wildcat.pipeline

import chisel3._

class FeDec extends Bundle {
  val instr = UInt(32.W)
  val pc = UInt(32.W)
}

class DecEx extends Bundle {
  val instr = UInt(32.W)
  val pc = UInt(32.W)
}

class FetchIO extends Bundle {
  val fedec = Output(new FeDec())
}
class DecodeIO extends Bundle {
  val fedec = Input(new FeDec())
  val decex = Output(new DecEx())

}
/*
class FeDec() extends Bundle() {
  val instr = Bits(width = 32)
  val pc = UInt(32.w)
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


 */