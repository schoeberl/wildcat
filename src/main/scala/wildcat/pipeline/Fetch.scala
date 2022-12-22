package wildcat.pipeline

import chisel3._
class Fetch extends Module {
  val io = IO(new FetchIO())

  io.fedec.pc := 0.U
  io.fedec.instr := 0.U
}
