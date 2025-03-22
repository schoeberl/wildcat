package Bootloader

import chisel3._
import chisel.lib.uart._
import chisel3.util._
import chisel3.experimental.ChiselEnum

/**
 * Bootloader by Alexander and Georg for the Wildcat
 *
 * Current version is simple and SHOULD BE sufficient for loading elf-files as needed for running uCLinux.
 * Current version is NO LONGER modelled after the following figure: https://media.discordapp.net/attachments/1017062502066036897/1342132354218463233/Bootloader_fem_design.jpg?ex=67b92e68&is=67b7dce8&hm=81295ce8f7da45314c537b57b1d813111f06dd7174463621f7f2cd665a5e183b&=&format=webp&width=543&height=993
 *    The model is outdated since Georg added address reading without adding it to the figure.
 *    To use this new module you should first send the address through UART and then immedietly after the instr
 *
 * Initial state waiting for magic number: 0xb00710ad maybe?
 * receive address, then receive data, then send data to address
 * Then return to idle
 *
 */
class BootloaderTop(frequ: Int, baudRate: Int = 115200) extends Module {
  val io = IO(new Bundle {
    val instrData = Output(UInt(32.W))
    val instrAddr = Output(UInt(32.W))
    val wrEnabled = Output(UInt(1.W))
    val rx = Input(UInt(1.W))
  })

  //val tx = Module(new BufferedTx(100000000, baudRate))
  val rx = Module(new Rx(frequ, baudRate))
  val buffer = Module(new BootBuffer())


  object State extends ChiselEnum {
    val Idle, Sleep = Value
  }
  import State._
  val stateReg = RegInit(Sleep)

  val incr = RegInit(0.U(1.W))
  val save = RegInit(0.U(1.W))
  val wrEnabled = RegInit(0.U(1.W))
  val byteCount = RegInit(0.U(4.W))

  when(incr === 1.U){
    byteCount := byteCount + 1.U
  }

  buffer.io.saveCtrl := save
  buffer.io.dataIn := rx.io.channel.bits

  rx.io.channel.ready := false.B
  incr := 0.U
  save := 0.U
  wrEnabled := 0.U


  switch(stateReg){
    is(Sleep){
      when(io.instrData === "hB00710AD".U){ //Magic nubmber is 0xB00710AD = BOOTLOAD
        stateReg := Idle
      } .elsewhen(rx.io.channel.valid){
        rx.io.channel.ready := true.B
        save := 1.U
        stateReg := Sleep
      } .elsewhen(true.B){
        stateReg := Sleep
      }
    }
    is(Idle) {
     when(byteCount === 8.U) {
       wrEnabled := 1.U
       byteCount := 0.U
       stateReg := Idle
     } .elsewhen(rx.io.channel.valid) {
       incr := 1.U
       rx.io.channel.ready := true.B
       save := 1.U
       stateReg := Idle
     } .elsewhen(true.B) {
       stateReg := Idle
     }
    }
  }

  io.wrEnabled := wrEnabled
  io.instrData := buffer.io.dataOut(63,32)
  io.instrAddr := buffer.io.dataOut(31,0)
  rx.io.rxd := io.rx
}

object BootloaderTopTop extends App {
  emitVerilog(new BootloaderTop(100000000), Array("--target-dir", "generated"))
}