package wildcat.pipeline

import chisel3._
import chisel3.util.RegEnable

import wildcat.Util
import device._

import soc._


/*
 * This file is part of the RISC-V processor Wildcat.
 *
 * This is a top-level for the Wildcat pipeline including caches.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 */
class WildcatCache() extends Module {

  val cpu = Module(new ThreeCats())

  val memPort = IO(Flipped(new PipeConIO(32)))

  val icache = Module(new PipeConCache())
  cpu.io.imem <> icache.cpuPort
  val dcache = Module(new PipeConCache())
  cpu.io.dmem <> dcache.cpuPort

  val arbiter = Module(new PipeConArbiter(addrWidth = 32, nrPorts = 2))
  arbiter.cpuPorts(0) <> icache.memPort
  arbiter.cpuPorts(1) <> dcache.memPort
  arbiter.memPort <> memPort
}

object WildcatCache extends App {
  emitVerilog(new WildcatCache(), Array("--target-dir", "generated"))
}