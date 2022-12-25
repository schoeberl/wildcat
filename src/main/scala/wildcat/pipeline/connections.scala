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

class ExWb extends Bundle {

  val data = UInt(32.W)
  val regNr = UInt(5.W)
  val valid = Bool()
}

class WbDec extends Bundle {

  val data = UInt(32.W)
  val regNr = UInt(5.W)
  val valid = Bool()
}

class FetchIO extends Bundle {
  val fedec = Output(new FeDec())
}
class DecodeIO extends Bundle {
  val fedec = Input(new FeDec())
  val decex = Output(new DecEx())
  val wbdec = Input(new WbDec())
}

class ExecuteIO extends Bundle {
  val decex = Input(new DecEx())
  val exwb = Output(new ExWb())
}

class WriteBackIO extends Bundle {
  val exwb = Input(new ExWb())
  val wbdec = Output(new WbDec())
}