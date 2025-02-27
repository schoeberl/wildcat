package Bootloader

import chisel3._
import chisel.lib.uart._
import chisel3.util._
import chisel3.experimental.ChiselEnum

/**
 * Bootloader by Alexander and Georg for the Wildcat
 *
 * Current version is simple and not sufficient for loading elf-files as needed for running uCLinux.
 * Current version modelled after the following figure: https://media.discordapp.net/attachments/1017062502066036897/1342132354218463233/Bootloader_fem_design.jpg?ex=67b92e68&is=67b7dce8&hm=81295ce8f7da45314c537b57b1d813111f06dd7174463621f7f2cd665a5e183b&=&format=webp&width=543&height=993
 */
class BootloaderTop(frequ: Int, baudRate: Int = 115200) extends Module {
  val io = IO(new Bundle {
    val instrData = Output(UInt(32.W))
    val wrEnabled = Output(UInt(1.W))
    val rx = Input(UInt(1.W))
  })

  //val tx = Module(new BufferedTx(100000000, baudRate))
  val rx = Module(new Rx(frequ, baudRate))
  val buffer = Module(new BootBuffer())
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

  buffer.io.saveCtrl := save
  buffer.io.dataIn := rx.io.channel.bits

  rx.io.channel.ready := false.B
  incr := 0.U
  save := 0.U
  wrEnabled := 0.U


  switch(stateReg){
    is(Idle){
      when(rx.io.channel.valid){
        stateReg := Sample
        incr := 1.U
        rx.io.channel.ready := true.B
        save := 1.U
      }
    }
    is(Sample){
      when(byteCount === 3.U) {
        wrEnabled := 1.U
        stateReg := Send
      } .elsewhen(rx.io.channel.valid && (byteCount =/= 3.U)){
        stateReg := Sample
        incr := 1.U
        rx.io.channel.ready := true.B
      } .elsewhen(true.B) {
        stateReg := Idle
      }
    }
    is(Send){
      when(rx.io.channel.valid) {
        incr := 1.U
        save := 1.U
        stateReg := Sample
        rx.io.channel.ready := true.B
      } .elsewhen(true.B) {
        stateReg := Idle
      }

    }
  }

  io.wrEnabled := wrEnabled
  io.instrData := buffer.io.dataOut
  rx.io.rxd := io.rx
}

object BootloaderTopTop extends App {
  emitVerilog(new BootloaderTop(100000000), Array("--target-dir", "generated"))
}