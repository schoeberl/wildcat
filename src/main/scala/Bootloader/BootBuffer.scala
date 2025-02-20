package Bootloader

import chisel.lib.uart._
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._

/**
 * Bootloader buffer module by Alexander and Georg for the Wildcat
 */
class BootBuffer() extends Module {
  val io = IO(new Bundle {
    val saveCtrl = Input(UInt(1.W))
    val dataIn = Input(UInt(8.W))
    val dataOut = Output(UInt(32.W))
  })

  val buffer = RegInit(0.U(32.W))

  when(io.saveCtrl === 1.U){
    buffer := buffer(24,0) ## io.dataIn
  }

  io.dataOut := buffer

}
