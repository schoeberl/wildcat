package wildcat.pipeline

import chisel3._

/**
 * Does this stage do anything?
 */
class WriteBack extends Module {
  val io = IO(new WriteBackIO)
  io.wbdec <> io.exwb
}
