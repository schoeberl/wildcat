package wildcat.pipeline

import chisel3._
import wildcat.pipeline.Common._

class Memory extends Module {
  val io = IO(new MemeoryIO)

  io.exmem <> io.memwb
}
