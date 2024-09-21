package wildcat.explore

import chisel3._
class MemSpeed extends Module {
  val io = IO(new Bundle {
    val rdAddr = Input(UInt(32.W))
    val rdData = Output(UInt(32.W))
    val wrAddr = Input(UInt(32.W))
    val wrData = Input(UInt(32.W))
    val wrEna = Input(Bool())
  })

  val mem = SyncReadMem(1024, UInt(32.W), SyncReadMem.WriteFirst)

  val rda = io.rdAddr
  val wra = io.wrAddr
  val wrd = io.wrData
  val ena = io.wrEna

  val rdd = mem.read(rda)

  when(ena) {
    mem.write(wra, wrd)
  }

  io.rdData := RegNext(rdd)
}

object MemSpeed extends App {
  emitVerilog(new MemSpeed, Array("--target-dir", "generated"))
}
