package wildcat.pipeline

import chisel3._
import wildcat.Util

/*
 * Copyright (c) 2015-2022, DTU
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
class Wildcat(args: Array[String]) extends Module {
  val io = IO(new Bundle {
    val debug = Output(UInt(32.W))
  })

  val (code, start) = Util.getCode(args)

  // Five pipeline stages
  val fetch = Module(new Fetch(code))
  val decode = Module(new Decode())
  val execute = Module(new Execute())
  val memory = Module(new Memory())
  val writeback = Module(new WriteBack())

  fetch.io.fedec <> decode.io.fedec
  decode.io.decex <> execute.io.decex
  execute.io.exmem <> memory.io.exmem
  memory.io.memwb <> writeback.io.memwb
  decode.io.wbdec <> writeback.io.wbdec

  decode.io.stall := false.B
  fetch.io.stall := false.B
  fetch.io.loadPc := false.B
  fetch.io.pcIn := 0.U

  io.debug := writeback.io.wbdec.data
}

object Wildcat extends App {
  emitVerilog(new Wildcat(Array("a.bin")), Array("--target-dir", "generated"))
}