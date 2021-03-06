/*
 * Copyright (c) 2015-2017, DTU
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

class SimRV(mem: Array[Int], start: Int, stop: Int) {

  // That's the state of the processor.
  // That's it, nothing else (except memory ;-)
  var pc = start // RISC-V tests start at 0x200
  var reg = new Array[Int](32)
  reg(0) = 0

  // stop on a test end
  var run = true;

  // some statistics
  var iCnt = 0

  def execute(instr: Int): Boolean = {

    // Do some decoding: extraction of decoded fields
    val opcode = instr & 0x7f
    val rd = (instr >> 7) & 0x01f
    val rs1 = (instr >> 15) & 0x01f
    val rs2 = (instr >> 20) & 0x01f
    val funct3 = (instr >> 12) & 0x07
    val funct7 = (instr >> 25) & 0x03f

    /**
     * Immediate generation is a little bit elaborated,
     * but shall give smaller multiplexers in the hardware.
     */
    def genImm() = {

      val instrType: InstrType = opcode match {
        case AluImm => I
        case Alu => R
        case Branch => SB
        case Load => I
        case Store => S
        case Lui => U
        case AuiPc => U
        case Jal => UJ
        case JalR => I
        case ECall => I
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
      val imm0 = instrType match {
        case I => instr20
        case S => instr7
        case _ => 0
      }
      val imm4_1 = instrType match {
        case I => instr24_21
        case U => 0
        case UJ => instr24_21
        case _ => instr11_8
      }
      val imm10_5 = if (instrType == U) 0 else instr30_25
      val imm11 = instrType match {
        case SB => instr7
        case U => 0
        case UJ => instr20
        case _ => instr31
      }
      val imm19_12 = if (instrType == U || instrType == UJ) instr19_12 else sext8
      val imm31_20 = if (instrType == U) instr31_20 else sext12

      // now glue together
      (imm31_20 << 20) | (imm19_12 << 12) | (imm11 << 11) |
        (imm10_5 << 5) | (imm4_1 << 1) | imm0
    }

    val imm = genImm()

    // single bit on extended function - this is not nice
    val sraSub = funct7 == SRA_SUB && (opcode == Alu || (opcode == AluImm && funct3 == SRL_SRA))

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
      val addr = ((base + displ) & 0xfffff) // 1 MB wrap around
      val data = mem(addr >>> 2)
      funct3 match {
        case LSB => (((data >> (8 * (addr & 0x03))) & 0xff) << 24) >> 24
        case LSH => (((data >> (8 * (addr & 0x03))) & 0xffff) << 16) >> 16
        case LSW => data
        case LBU => (data >> (8 * (addr & 0x03))) & 0xff
        case LHU => (data >> (8 * (addr & 0x03))) & 0xffff
      }
    }

    def store(funct3: Int, base: Int, displ: Int, value: Int): Unit = {
      val addr = ((base + displ) & 0xfffff) // 1 MB wrap around
      val wordAddr = addr >>> 2
      funct3 match {
        case LSB => {
          val mask = (addr & 0x03) match {
            case 0 => 0xffffff00
            case 1 => 0xffff00ff
            case 2 => 0xff00ffff
            case 3 => 0x00ffffff
          }
          mem(wordAddr) = (mem(wordAddr) & mask) | ((value & 0xff) << (8 * (addr & 0x03)))
        }
        case LSH => {
          val mask = (addr & 0x03) match {
            case 0 => 0xffff0000
            case 2 => 0x0000ffff
          }
          mem(wordAddr) = (mem(wordAddr) & mask) | ((value & 0xffff) << (8 * (addr & 0x03)))
        }
        case LSW => {
          // very primitive IO simulation
          if (addr == 0xf0000000) {
            println("out: " + value.toChar)
          } else {
            mem(wordAddr) = value
          }
        }
      }
    }

    def ecall(): Int = {
      imm & 0xfff match {
        case 0xf10 => 0 // mhartid
        case 0x000 =>
          // test x28 for test condition: 1 = ok
          if (reg(28) == 1) {
            println("Test passed")
          } else {
            println("Test failed with return code " + reg(1))
          }
          run = false
          0
        case _ => 0 // this gets us around _start in the test cases
      }
    }

    // read register file
    val rs1Val = reg(rs1)
    val rs2Val = reg(rs2)
    // next pc
    val pcNext = pc + 4

    printf("     pc: %04x instr: %08x ", pc, instr)

    // Execute the instruction and return a tuple for the result:
    //   (ALU result, writeBack, next PC)
    val result = opcode match {
      case AluImm => (alu(funct3, sraSub, rs1Val, imm), true, pcNext)
      case Alu => (alu(funct3, sraSub, rs1Val, rs2Val), true, pcNext)
      case Branch => (0, false, if (compare(funct3, rs1Val, rs2Val)) pc + imm else pcNext)
      case Load => (load(funct3, rs1Val, imm), true, pcNext)
      case Store =>
        store(funct3, rs1Val, imm, rs2Val); (0, false, pcNext)
      case Lui => (imm, true, pcNext)
      case AuiPc => (pc + imm, true, pcNext)
      case Jal => (pc + 4, true, pc + imm)
      case JalR => (pc + 4, true, (rs1Val + imm) & 0xfffffffe)
      case Fence => (0, false, pcNext)
      case ECall => (ecall(), true, pcNext)
      case _ => throw new Exception("Opcode " + opcode + " not (yet) implemented")
    }

    if (rd != 0 && result._2) {
      reg(rd) = result._1
    }

    val oldPc = pc
    pc = result._3

    iCnt += 1

    println(pc + " " + stop)
    pc != oldPc && run && pc < stop // detect endless loop or go beyond code to stop simulation
  }

  var cont = true
  while (cont) {
    cont = execute(mem(pc >> 2))
    print("regs: ")
    reg.foreach(printf("%08x ", _))
    println
  }

}

object SimRV {

  def main(args: Array[String]): Unit = {

    val mem = new Array[Int](1024 * 256) // 1 MB, also check masking in load and store

    val (code, start) =
      if (args.isEmpty) {
        // No program given, do something very minimal
        (Array(0x00200093, //	addi x1 x0 2
          0x00300113, //	addi x2 x0 3
          0x002081b3 // add x3 x1 x2
          ), 0)
      } else if (args(0).endsWith(".bin")) {
        (Util.readBin(args(0)), 0)
      } else if (args(0).endsWith(".hex")) {
        (Util.readHex(args(0)), 0x200)
      } else {
        throw new Exception("Unknown file extension")
      }

    for (i <- 0 until code.length) {
      mem(i) = code(i)
    }

    val stop = start + code.length * 4

    new SimRV(mem, start, stop)
  }
}