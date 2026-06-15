package wildcat.pipeline

import chisel3._
import chisel3.util.RegEnable
import device.{LedDevice, UartDevice}
import soc._

/**
 * Shared data-memory address split.
 *
 * 0xf000_0000..0xffff_ffff is uncached MMIO. Everything else is cacheable
 * memory/SPM.
 */
class PipeConMemoryMap(addrWidth: Int = 32) extends Module {
  val cpuPort = IO(new PipeConIO(addrWidth))
  val memPort = IO(Flipped(new PipeConIO(addrWidth)))
  val mmioPort = IO(Flipped(new PipeConIO(addrWidth)))

  val cpuCmd = cpuPort.rd || cpuPort.wr
  val isMmio = cpuPort.address(31, 28) === 0xf.U
  val respIsMmioReg = RegEnable(isMmio, false.B, cpuCmd)

  memPort.address := cpuPort.address
  memPort.rd := cpuPort.rd && !isMmio
  memPort.wr := cpuPort.wr && !isMmio
  memPort.wrData := cpuPort.wrData
  memPort.wrMask := cpuPort.wrMask

  mmioPort.address := cpuPort.address
  mmioPort.rd := cpuPort.rd && isMmio
  mmioPort.wr := cpuPort.wr && isMmio
  mmioPort.wrData := cpuPort.wrData
  mmioPort.wrMask := cpuPort.wrMask

  cpuPort.rdData := Mux(respIsMmioReg, mmioPort.rdData, memPort.rdData)
  cpuPort.ack := Mux(respIsMmioReg, mmioPort.ack, memPort.ack)
}

/**
 * Shared Wildcat MMIO device decoder.
 *
 * 0xf000_0000: UART
 * 0xf001_0000: LEDs
 */
class WildcatMmio(frequency: Int = 100000000, baudRate: Int = 115200, ledWidth: Int = 16) extends PipeConDevice(32) {
  val io = IO(new Bundle {
    val led = Output(UInt(ledWidth.W))
    val tx = Output(UInt(1.W))
    val rx = Input(UInt(1.W))
  })

  val cmd = cpuPort.rd || cpuPort.wr
  val addrReg = RegEnable(cpuPort.address, 0.U, cmd)
  val deviceSelect = cpuPort.address(19, 16)
  val deviceSelectReg = addrReg(19, 16)

  val uartDevice = Module(new UartDevice(frequency, baudRate))
  uartDevice.io.rxd := io.rx
  io.tx := uartDevice.io.txd

  val csUart = deviceSelect === 0.U
  uartDevice.cpuPort.address := cpuPort.address
  uartDevice.cpuPort.rd := cpuPort.rd && csUart
  uartDevice.cpuPort.wr := cpuPort.wr && csUart
  uartDevice.cpuPort.wrData := cpuPort.wrData
  uartDevice.cpuPort.wrMask := cpuPort.wrMask

  val ledDevice = Module(new LedDevice(ledWidth))
  io.led := RegNext(ledDevice.io.leds)

  val csLed = deviceSelect === 1.U
  ledDevice.cpuPort.address := cpuPort.address
  ledDevice.cpuPort.rd := cpuPort.rd && csLed
  ledDevice.cpuPort.wr := cpuPort.wr && csLed
  ledDevice.cpuPort.wrData := cpuPort.wrData
  ledDevice.cpuPort.wrMask := cpuPort.wrMask

  cpuPort.rdData := uartDevice.cpuPort.rdData
  when(deviceSelectReg === 1.U) {
    cpuPort.rdData := RegNext(ledDevice.io.leds)
  }
  cpuPort.ack := uartDevice.cpuPort.ack || ledDevice.cpuPort.ack
}
