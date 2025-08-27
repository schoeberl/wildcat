package wildcat.pipeline

import chisel3._
import soc._

/**
 * On-chip memory with one clock cycle read timing, preloaded on construction.
 */
class InstructionROM(code: Array[Int]) extends Module {
  val io = IO(PipeCon(32))

  // No reset, as ASIC memories have no reset
  val addrReg = Reg(UInt(32.W))
  addrReg := io.address
  val instructions = VecInit(code.toIndexedSeq.map(_.S(32.W).asUInt))
  io.rdData := instructions(addrReg(31, 2))
  // simulating cache misses
  val toggle = RegInit(false.B)
  toggle := !toggle
  // first instruction shall not be executed (random address register)
  val firstReg = RegInit(true.B)
  firstReg := false.B
  io.ack := !(firstReg || false.B) // add toggle
}
