package wildcat.pipeline

import chisel3._
class Fetch(code: Array[Int]) extends Module {
  val io = IO(new FetchIO())

  val imem = VecInit(code.map(_.S(32.W).asUInt))
  val pcReg = RegInit(0.U(30.W))

  pcReg := pcReg + 1.U // counting in words

  io.fedec.pc := pcReg ## 0.U(2.W)
  io.fedec.instr := imem(pcReg)
}
