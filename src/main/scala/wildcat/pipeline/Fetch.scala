package wildcat.pipeline

import chisel3._
class Fetch(code: Array[Int]) extends Module {
  val io = IO(new Bundle {
    val fedec = Output(new FeDec())
    val stall = Input(Bool())
    val pcIn = Input(UInt(32.W))
    val loadPc = Input(Bool())
    val flash = Input(Bool())
  })

  val imem = VecInit(code.toIndexedSeq.map(_.S(32.W).asUInt))
  val pcReg = RegInit(0.U(32.W))

  when (!io.stall) {
    when (io.loadPc) {
      pcReg := io.pcIn
    } .otherwise {
      pcReg := pcReg + 4.U
    }
  }

  io.fedec.pc := pcReg
  io.fedec.instr := imem(pcReg(31, 2))
}
