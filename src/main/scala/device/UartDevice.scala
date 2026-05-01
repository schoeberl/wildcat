package device

import chisel.lib.uart._
import chisel3._
import soc._

/**
 * UART as PipeCon device.
 * Should use PipeConDeviceRV, but that one is missing the single cycle write.
 *
 * Addressing:
 * 0 status register:
 *   bit 0 TX ready (TDE)
 *   bit 1 RX data available (RDF)
 * 4 send and receive register
 */
class UartDevice(frequency: Int, baudRate: Int = 115200) extends PipeConDevice(32) {
  val io = IO(new Bundle {
    val txd = Output(UInt(1.W))
    val rxd = Input(UInt(1.W))
  })

  val tx = Module(new BufferedTx(frequency, baudRate))
  val rx = Module(new Rx(frequency, baudRate))
  io.txd := tx.io.txd
  rx.io.rxd := io.rxd

  tx.io.channel.bits := cpuPort.wrData(7, 0)
  tx.io.channel.valid := false.B
  rx.io.channel.ready := RegNext(cpuPort.rd)

  val uartStatusReg = RegNext(rx.io.channel.valid ## tx.io.channel.ready)

  // we could also register the read output instead of the address
  val addrReg = RegNext(cpuPort.address(3, 0))

  cpuPort.rdData := uartStatusReg
  when (addrReg === 4.U) {
    cpuPort.rdData := rx.io.channel.bits
  }

  // Shall we decode the write address or just ignore?
  when (cpuPort.wr && (cpuPort.address(3, 0) === 4.U)) {
    printf(" %c %d\n", cpuPort.wrData(7, 0), cpuPort.wrData(7, 0))
    tx.io.channel.valid := true.B
  }

  // should we stall on an empty rx or a full tx?
  // Probably, but later with the PipeConDeviceRV
  cpuPort.ack := RegNext(cpuPort.rd || cpuPort.wr, false.B)
}
