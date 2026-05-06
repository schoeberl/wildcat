package wildcat.pipeline

import chisel3._
import chisel3.util.RegEnable

import memory._
import device._

import soc._
import debug.UartDebug

class LittleCat(frequency: Int = 100000000, baudRate: Int = 115200) extends Module {
  val io = IO(new Bundle {
    val out = Output(UInt(2.W))
    val in = Input(UInt(2.W))
    val rx = Input(UInt(1.W))
    val tx = Output(UInt(1.W))
    val rxConf = Input(UInt(1.W))
    val txConf = Output(UInt(1.W))
  })

  val config = Module(new UartDebug(frequency, baudRate, 64))
  config.io.rx := RegNext(RegNext(io.rxConf))
  io.txConf := RegNext(RegNext(config.io.tx))

  val cpu = Module(new ThreeCats())

  val imem = Module(new OpenRAMInstrMem())
  cpu.io.imem <> imem.cpuPort

  // Configuration port for writing instructions
  imem.writePort.wrData := config.io.dout(31, 0)
  imem.writePort.address := config.io.dout(41, 32)
  imem.writePort.wr := config.io.dout(42)
  imem.writePort.rd := config.io.dout(43)
  imem.writePort.wrMask := 15.U
  cpu.reset := RegNext(config.io.dout(44))
  config.io.din := imem.writePort.rdData

  // Address register for read multiplexing
  val memAddressReg = RegEnable(cpu.io.dmem.address, 0.U, cpu.io.dmem.rd)

  val csMem = cpu.io.dmem.address(31, 28) === 0.U
  val dmem = Module(new OpenRAMMem())
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
  val uartDevice = Module(new UartDevice(frequency, baudRate))
  io.tx := uartDevice.io.txd
  uartDevice.io.rxd := io.rx

  val csUart = csIO && ioDecodeAddress === 0.U
  val muxUart = csIOReg && ioDecodeAddressReg === 0.U
  uartDevice.cpuPort <> cpu.io.dmem
  uartDevice.cpuPort.rd := csUart && cpu.io.dmem.rd
  uartDevice.cpuPort.wr := csUart && cpu.io.dmem.wr

  class IODevice() extends PipeConDevice(32) {
    val io = IO(new Bundle {
      val out = Output(UInt(2.W))
      val in = Input(UInt(2.W))
    })

    val outReg = RegInit(0.U(2.W))
    when (cpuPort.wr) {
      outReg := cpuPort.wrData(1, 0)
    }
    cpuPort.rdData := RegNext(RegNext(io.in))
    cpuPort.ack := RegNext(cpuPort.rd || cpuPort.wr, false.B)
    io.out := RegNext(RegNext(outReg))
  }
  // Minimal IO device for testing
  val ioDevice = Module(new IODevice())
  ioDevice.io.in := io.in
  io.out := ioDevice.io.out

  val csIODev = csIO && ioDecodeAddress === 1.U
  val muxIODev = csIOReg && ioDecodeAddressReg === 1.U
  ioDevice.cpuPort <> cpu.io.dmem
  ioDevice.cpuPort.rd := csIODev && cpu.io.dmem.rd
  ioDevice.cpuPort.wr := csIODev && cpu.io.dmem.wr

  // read mux for memory and IO devices
  cpu.io.dmem.rdData := dmem.cpuPort.rdData
  when (muxUart) {
    cpu.io.dmem.rdData := uartDevice.cpuPort.rdData
  } .elsewhen(muxIODev) {
    cpu.io.dmem.rdData := ioDevice.io.out
  }
  // or reduce all ack signals
  cpu.io.dmem.ack := dmem.cpuPort.ack || uartDevice.cpuPort.ack || ioDevice.cpuPort.ack
}

object LittleCat extends App {
  emitVerilog(new LittleCat, Array("--target-dir", "generated"))
}
