package wildcat.pipeline

import chisel3._
class Fetch(code: Array[Long]) extends Module {
  val io = IO(new FetchIO())

  val imem = VecInit(code.map(_.U(32.W)))
  val pcReg = RegInit(0.U(30.W))

  pcReg := pcReg + 1.U // counting in words

  io.fedec.pc := pcReg ## 0.U(2.W)
  io.fedec.instr := imem(pcReg)
}
