package wildcat

import chisel3._
import chisel3.util.experimental.BoringUtils
import memory.{InstructionROM, OpenRAMInstrMem}
import wildcat.pipeline._


/*
 * Top-level for testing and verification
 *
 */
class WildcatTestCacheTop(file: String) extends Module {

  val io = IO(new Bundle {
    val regFile = Output(Vec(32,UInt(32.W)))
    val led = Output(UInt(8.W))
    val tx = Output(UInt(1.W))
    val rx = Input(UInt(1.W))
    val stop = Output(Bool())
  })
  val cpuTop = Module(new WildcatCache())

  io.regFile := DontCare
  BoringUtils.bore(cpuTop.cpu.debugRegs, Seq(io.regFile))
  io.stop := DontCare
  BoringUtils.bore(cpuTop.cpu.stop, Seq(io.stop))

  val (memory, start) = Util.getCode(file)
  val imem = Module(new InstructionROM(memory))
  cpuTop.memPort <> imem.cpuPort

  val mmio = Module(new WildcatMmio(100000000, 115200, 16))
  cpuTop.mmioPort <> mmio.cpuPort
  io.tx := mmio.io.tx
  mmio.io.rx := io.rx
  io.led := mmio.io.led(7, 0)
}
