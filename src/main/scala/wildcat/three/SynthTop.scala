package wildcat.three

import chisel3._
import wildcat.Util
import wildcat.common._

// Only for synthesis tests, dummy write to instruction memory
class SynthTop(args: Array[String]) extends Module {

  val io = IO(new Bundle {
    val dummy = Output(UInt(32.W))
    val wrData = Input(UInt(32.W))
    val wrAddr = Input(UInt(32.W))
  })

  val (code, start) = Util.getCode(args)

  val cpu = Module(new Three())
  val dmem = Module(new ScratchPadMem())
  cpu.io.dmem <> dmem.io

  val imem = Module(new ScratchPadMem())
  imem.io.rdAddress := cpu.io.imem.address
  cpu.io.imem.data := imem.io.rdData
  cpu.io.imem.stall := imem.io.stall

  // dummy write for synthesis tests
  imem.io.wrAddress := io.wrAddr
  imem.io.wrData := io.wrData
  imem.io.wrEnable := 15.U

  io.dummy := RegNext(cpu.io.dmem.wrData)
}

object SynthTop extends App {
  emitVerilog(new SynthTop(Array.empty[String]), Array("--target-dir", "generated"))
}