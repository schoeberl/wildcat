package wildcat.pipeline

import chisel3._
import wildcat.Util

import chisel.lib.uart._
import chisel3.util.RegEnable

/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is the top-level for a three stage pipeline.
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

  // Here switch between different designs
  val cpu = Module(new ThreeCats())
  // val cpu = Module(new WildFour())
  // val cpu = Module(new StandardFive())
  val dmem = if (testFPGA) Module(new ScratchPadMem(memory, nrBytes = dmemNrByte)) else Module(new OpenRAMMem())
  cpu.io.dmem <> dmem.cpuPort
  // gate memory access when not to data memory
  when (cpu.io.dmem.address(31, 28) =/= 0.U) {
    dmem.cpuPort.rd      := false.B
    dmem.cpuPort.wr      := false.B
  }
  val imem = if (testFPGA) Module(new InstructionROM(memory)) else Module(new OpenRAMMem())
  cpu.io.imem <> imem.cpuPort

  // Here IO stuff
  // IO is mapped ot 0xf000_0000
  // use lower bits to select IOs

  // UART:
  // 0xf000_0000 status:
  // bit 0 TX ready (TDE)
  // bit 1 RX data available (RDF)
  // 0xf000_0004 send and receive register

  val tx = Module(new BufferedTx(100000000, 115200))
  val rx = Module(new Rx(100000000, 115200))
  io.tx := tx.io.txd
  rx.io.rxd := io.rx

  tx.io.channel.bits := cpu.io.dmem.wrData(7, 0)
  tx.io.channel.valid := false.B
  rx.io.channel.ready := cpu.io.dmem.rd && (cpu.io.dmem.address(31, 28) === 0xf.U && cpu.io.dmem.address(19,16) === 0.U && cpu.io.dmem.address(3, 0) === 4.U)

  val uartStatusReg = RegNext(rx.io.channel.valid ## tx.io.channel.ready)
  val memAddressReg = RegEnable(cpu.io.dmem.address, 0.U, cpu.io.dmem.rd)
  when (memAddressReg(31, 28) === 0xf.U && memAddressReg(19,16) === 0.U) {
    when (memAddressReg(3, 0) === 0.U) {
      cpu.io.dmem.rdData := uartStatusReg
    } .elsewhen(memAddressReg(3, 0) === 4.U) {
      cpu.io.dmem.rdData := rx.io.channel.bits
    }
  }

  val ledReg = RegInit(0.U(8.W))
  when ((cpu.io.dmem.address(31, 28) === 0xf.U) && cpu.io.dmem.wr) {
    when (cpu.io.dmem.address(19,16) === 0.U && cpu.io.dmem.address(3, 0) === 4.U) {
      printf(" %c %d\n", cpu.io.dmem.wrData(7, 0), cpu.io.dmem.wrData(7, 0))
      tx.io.channel.valid := true.B
    } .elsewhen (cpu.io.dmem.address(19,16) === 1.U) {
      ledReg := cpu.io.dmem.wrData(7, 0)
    }
  }

  io.led := 1.U ## 0.U(7.W) ## RegNext(ledReg)
}

object WildcatTop extends App {
  emitVerilog(new WildcatTop(args(0), testFPGA = true), Array("--target-dir", "generated"))
}