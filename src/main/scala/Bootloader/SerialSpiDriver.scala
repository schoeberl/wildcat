package Bootloader

import com.fazecast.jSerialComm._

import chiseltest._

/**
 * A simple driver for SPI communication via the serial port and the debug interface.
 * USed from this App, the FlashTest with testing the controller, and should be used by Wildcat in simulation.
 * @param id (Adx, Flash, SRAM)
 */
class SerialSpiDriver(id: Int, portName: String = "/dev/tty.usbserial-210292B408601") {

  // TODO: fix the hard coded port name
  val port = SerialPort.getCommPort(portName)
  port.openPort()
  port.setBaudRate(115200)
  // port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0)
  port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0)
  val out = port.getOutputStream
  csHigh()

  def writeReadSerial(s: String): String = {
    for (c <- s) {
      out.write(c.toByte)
    }
    // println("Wrote: " + s)
    val buf = new Array[Byte](1)
    var ret = ""
    var stop = false
    while (!stop) {
      if (port.bytesAvailable() > 0) {
        port.readBytes(buf, 1)
        val c = buf(0).toChar
        ret = ret + c
        stop = c == '\n'
      }
    }
    // print("Received: " + ret)

    // Thread.sleep(10)
    ret
  }

  def setCmd(v: Int): String = {
    val cmd = v.toHexString
    id match {
      case 0 => "w44" + cmd + "\r"
      case 1 => "w4" + cmd + "4\r"
      case 2 => "w" + cmd + "44\r"
    }
  }

  def writeByte(v: Int) = {
    for (i <- 0 until 8) {
      val bits = ((v >> (7 - i)) & 1) << 1
      // clock off, set data
      writeReadSerial(setCmd(bits)) // clock off, set data
      // clock on, keep data
      writeReadSerial(setCmd(bits + 1)) // clock on, keep data
    }
    // not now, writeRead("w440\r") // clock off
  }

  def readByte() = {
    var v = 0
    for (i <- 0 until 8) {
      writeReadSerial(setCmd(0)) // clock off
      // data changes after neg edge
      writeReadSerial(setCmd(1)) // clock on
      // sample on pos edge
      val rx = writeReadSerial("r")
      // '8' is MISO bit set
      val bit = if (rx(8 - id) == '8') 1 else 0
      v = (v << 1) | bit
    }
    writeReadSerial(setCmd(0)) // clock off (maybe?), does not hurt on multibyte read
    v
  }

  def csLow() = writeReadSerial(setCmd(0))

  def csHigh() = writeReadSerial(setCmd(4))

}