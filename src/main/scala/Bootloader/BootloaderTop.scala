package Bootloader

import chisel3._
import chisel.lib.uart._
import chisel3.util._
import chisel3.experimental.ChiselEnum

/**
 * Bootloader by Alexander and Georg for the Wildcat
 */
class BootloaderTop(frequ: Int, baudRate: Int = 115200) extends Module {
  val io = IO(new Bundle {

  })

  //val tx = Module(new BufferedTx(100000000, baudRate))
  val rx = Module(new Rx(100000000, baudRate))
  //Insert Buffer module
  //Counter for keeping track of address and when 4 bytes are ready to be sent
  val counter = RegInit(0.U(32.W))


  object State extends ChiselEnum {
    val Idle, Sample, Send = Value
  }
  import State._
  val stateReg = RegInit(Idle)

  val incr = RegInit(0.U(1.W))
  val save = RegInit(0.U(1.W))
  val wrEnabled = RegInit(0.U(1.W))

  when(incr === 1.U){
    counter := counter + 1.U
  }
  val byteCount = counter % 4.U

  switch(stateReg){
    is(Idle){
      when(rx.io.channel.valid){
        stateReg := Sample
        incr := 1.U
        save := 1.U
      }
    }
    is(Sample){
      when(byteCount === 3.U) { //temp couner signal
        wrEnabled := 1.U
        stateReg := Send
      } .elsewhen(rx.io.channel.valid && (byteCount =/= 3.U)){
        stateReg := Sample
      } .elsewhen(true.B) {
        stateReg := Idle
      }
    }
    is(Send){
      when(rx.io.channel.valid) {
        incr := 1.U
        save := 1.U
        stateReg := Sample
      } .elsewhen(true.B) {
        stateReg := Idle
      }

    }
  }

}
