package wildcat.pipeline

import chisel3._
class Fetch(code: Array[Int]) extends Module {
  val io = IO(new FetchIO())

  val imem = VecInit(code.map(_.S(32.W).asUInt))
  val pcReg = RegInit(0.U(32.W))

  pcReg := pcReg + 4.U

  io.fedec.pc := RegNext(pcReg, 0.U)
  io.fedec.instr := RegNext(imem(pcReg), 0x00000033.U) // nop on reset
}
