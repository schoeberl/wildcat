package wildcat

import chisel3._
import chisel3.util.experimental.BoringUtils
import wildcat.pipeline._


/*
 * Top-level for testing and  verification
 *
 */
class WildcatTestTop(args: Array[String]) extends Module {

  val io = IO(new Bundle {
    val regFile = Output(Vec(32,UInt(32.W)))
    val stop = Output(Bool())
  })
  val cpuTop = Module(new WildcatTop(args))

  io.regFile := DontCare
  BoringUtils.bore(cpuTop.cpu.debugRegs, Seq(io.regFile))
  io.stop := DontCare
  BoringUtils.bore(cpuTop.cpu.stop, Seq(io.stop))
}