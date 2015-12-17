/*
 * This code is a minimal hardware described in Chisel.
 * 
 * Copyright: 2013, Technical University of Denmark, DTU Compute
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * License: Simplified BSD License
 * 
 * Blinking LED: the FPGA version of Hello World
 */

package hello

import Chisel._
import Node._

/**
 * The blinking LED component.
 * 
 * The BeMicro has a 16 MHz clock.
 * The DE0 has a 50 MHz clock.
 */

class Hello extends Module {
  val io = new Bundle {
    val led = UInt(OUTPUT, 1)
  }
  val CNT_MAX = UInt(16000000 / 2 - 1);
  val r1 = Reg(init = UInt(0, 25))
  val blk = Reg(init = UInt(0, 1))

  r1 := r1 + UInt(1)
  when(r1 === CNT_MAX) {
    r1 := UInt(0)
    blk := ~blk
  }
  io.led := blk
}

// Generate the Verilog code by invoking chiselMain() in our main()
object HelloMain {
  def main(args: Array[String]): Unit = {
    chiselMain(args, () => Module(new Hello()))
  }
}
