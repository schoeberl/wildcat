package wildcat.pipeline

import chisel3._

class LittleCat extends Module {
  val io = IO(new Bundle {
    val out = Output(UInt(2.W))
    val in = Input(UInt(2.W))
    val rx = Input(UInt(1.W))
    val tx = Output(UInt(1.W))
    val rxConf = Input(UInt(1.W))
    val txConf = Output(UInt(1.W))
  })

  // Just a pass-through for testing the interface
  io.out := RegNext(RegNext(io.in))
  io.tx := RegNext(RegNext(io.rx))
  io.txConf := RegNext(RegNext(io.rxConf))
}

object LittleCat extends App {
  emitVerilog(new LittleCat)
}
