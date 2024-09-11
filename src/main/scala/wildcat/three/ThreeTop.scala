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
    val dummy = Output(UInt(32.W))
  })

  val (code, start) = Util.getCode(args)

  val cpu = Module(new Three())
  val dmem = Module(new ScratchPadMem())
  cpu.io.dmem <> dmem.io
  val imem = Module(new InstructionROM(code))
  imem.io.address := cpu.io.imem.address
  cpu.io.imem.data := imem.io.data
  cpu.io.imem.stall := imem.io.stall

  io.dummy := cpu.io.dmem.wrData
}

object ThreeTop extends App {
  emitVerilog(new ThreeTop(args), Array("--target-dir", "generated"))
}