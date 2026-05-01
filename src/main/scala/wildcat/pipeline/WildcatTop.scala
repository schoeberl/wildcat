package wildcat.pipeline

import chisel3._
import chisel3.util.RegEnable

import wildcat.Util
import device._
import memory.{InstructionROM, OpenRAMMem, ScratchPadMem}


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

  val imem = if (testFPGA) Module(new InstructionROM(memory)) else Module(new OpenRAMMem())
  cpu.io.imem <> imem.cpuPort

  // Address register for read multiplexing
  val memAddressReg = RegEnable(cpu.io.dmem.address, 0.U, cpu.io.dmem.rd)

  val csMem = cpu.io.dmem.address(31, 28) === 0.U
  val dmem = if (testFPGA) Module(new ScratchPadMem(memory, nrBytes = dmemNrByte)) else Module(new OpenRAMMem())
  cpu.io.dmem <> dmem.cpuPort
  dmem.cpuPort.rd := csMem && cpu.io.dmem.rd
  dmem.cpuPort.wr := csMem && cpu.io.dmem.wr

  // IO is mapped ot 0xf000_0000
  // bits 19..16 are used to select IO devices
  val csIO = cpu.io.dmem.address(31, 28) === 0xf.U
  val csIOReg = memAddressReg(31, 28) === 0xf.U
  val ioDecodeAddress = cpu.io.dmem.address(19,16)
  val ioDecodeAddressReg = memAddressReg(19, 16)

  // Everyone needs a UART
  val uartDevice = Module(new UartDevice(100000000, 115200))
  io.tx := uartDevice.io.txd
  uartDevice.io.rxd := io.rx

  val csUart = csIO && ioDecodeAddress === 0.U
  val muxUart = csIOReg && ioDecodeAddressReg === 0.U
  uartDevice.cpuPort <> cpu.io.dmem
  uartDevice.cpuPort.rd := csUart && cpu.io.dmem.rd
  uartDevice.cpuPort.wr := csUart && cpu.io.dmem.wr

  // We also love to have an LED to blink
  val ledDevice = Module(new LedDevice(16))
  io.led := 1.U ## 0.U(7.W) ## RegNext(ledDevice.io.leds)

  val csLed = csIO && ioDecodeAddress === 1.U
  val muxLed = csIOReg && ioDecodeAddressReg === 1.U
  ledDevice.cpuPort <> cpu.io.dmem
  ledDevice.cpuPort.rd := csLed && cpu.io.dmem.rd
  ledDevice.cpuPort.wr := csLed && cpu.io.dmem.wr

  // read mux for memory and IO devices
  cpu.io.dmem.rdData := dmem.cpuPort.rdData
  when (muxUart) {
    cpu.io.dmem.rdData := uartDevice.cpuPort.rdData
  } .elsewhen(muxLed) {
    cpu.io.dmem.rdData := RegNext(ledDevice.io.leds)
  }
  // or reduce all ack signals
  cpu.io.dmem.ack := dmem.cpuPort.ack || uartDevice.cpuPort.ack || ledDevice.cpuPort.ack
}

object WildcatTop extends App {
  emitVerilog(new WildcatTop(args(0), testFPGA = true), Array("--target-dir", "generated"))
}

object WildcatTopAsic extends App {
  emitVerilog(new WildcatTop(args(0), testFPGA = false), Array("--target-dir", "generated"))
}