package wildcat.pipeline

import chisel3._
class Fetch(code: Array[Int]) extends Module {
  val io = IO(new FetchIO())

  val imem = VecInit(code.map(_.S(32.W).asUInt))
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
