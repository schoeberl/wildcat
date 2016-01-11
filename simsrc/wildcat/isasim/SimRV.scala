/*
 * Copyright (c) 2015-2016, DTU
 * Simplified BSD License
 */

/*
 * A simple ISA simulator of RISC-V.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * 
 * TODO: should live in the same world as Chisel source,
 * but compile errors make it less convenient.
 */

package wildcat.isasim

import Opcode._
import AluFunct._
import AluFunct7._
import BranchFunct._

class SimRV(code: Array[Int], mem: Array[Int]) {

  // That's the state of the processor
  // That's it, nothing else (except memory ;-)
  var pc = 0
  var reg = new Array[Int](32)
  reg(0) = 0
  // TODO: maybe initialize other registers with random values

  def execute(instr: Int) {

    // extraction of decoded fields
    val opcode = instr & 0x7f
    val rd = (instr >> 7) & 0x01f
    val rs1 = (instr >> 15) & 0x01f
    val rs2 = (instr >> 20) & 0x01f
    val funct3 = (instr >> 12) & 0x07
    val funct7 = (instr >> 25) & 0x03f
    // immediate is more tricky - probably the main overhead in simulation
    val immi = (instr & 0xfff00000) >> 20
    val imms = ((instr & 0xfe00000) >> (25 - 5)) | ((opcode & 0x0f80) >> 7)
    val immu = (instr & 0xfffff000) >>> 12
    val immb = (instr & 0x80000000) >> 19 | (instr & 0x0080) << 4 |
      (instr & 0x7e000000) >>> 20 | (instr & 0x0f00) >>> 7
    val boff = immb >> 2 // now in words
    // TODO: there is one additional versions of immediate

    // single bit on extended function
    val sraSub = funct7 == SRA_SUB

    def alu(funct3: Int, sraSub: Boolean, op1: Int, op2: Int): Int = {
      val shamt = op2 & 0x1f

      funct3 match {
        case ADD_SUB => if (sraSub) op1 - op2 else op1 + op2
        case SLL => op1 << shamt
        case SLT => if (op1 < op2) 1 else 0
        case SLTU => if ((op1 < op2) ^ (op1 < 0) ^ (op2 < 0)) 1 else 0
        case XOR => op1 ^ op2
        case SRL_SRA => if (sraSub) op1 >> shamt else op1 >>> shamt
        case OR => op1 | op2
        case AND => op1 & op2
      }
    }

    def compare(funct3: Int, rs1: Int, rs2: Int): Boolean = {
      funct3 match {
        case BEQ => rs1 == rs2
        case BNE => !(rs1 == rs2)
        case BLT => rs1 < rs2
        case BGE => rs1 >= rs2
        case BLTU => (rs1 < rs2) ^ (rs1 < 0) ^ (rs2 < 0)
        case BGEU => rs1 == rs2 || ((rs1 > rs2) ^ (rs1 < 0) ^ (rs2 < 0))
      }
    }

    // Don't know if we really should use Tuples.
    // Scala is already stretching the readability for teaching.

    val result = opcode match {
      case AluImm => (alu(funct3, sraSub, reg(rs1), immi), true, pc + 1)
      case Alu => (alu(funct3, sraSub, reg(rs1), reg(rs2)), true, pc + 1)
      case Branch => (0, false,
        if (compare(funct3, reg(rs1), reg(rs2))) pc + boff else pc + 1)
      case _ => {
        throw new Exception("Opcode " + opcode + " not implemented")
      }
    }

    printf("instr: %08x ", instr)
    // println("result " + result)
    printf("pc: %04x ", pc * 4)

    if (rd != 0 && result._2) {
      reg(rd) = result._1
    }

    pc = result._3
  }

  while (pc < code.length) {
    execute(code(pc))
    print("regs: ")
    for (i <- 0 to 3) {
      printf("%08x ", reg(i))
    }
    println
  }

}

/* 
 * TODO: grab the precompiled tests form sodor and run them.
 * 
 * Test result signaling in riscv-test
 * 
 * #undef RVTEST_PASS
#define RVTEST_PASS li a0, 1; scall

#undef RVTEST_FAIL
#define RVTEST_FAIL sll a0, TESTNUM, 1; 1:beqz a0, 1b; or a0, a0, 1; scall;
 */

object SimRV extends App {
  println("Hello RISC-V World")

  val codeLocal = Array(
    Helper.genAlu(AluImm, ADD_SUB, 0, 0, 0x0f, 0),
    Helper.genAlu(AluImm, ADD_SUB, 0, 0, 111, 1),
    Helper.genAlu(AluImm, ADD_SUB, 1, 0, 222, 2),
    Helper.genAlu(Alu, ADD_SUB, 1, 2, 0, 3))

  val mem = new Array[Int](1024)

  val code = Util.readBin("/Users/martin/source/wildcat/asm/a.bin")

  new SimRV(code, mem)
}