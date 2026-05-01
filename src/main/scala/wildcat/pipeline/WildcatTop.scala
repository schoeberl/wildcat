package wildcat.pipeline

import chisel3._
import chisel.lib.uart._
import chisel3.util.RegEnable

import wildcat.Util
import device._
import memory.{InstructionROM, OpenRAMMem, ScratchPadMem}


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

  val imem = if (testFPGA) Module(new InstructionROM(memory)) else Module(new OpenRAMMem())
  cpu.io.imem <> imem.cpuPort
  // for read multiplexing
  val memAddressReg = RegEnable(cpu.io.dmem.address, 0.U, cpu.io.dmem.rd)


  val csMem = cpu.io.dmem.address(31, 28) === 0.U
  val dmem = if (testFPGA) Module(new ScratchPadMem(memory, nrBytes = dmemNrByte)) else Module(new OpenRAMMem())
  cpu.io.dmem <> dmem.cpuPort
  dmem.cpuPort.rd := csMem && cpu.io.dmem.rd
  dmem.cpuPort.wr := csMem && cpu.io.dmem.wr

  // IO is mapped ot 0xf000_0000
  // use bits 19..16 to select IO devices

  val csIO = cpu.io.dmem.address(31, 28) === 0xf.U
  val csIOReg = memAddressReg(31, 28) === 0xf.U
  val ioDecodeAddress = cpu.io.dmem.address(19,16)
  val ioDecodeAddressReg = memAddressReg(19, 16)

  val csLed = csIO && ioDecodeAddress === 1.U
  val csLedReg = csIOReg && ioDecodeAddressReg === 1.U
  val ledDevice = Module(new LedDevice(16))
  ledDevice.cpuPort <> cpu.io.dmem
  ledDevice.cpuPort.rd := csLed && cpu.io.dmem.rd
  ledDevice.cpuPort.wr := csLed && cpu.io.dmem.wr

  // read and ack mux
  cpu.io.dmem.rdData := dmem.cpuPort.rdData
  when (csLedReg) {
    cpu.io.dmem.rdData := ledDevice.cpuPort.rdData
  }
  cpu.io.dmem.ack := dmem.cpuPort.ack || ledDevice.cpuPort.ack

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
  when (memAddressReg(31, 28) === 0xf.U && memAddressReg(19,16) === 0.U) {
    when (memAddressReg(3, 0) === 0.U) {
      cpu.io.dmem.rdData := uartStatusReg
    } .elsewhen(memAddressReg(3, 0) === 4.U) {
      cpu.io.dmem.rdData := rx.io.channel.bits
    }
  }

  when ((cpu.io.dmem.address(31, 28) === 0xf.U) && cpu.io.dmem.wr) {
    when (cpu.io.dmem.address(19,16) === 0.U && cpu.io.dmem.address(3, 0) === 4.U) {
      printf(" %c %d\n", cpu.io.dmem.wrData(7, 0), cpu.io.dmem.wrData(7, 0))
      tx.io.channel.valid := true.B
    }
  }

  io.led := 1.U ## 0.U(7.W) ## RegNext(ledDevice.io.leds)
}

object WildcatTop extends App {
  emitVerilog(new WildcatTop(args(0), testFPGA = true), Array("--target-dir", "generated"))
}

object WildcatTopAsic extends App {
  emitVerilog(new WildcatTop(args(0), testFPGA = false), Array("--target-dir", "generated"))
}