package wildcat.single

import chisel3._
import wildcat.Opcode.Branch
import wildcat.Util
import wildcat.pipeline.Functions._

class SingleCycleTop(file: String) extends Module {
  val io = IO(new Bundle {
    val led = Output(UInt(8.W))
  })

  val cpu = Module(new SingleCycle(file))
  io.led := cpu.io.regs(4)(7, 0)
}

object SingleCycleTop extends App {
  emitVerilog(new SingleCycleTop(args(0)), Array("--target-dir", "generated"))
}