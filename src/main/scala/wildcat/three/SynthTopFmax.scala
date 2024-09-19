package wildcat.three

import chisel3._
import wildcat.Util
import wildcat.common._

// Only for synthesis tests leave all connections open
// Maybe add registers for fmax
class SynthTopFmax(args: Array[String]) extends Module {

  val io = IO(new Bundle {
    val imem = new InstrIO()
    val dmem = new MemIO()
  })

  val cpu = Module(new Three())
  cpu.io.imem <> io.imem
  cpu.io.dmem <> io.dmem
}

object SynthTopFmax extends App {
  emitVerilog(new SynthTopFmax(Array.empty[String]), Array("--target-dir", "generated"))
}