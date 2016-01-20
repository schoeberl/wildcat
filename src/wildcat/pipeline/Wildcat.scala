/*
 * Copyright (c) 2015-2016, DTU
 * Simplified BSD License
 */

/*
 * This file is part of the RISC-V processor Wildcat.
 * 
 * Wildcat will become a simple RISC-V pipeline.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * 
 */

package wildcat.pipeline

import Chisel._
import Node._

/**
 * To show that there is something executing in the FPGA
 * provide a blinking LED.
 *
 * The DE2 has a 50 MHz clock.
 */

class Blinker extends Module {
    val io = new Bundle {
    val led = UInt(OUTPUT, 1)
  }
  val CNT_MAX = UInt(50000000 / 2 - 1);
  val r1 = Reg(init = UInt(0, 25))
  val blk = Reg(init = UInt(0, 1))

  r1 := r1 + UInt(1)
  when(r1 === CNT_MAX) {
    r1 := UInt(0)
    blk := ~blk
  }
  io.led := blk
}

class Wildcat extends Module {
  val io = new Bundle {
    val led = UInt(OUTPUT, 1)
  }
  val blink = Module(new Blinker())
  io <> blink.io

  val fetch = Module(new Fetch())
  val decode = Module(new Decode())
  val execute = Module(new Execute())
  val memory = Module(new Memory())
  val writeback = Module(new WriteBack())
  
  // connect the pipeline stages
  fetch.io.fedec <> decode.io.fedec
  decode.io.decex <> execute.io.decex
  execute.io.exmem <> memory.io.exmem
  memory.io.memwb <> writeback.io.memwb
}

object WildcatMain {
  def main(args: Array[String]): Unit = {
    chiselMain(args, () => Module(new Wildcat()))
  }
}

/**
 * Some testing
 */
class WildcatTester(dut: Wildcat) extends Tester(dut) {

  // This is exhaustive testing, which usually is not possible
  for (a <- 0 to 15) {
    step(1) // for a truly combinational circuit not really needed
    // expect(dut.io.result, res.litValue())
  }
}

object WildcatTester {
  def main(args: Array[String]): Unit = {
    println("Testing Wildcat")
    chiselMainTest(args, () => Module(new Wildcat())) {
      f => new WildcatTester(f)
    }
  }
}

