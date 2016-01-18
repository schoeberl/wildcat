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
import LoadStoreFunct._
import InstrType._

class SimRV(mem: Array[Int]) {

  // That's the state of the processor
  // That's it, nothing else (except memory ;-)
  var pc = 0
  var reg = new Array[Int](32)
  reg(0) = 0
  // TODO: maybe initialize other registers with random values

  def execute(instr: Int): Boolean = {

    // extraction of decoded fields
    val opcode = instr & 0x7f
    val rd = (instr >> 7) & 0x01f
    val rs1 = (instr >> 15) & 0x01f
    val rs2 = (instr >> 20) & 0x01f
    val funct3 = (instr >> 12) & 0x07
    val funct7 = (instr >> 25) & 0x03f

    /**
     * Immediate generation is a little bit more elaborated,
     * but shall give smaller multiplexers in the hardware.
     */
    def genImm() = {

      val iType: InstrType = opcode match {
        case AluImm => I
        case Alu => R
        case Branch => SB
        case Load => I
        case Store => S
        case Lui => U
        case AuiPc => U
        case Jal => UJ
        case JalR => I
        case SCall => R
        case _ => R
      }

      // subfields of the instruction 
      val instr7 = (instr >> 7) & 0x01
      val instr11_8 = (instr >> 8) & 0x0f
      val instr19_12 = (instr >> 12) & 0xff
      val instr20 = (instr >> 20) & 0x01
      val instr24_21 = (instr >> 21) & 0x0f
      val instr31_20 = (instr >> 20) & 0xfff
      val instr30_25 = (instr >> 25) & 0x3f
      val instr31 = (instr >> 31) & 0x01
      val sext8 = if (instr31 == 1) 0xff else 0
      val sext12 = if (instr31 == 1) 0xfff else 0

      // subfields of the immediate, depending on instruction type
      val imm0 = iType match {
        case I => instr20
        case S => instr7
        case _ => 0
      }
      val imm4_1 = iType match {
        case I => instr24_21
        case U => 0
        case UJ => instr24_21
        case _ => instr11_8
      }
      val imm10_5 = if (iType == U) 0 else instr30_25
      val imm11 = iType match {
        case SB => instr7
        case U => 0
        case UJ => instr20
        case _ => instr31
      }
      val imm19_12 = if (iType == U || iType == UJ) instr19_12 else sext8
      val imm31_20 = if (iType == U) instr31_20 else sext12

      // now glue together
      (imm31_20 << 20) | (imm19_12 << 12) | (imm11 << 11) |
        (imm10_5 << 5) | (imm4_1 << 1) | imm0
    }

    //    // immediate is more tricky - probably the main overhead in simulation
    //    // maybe compute it within the function and only when used - do benchmark
    //    // this first, before obscuring readability
    //    val immi = (instr & 0xfff00000) >> 20
    //    val imms = ((instr & 0xfe00000) >> (25 - 5)) | ((opcode & 0x0f80) >> 7)
    //    val immu = (instr & 0xfffff000) >>> 12
    //    val immb = (instr & 0x80000000) >> 19 | (instr & 0x0080) << 4 |
    //      (instr & 0x7e000000) >>> 20 | (instr & 0x0f00) >>> 7
    //    val boff = immb >> 2 // now in words
    //    // TODO: there is one additional version of immediate

    val imm = genImm()
    // val boff = imm >> 2 // now in words

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

    def compare(funct3: Int, op1: Int, op2: Int): Boolean = {
      funct3 match {
        case BEQ => op1 == op2
        case BNE => !(op1 == op2)
        case BLT => op1 < op2
        case BGE => op1 >= op2
        case BLTU => (op1 < op2) ^ (op1 < 0) ^ (op2 < 0)
        case BGEU => op1 == op2 || ((op1 > op2) ^ (op1 < 0) ^ (op2 < 0))
      }
    }

    def load(funct3: Int, base: Int, displ: Int): Int = {
      funct3 match {
        case LSB => throw new Exception("B implementation needed")
        case LSH => throw new Exception("H implementation needed")
        case LSW => mem(((base + displ) >> 2) & 0x3ff)
        case LBU => throw new Exception("BU implementation needed")
        case LHU => throw new Exception("HU implementation needed")
      }
    }

    def store(funct3: Int, base: Int, displ: Int, value: Int): Unit = {
      funct3 match {
        case LSB => throw new Exception("B implementation needed")
        case LSH => throw new Exception("H implementation needed")
        case LSW => {
          mem(((base + displ) >> 2) & 0x3ff) = value
        }
        case LBU => throw new Exception("BU implementation needed")
        case LHU => throw new Exception("HU implementation needed")
      }
    }
    // read register file
    val rs1Val = reg(rs1)
    val rs2Val = reg(rs2)
    // next pc
    val pcNext = pc + 4

    printf("pc: %04x ", pc)
    printf("instr: %08x ", instr)

    // Execute the instruction and
    // return a tuple for the result: (ALU result, writeBack, next PC)
    val result = opcode match {
      case AluImm => (alu(funct3, sraSub, rs1Val, imm), true, pcNext)
      case Alu => (alu(funct3, sraSub, rs1Val, rs2Val), true, pcNext)
      case Branch => (0, false, if (compare(funct3, rs1Val, rs2Val)) pc + imm else pcNext)
      case Load => (load(funct3, rs1Val, imm), true, pcNext)
      case Store => store(funct3, rs1Val, imm, rs2Val); (0 , false, pcNext)
      case AuiPc => (pc + imm, true, pcNext)
      case Jal => {
        println(pc + " " + imm + " " + ((pc + imm) & 0xfffffffe))
        (pc + 4, true, (pc + imm) & 0xfffffffe)
      }
      case SCall => {
        // test a0 (x10) for test condition: 1 = ok
        // but FlexPRET examples use x1 -- maybe change those examples later
        if (reg(1) == 1) {
          println("Test passed")
        } else {
          println("Test failed with return code " + reg(1))
        }
        (0, false, pcNext)
      }
      case _ => {
        throw new Exception("Opcode " + opcode + " not (yet) implemented")
      }
    }

    if (rd != 0 && result._2) {
      reg(rd) = result._1
    }

    val oldPc = pc
    pc = result._3

    pc != oldPc // detect endless loop to stop simulation
  }

  while (execute(mem(pc >> 2))) {
    print("regs: ")
    for (i <- 0 to 7) {
      printf("%08x ", reg(i))
    }
    println
  }

}

/* 
 * TODO: grab the precompiled tests form sodor and run them.
 * Or better use the riscv-test code.
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

  val mem = new Array[Int](1024)

  val code = Util.readBin("/Users/martin/source/wildcat/asm/a.bin")

  for (i <- 0 until code.length) {
    mem(i) = code(i)
  }

  new SimRV(mem)
}