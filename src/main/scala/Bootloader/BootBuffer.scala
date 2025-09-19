package Bootloader

import chisel.lib.uart._
import chisel3._
import chisel3.util._

/**
 * Bootloader buffer module by Alexander and Georg for the Wildcat
 *
 * This buffer works as a simple shift register. Goal is to receive 8x8 bit, structure them to be 1x64 bit
 * and then output those 64bits. We only accept new data into the buffer when saveCtrl is HIGH.
 */
class BootBuffer() extends Module {
  val io = IO(new Bundle {
    val saveCtrl = Input(UInt(1.W))
    val dataIn = Input(UInt(8.W))
    val dataOut = Output(UInt(64.W))
  })

  val buffer = RegInit(0.U(64.W))

  when(io.saveCtrl === 1.U){
    buffer := io.dataIn ## buffer(63,8) // little endian architecture dictates that LSB of instruction should get smallest address.
  }

  io.dataOut := buffer

}
