package wildcat.pipeline

import chisel3._
class Fetch(code: Array[Int]) extends Module {
  val io = IO(new FetchIO())

  val imem = VecInit(code.map(_.U(32.W)))
  val pc = RegInit(0.U(30.W))

  pc := pc + 1.U // counting in words

  io.fedec.pc := pc ## 0.U(2.W)
  io.fedec.instr := imem(pc)
}
