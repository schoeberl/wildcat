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

class SimRV(code: Array[Int], mem: Array[Int]) {

  // That's the state of the processor
  // That's it, nothing else (except memory ;-)
  var pc = 0
  var reg = new Array[Int](32)
  reg(0) = 0
  // TODO: maybe initialize other registers with random values

  def execute(instr: Int) {
    printf("instr -> %08x\n", instr)
    // quick extraction of decoded fields
    val opcode = instr & 0x7f
    val rd = (instr >> 7) & 0x01f
    val rs1 = (instr >> 15) & 0x01f
    val rs2 = (instr >> 20) & 0x01f
    val funct3 = (instr >> 12) & 0x07
    val funct7 = (instr >> 25) & 0x03f
    // immediate is more tricky
    val immi = (instr & 0xfff00000) >> 20
    val imms = ((instr & 0xfe00000) >> (25 - 5)) | ((opcode & 0x0f80) >> 7)
    val immu = (instr & 0xfffff000) >>> 12
    // TODO: there are two additional versions of immediate
    
    val sraSub = funct7 == SRA_SUB

    def alu(funct3: Int, sraSub: Boolean, op1: Int, op2: Int): Int = {

      val shamt = op2 & 0x1f
      
      printf("instr: %d ops: %08x %08x\n", funct3, op1, op2)

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

    // Don't know if we really should use Tuples.
    // Scala is already stretching the readability for teaching.
    
    val result = opcode match {
      case AluImm => (alu(funct3, sraSub, reg(rs1), immi), true)
      case Alu => (alu(funct3, sraSub, reg(rs1), reg(rs2)), true)     
    }
    
    println("result " + result)
    
    if (rd != 0 && result._2) {
      reg(rd) = result._1
    }
  }

  while (pc < code.length) {
    execute(code(pc))
    for (i <- 0 to 3) {
      printf("%08x ", reg(i))
    }
    println
    pc += 1
  }

}

/* 
 * TODO: grab the precompiled tests form sodor and run them.
 * 
 * Test result signalling in riscv-test
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