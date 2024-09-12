package wildcat.three

import chisel3._
import wildcat.Util
import wildcat.common._


/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is the top-level for a three stage pipeline.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class ThreeTop(args: Array[String]) extends Module {

  val io = IO(new Bundle {
    val led = Output(UInt(8.W))
  })

  val (code, start) = Util.getCode(args)

  val cpu = Module(new Three())
  val dmem = Module(new ScratchPadMem())
  cpu.io.dmem <> dmem.io
  val imem = Module(new InstructionROM(code))
  imem.io.address := cpu.io.imem.address
  cpu.io.imem.data := imem.io.data
  cpu.io.imem.stall := imem.io.stall

  // quick hack to get the LED output
  val ledReg = RegInit(0.U(8.W))
  when (cpu.io.dmem.wrEnable === 15.U && cpu.io.dmem.wrAddress === 0.U) {
    ledReg := cpu.io.dmem.wrData(7, 0)
  }
  io.led := ledReg
}

object ThreeTop extends App {
  emitVerilog(new ThreeTop(args), Array("--target-dir", "generated"))
}