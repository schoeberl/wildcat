package wildcat.pipeline

import chisel3._

import wildcat.Util
import memory.{InstructionROM, OpenRAMMem, ScratchPadMem}
import memory.OpenRAMInstrMem


/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is one top-level for the Wildcat pipeline.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class WildcatTop(file: String, dmemNrByte: Int = 4096, testFPGA: Boolean = true) extends Module {

  val io = IO(new Bundle {
    val led = Output(UInt(16.W))
    val tx = Output(UInt(1.W))
    val rx = Input(UInt(1.W))
  })

  val (memory, start) = Util.getCode(file)

  // Switch between different pipes
  val cpu = Module(new ThreeCats())
  // val cpu = Module(new WildFour())
  // val cpu = Module(new StandardFive())

  val imem = if (testFPGA) Module(new InstructionROM(memory)) else Module(new OpenRAMInstrMem())
  cpu.io.imem <> imem.cpuPort

  val memoryMap = Module(new PipeConMemoryMap())
  cpu.io.dmem <> memoryMap.cpuPort

  val dmem = if (testFPGA) Module(new ScratchPadMem(memory, nrBytes = dmemNrByte)) else Module(new OpenRAMMem())
  memoryMap.memPort <> dmem.cpuPort

  val mmio = Module(new WildcatMmio(100000000, 115200, 16))
  memoryMap.mmioPort <> mmio.cpuPort
  io.tx := mmio.io.tx
  mmio.io.rx := io.rx
  io.led := 1.U ## 0.U(7.W) ## mmio.io.led(7, 0)
}

object WildcatTop extends App {
  emitVerilog(new WildcatTop(args(0), testFPGA = true), Array("--target-dir", "generated"))
}

object WildcatTopAsic extends App {
  emitVerilog(new WildcatTop(args(0), testFPGA = false), Array("--target-dir", "generated"))
}
