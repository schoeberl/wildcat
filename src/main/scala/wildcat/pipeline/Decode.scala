package wildcat.pipeline

import chisel3._
class Decode extends Module {
  val io = IO(new DecodeIO)

  io.decex.pc := io.fedec.pc
  io.decex.instr := io.fedec.instr
}
