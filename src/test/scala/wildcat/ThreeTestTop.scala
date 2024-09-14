package wildcat

import chisel3._
import chisel3.util.experimental.BoringUtils
import wildcat.three._


/*
 * Top-level for testing and  verification
 *
 */
class ThreeTestTop(args: Array[String]) extends Module {

  val io = IO(new Bundle {
    val regFile = Output(Vec(32,UInt(32.W)))
  })
  val three = Module(new ThreeTop(args))

  io.regFile := DontCare
  BoringUtils.bore(three.cpu.debugRegs, Seq(io.regFile))
}