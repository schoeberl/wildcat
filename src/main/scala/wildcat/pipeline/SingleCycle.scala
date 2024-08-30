package wildcat.pipeline

import chisel3._

/**
 * A single cycle implementation of the Wildcat processor.
 * Not practical, but can serve as ISA simulator.
 *
 * @param args
 */
class SingleCycle(args: Array[String]) extends Wildcat(args) {

  val pcReg = RegInit(0.U(32.W))
  val imem = VecInit(code.toIndexedSeq.map(_.S(32.W).asUInt))

  val instr = imem(pcReg(31, 2))
  pcReg := pcReg + 4.U

}
