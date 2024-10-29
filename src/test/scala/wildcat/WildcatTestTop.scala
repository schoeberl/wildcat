package wildcat

import chisel3._
import chisel3.util.experimental.BoringUtils
import wildcat.pipeline._


/*
 * Top-level for testing and verification
 *
 */
class WildcatTestTop(file: String) extends Module {

  val io = IO(new Bundle {
    val regFile = Output(Vec(32,UInt(32.W)))
    val led = Output(UInt(8.W))
    val tx = Output(UInt(1.W))
    val rx = Input(UInt(1.W))
    val stop = Output(Bool())
  })
  val cpuTop = Module(new WildcatTop(file))

  io.regFile := DontCare
  BoringUtils.bore(cpuTop.cpu.debugRegs, Seq(io.regFile))
  io.stop := DontCare
  BoringUtils.bore(cpuTop.cpu.stop, Seq(io.stop))
  io.led := DontCare
  BoringUtils.bore(cpuTop.ledReg, Seq(io.led))

  cpuTop.io.rx := io.rx
  io.tx := cpuTop.io.tx
}