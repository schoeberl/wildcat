package wildcat.explore

import chisel3._
import wildcat.pipeline.Functions._


class AluSpeed extends Module {
  val io = IO(new Bundle {
    val op = Input(UInt(4.W))
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
  })
  val out = IO(new Bundle{
    val outData = Output(UInt(32.W))
  })

  val res = alu(RegNext(io.op), RegNext(io.a), RegNext(io.b))
  out.outData := RegNext(res)

}

object AluSpeed extends App {
  emitVerilog(new AluSpeed, Array("--target-dir", "generated"))
}
