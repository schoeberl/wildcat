package wildcat.pipeline

import chisel3._
import wildcat.Util


/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is the top-level for a three stage pipeline.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class WildcatTop(file: String) extends Module {

  val io = IO(new Bundle {
    val led = Output(UInt(8.W))
  })

  val (memory, start) = Util.getCode(file)

  // Here switch between different designs
  val cpu = Module(new ThreeCats())
  // val cpu = Module(new WildFour())
  // val cpu = Module(new StandardFive())
  val dmem = Module(new ScratchPadMem(memory))
  cpu.io.dmem <> dmem.io
  val imem = Module(new InstructionROM(memory))
  imem.io.address := cpu.io.imem.address
  cpu.io.imem.data := imem.io.data
  cpu.io.imem.stall := imem.io.stall

  // quick hack to get the LED output, should do some decoding
  val ledReg = RegInit(0.U(8.W))
  when ((cpu.io.dmem.wrAddress === 0xe000_0000L.U) && cpu.io.dmem.wrEnable(0)) {
    ledReg := cpu.io.dmem.wrData(7, 0)
    dmem.io.wrEnable := VecInit(Seq.fill(4)(false.B))
  } .elsewhen ((cpu.io.dmem.wrAddress === 0xf000_0000L.U) && cpu.io.dmem.wrEnable(0)) {
    printf("%c", cpu.io.dmem.wrData(7, 0))
    dmem.io.wrEnable := VecInit(Seq.fill(4)(false.B))
  }
  io.led := RegNext(ledReg)
}

object WildcatTop extends App {
  emitVerilog(new WildcatTop(args(0)), Array("--target-dir", "generated"))
}