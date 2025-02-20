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
  //Insert counter module

  object State extends ChiselEnum {
    val Idle, Sample, Send = Value
  }
  import State._
  val stateReg = RegInit(Idle)

  val incr = RegInit(0.U(1.W))
  val save = RegInit(0.U(1.W))
  val wrEnabled = RegInit(0.U(1.W))



  switch(stateReg){
    is(Idle){
      when(rx.io.channel.valid){
        stateReg := Sample
        incr := 1.U
        save := 1.U
      }
    }
    is(Sample){
      when(counter.io.byteCnt === 3.U){ //temp couner signal
        wrEnabled := 1.U
        stateReg := Send
      } .elsewhen(rx.io.channel.valid && (counter.io.byteCnt =/= 3.U)){
        stateReg := Sample
      } .elsewhen(true.B){
        stateReg := Idle
      }
    }
    is(Send){

    }
  }

}
