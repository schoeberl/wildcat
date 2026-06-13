/*
 * Copyright (c) 2015-2017, DTU
 * Simplified BSD License
 */

/*
 * A simple ISA simulator of RISC-V.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 */

package wildcat.isasim

import net.fornwall.jelf.ElfFile

import wildcat.Opcode._
import wildcat.AluFunct3._
import wildcat.AluFunct7._
import wildcat.BranchFunct3._
import wildcat.LoadStoreFunct3._
import wildcat.CSRFunct3._
import wildcat.InstrType._
import wildcat.CSR._
import wildcat.Util

class SimRV(mem: Array[Int], start: Int, stop: Int, linux: Boolean = false) {
  import SimRV._

  // That's the state of the processor.
  // That's it, nothing else (except memory ;-)
  var pc = start // RISC-V tests start at 0x200
  var reg = new Array[Int](32)
  reg(0) = 0

  // Reservation state for LR/SC
  var reservationValid = false
  var reservationAddr = 0

  // M-mode CSRs
  private var mstatus: Int = 0 // Machine status register
  private var mie: Int = 0 // Machine interrupt-enable register
  private var mtvec: Int = 0 // Machine trap-handler base address
  private var mscratch: Int = 0 // Machine scratch register
  private var mepc: Int = 0 // Machine exception program counter
  private var mcause: Int = 0 // Machine trap cause
  private var mtval: Int = 0 // Machine trap value
  private var mip: Int = 0 // Machine interrupt pending

  private var currentPriv: Int = 3 // 3 = M-mode at reset

  // stop on a test end
  var run = true;

  // some statistics
  var instrCnt: Long = 0L

  // Memory-mapped UART + CLINT, only active in Linux mode.
  private val mmio = new Mmio(linux)

  // Translate a physical address into an index into the memory array
  private def memIdx(addr: Int): Int = {
    val idx = (addr - start) >>> 2
    if (idx < 0 || idx >= mem.length) {
      throw new RuntimeException(
        f"RAM access out of bounds: addr=0x$addr%08x (idx=$idx, mem.length=${mem.length})"
      )
    }
    idx
  }

  def execute(instr: Int): Boolean = {
    val oldPc = pc

    // Do some decoding: extraction of decoded fields
    val opcode = instr & 0x7f
    val rd = (instr >> 7) & 0x01f
    val rs1 = (instr >> 15) & 0x01f
    val rs2 = (instr >> 20) & 0x01f
    val funct3 = (instr >> 12) & 0x07
    val funct7 = (instr >> 25) & 0x07f  // Extended to 7 bits for AMO
    val aq = (instr >> 26) & 0x01       // Acquire bit
    val rl = (instr >> 25) & 0x01       // Release bit
    val csrAddr = (instr >> 20) & 0xfff // CSR address

    /**
     * Immediate generation is a little bit elaborated,
     * but shall give smaller multiplexers in the hardware.
     */
    def genImm() = {

      val instrType: InstrType = opcode match {
        case AluImm => I
        case Alu => R
        case Branch => SBT
        case Load => I
        case Store => S
        case Lui => U
        case AuiPc => U
        case Jal => UJ
        case JalR => I
        case System => I
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
        case SBT => instr7
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
    val sraSub = funct7 == SRA_SUB && (opcode == Alu || (opcode == AluImm && funct3 == F3_SRL_SRA))

    def alu(funct3: Int, sraSub: Boolean, op1: Int, op2: Int): Int = {
      val shamt = op2 & 0x1f

      funct3 match {
        case F3_ADD_SUB => if (sraSub) op1 - op2 else op1 + op2
        case F3_SLL => op1 << shamt
        case F3_SLT => if (op1 < op2) 1 else 0
        case F3_SLTU => if ((op1 < op2) ^ (op1 < 0) ^ (op2 < 0)) 1 else 0
        case F3_XOR => op1 ^ op2
        case F3_SRL_SRA => if (sraSub) op1 >> shamt else op1 >>> shamt
        case F3_OR => op1 | op2
        case F3_AND => op1 & op2
      }
    }

    def mulDiv(funct3: Int, op1: Int, op2: Int): Int = {
      val a = op1.toLong
      val b = op2.toLong
      val au = op1.toLong & 0xffffffffL
      val bu = op2.toLong & 0xffffffffL
      funct3 match {
        case F3_MUL    => ((a * b) & 0xffffffffL).toInt
        case F3_MULH   => ((a * b) >> 32).toInt
        case F3_MULHSU => ((a * bu) >> 32).toInt
        case F3_MULHU  => ((au * bu) >> 32).toInt
        case F3_DIV    => if (b == 0) -1 else (a / b).toInt
        case F3_DIVU   => if (bu == 0) -1 else (au / bu).toInt
        case F3_REM    => if (b == 0) a.toInt else (a % b).toInt
        case F3_REMU   => if (bu == 0) au.toInt else (au % bu).toInt
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
      val addr = base + displ

      // Memory-mapped IO (UART + CLINT)
      if (mmio.isMmio(addr)) return mmio.load(addr, instrCnt)

      // RAM
      val data = mem(memIdx(addr))
      funct3 match {
        case LB => (((data >> (8 * (addr & 0x03))) & 0xff) << 24) >> 24
        case LH => (((data >> (8 * (addr & 0x03))) & 0xffff) << 16) >> 16
        case LW => data
        case LBU => (data >> (8 * (addr & 0x03))) & 0xff
        case LHU => (data >> (8 * (addr & 0x03))) & 0xffff
      }
    }

    def store(funct3: Int, base: Int, displ: Int, value: Int): Unit = {
      val addr = base + displ

      // Memory-mapped IO (UART + CLINT)
      if (mmio.isMmio(addr)) {
        mmio.store(funct3, addr, value)
        return
      }

      // RAM
      val wordAddr = memIdx(addr)

      // Any store should invalidate reservations to the same address
      if (reservationValid && wordAddr == (reservationAddr >>> 2) - (start >>> 2)) {
        reservationValid = false
      }

      funct3 match {
        case SB => {
          val mask = (addr & 0x03) match {
            case 0 => 0xffffff00
            case 1 => 0xffff00ff
            case 2 => 0xff00ffff
            case 3 => 0x00ffffff
          }
          mem(wordAddr) = (mem(wordAddr) & mask) | ((value & 0xff) << (8 * (addr & 0x03)))
        }
        case SH => {
          val mask = (addr & 0x03) match {
            case 0 => 0xffff0000
            case 2 => 0x0000ffff
          }
          mem(wordAddr) = (mem(wordAddr) & mask) | ((value & 0xffff) << (8 * (addr & 0x03)))
        }
        case SW => {
          // very primitive IO simulation
          if (addr == 0xf0000004) {
            println("out: " + value.toChar)
          } else {
            mem(wordAddr) = value
          }
        }
      }
    }

    // CSR operand selection (used only by SYSTEM/CSR ops)
    val isImm: Boolean = (funct3 & 0x4) != 0 // CSRR*I have bit 2 set
    val src: Int = if (isImm) rs1 else reg(rs1) // 5-bit zero-ext imm OR reg

    // SYSTEM opcode: separate priv instructions (funct3=0) from CSR ops (funct3!=0)
    def systemOp(): (Int, Boolean, Int) = {
      val pcNext = pc + 4
      if (funct3 == 0) {
        csrAddr match { // I-imm field [31:20] selects the priv instruction
          case 0x000 => // ECALL
            // Test programs run bare-metal (no trap handler), so ECALL ends the run.
            if (linux) {
              val cause = if (currentPriv == 3) 11 else 8
              takeTrap(cause, pc, 0)
            } else {
              run = false
            }
            (0, false, pc) // if trapped, takeTrap set pc = mtvec base
          case 0x001 => // EBREAK
            if (linux) takeTrap(3, pc, pc) else run = false
            (0, false, pc)
          case 0x105 => // WFI — no-op
            (0, false, pcNext)
          case 0x302 => // MRET — return from trap
            val mpie = (mstatus & MSTATUS_MPIE) >>> 4 // bit 7 -> bit 3
            mstatus = (mstatus & ~MSTATUS_MIE) | mpie // MIE <- MPIE
            mstatus |= MSTATUS_MPIE // MPIE <- 1
            currentPriv = (mstatus >>> 11) & 0x3 // priv <- MPP
            (0, false, mepc)
          case _ =>
            Console.err.println(
              f"Unknown SYSTEM f3=0 imm12=0x$csrAddr%03x at pc=0x$pc%08x — treating as nop"
            )
            (0, false, pcNext)
        }
      } else {
        // CSR read/write
        (csrOp(), true, pcNext)
      }
    }

    def applyCsrWrite(old: Int, src: Int, funct3: Int): Int = funct3 match {
      case CSRRW | CSRRWI => src        // write:  new = src
      case CSRRS | CSRRSI => old | src  // set:    new = old | src
      case CSRRC | CSRRCI => old & ~src // clear:  new = old & ~src
      case _ => old                     // shouldn't happen
    }

    def csrRead(addr: Int): Int = addr match {
      case 0x300 => mstatus
      case 0x301 => WILDCAT_MISA
      case 0x304 => mie
      case 0x305 => mtvec
      case 0x340 => mscratch
      case 0x341 => mepc
      case 0x342 => mcause
      case 0x343 => mtval
      case 0x344 => mip
      case 0xf12 => WILDCAT_MARCHID
      case _ => 0 // mvendorid/mimpid/mhartid or unknown return 0
    }

    def csrWrite(addr: Int, value: Int): Unit = addr match {
      case 0x300 => mstatus = value
      case 0x304 => mie = value
      case 0x305 => mtvec = value
      case 0x340 => mscratch = value
      case 0x341 => mepc = value
      case 0x342 => mcause = value
      case 0x343 => mtval = value
      case 0x344 => mip = value
      case _ => () // RO CSRs and unknowns get dropped
    }

    def csrOp(): Int = {
      val doWrite: Boolean = funct3 match {
        case CSRRW | CSRRWI => true
        case CSRRS | CSRRSI | CSRRC | CSRRCI => src != 0
        case _ => false
      }
      val old = csrRead(csrAddr)
      if (doWrite) csrWrite(csrAddr, applyCsrWrite(old, src, funct3))
      old
    }

    def atomic(funct5: Int, addr: Int, rs2Val: Int): (Int, Boolean) = {
      if ((addr & 0x3) != 0) {
        throw new Exception(f"Misaligned atomic address: 0x${addr}%08x")
      }
      val wordAddr = memIdx(addr)
      val oldValue = mem(wordAddr)
      
      funct5 match {
        case 0x02 => { // LR.W
          reservationValid = true
          reservationAddr = addr
          (oldValue, true)
        }
        case 0x03 => { // SC.W
          if (reservationValid && reservationAddr == addr) {
            mem(wordAddr) = rs2Val
            reservationValid = false
            (0, true) // Success: return 0
          } else {
            (1, true) // Failure: return non-zero
          }
        }
        case 0x01 => { // AMOSWAP.W
          mem(wordAddr) = rs2Val
          (oldValue, true)
        }
        case 0x00 => { // AMOADD.W
          val result = oldValue + rs2Val
          mem(wordAddr) = result
          (oldValue, true)
        }
        case 0x04 => { // AMOXOR.W
          val result = oldValue ^ rs2Val
          mem(wordAddr) = result
          (oldValue, true)
        }
        case 0x0C => { // AMOAND.W
          val result = oldValue & rs2Val
          mem(wordAddr) = result
          (oldValue, true)
        }
        case 0x08 => { // AMOOR.W
          val result = oldValue | rs2Val
          mem(wordAddr) = result
          (oldValue, true)
        }
        case _ => (0, false)
      }
    }

    // read register file
    val rs1Val = reg(rs1)
    val rs2Val = reg(rs2)
    // next pc
    val pcNext = pc + 4

    // Debug output for atomic instructions
    if (opcode == 0x2f) {
      // println(f"Atomic instruction at pc=0x${pc}%08x: rs1=x${rs1}%d(0x${rs1Val}%08x) rs2=x${rs2}%d(0x${rs2Val}%08x) rd=x${rd}%d funct7=0x${funct7}%02x")
    }

    // Execute the instruction and return a tuple for the result:
    //   (ALU result, writeBack, next PC)
    val result = opcode match {
      case Amo => { // AMO - Atomic Memory Operations
        val addr = rs1Val
        if (funct3 != 0x2) {
          throw new Exception(f"Invalid funct3 for atomic operation: 0x${funct3}%x")
        }
        val funct5 = (funct7 >> 2) & 0x1f  // Get bits [31:27] for funct5
        val (value, success) = atomic(funct5, addr, rs2Val)
        (value, success, pcNext)
      }
      case AluImm => (alu(funct3, sraSub, rs1Val, imm), true, pcNext)
      case Alu if funct7 == 0x01 =>
        (mulDiv(funct3, rs1Val, rs2Val), true, pcNext)
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
      case System => systemOp()
      case _ => throw new Exception("Opcode " + opcode + " at " + pc + " not (yet) implemented")
    }

    // External interference simulation (uncomment for testing)
    // if (scala.util.Random.nextInt(100) < 5) { // 5% chance
    //   reservationValid = false
    // }

    if (rd != 0 && result._2) {
      reg(rd) = result._1
    }

    pc = result._3

    instrCnt += 1

    pc != oldPc && run && pc < stop // detect endless loop or go beyond code to stop simulation
  }

  // Interrupt and trap handling
  def updateMip(): Unit = {
    // Timer: set MTIP whenever mtime >= mtimecmp
    if (mmio.timerPending(instrCnt)) mip |= MIP_MTIP else mip &= ~MIP_MTIP
  }

  def pendingInterruptCause(): Option[Int] = {
    if ((mstatus & MSTATUS_MIE) == 0) return None
    val active = mip & mie
    if ((active & MIP_MTIP) != 0) Some(CAUSE_M_TIMER) // We only ever expect timer interrupts
    else if ((active & MIP_MSIP) != 0) Some(CAUSE_M_SOFTWARE)
    else if ((active & MIP_MEIP) != 0) Some(CAUSE_M_EXTERNAL)
    else None
  }

  def takeTrap(cause: Int, epc: Int, tval: Int): Unit = {
    mepc = epc
    mcause = cause
    mtval = tval
    // Save current privilege into MPP, save MIE into MPIE, clear MIE.
    val mpie = (mstatus & MSTATUS_MIE) << 4 // bit 3 -> bit 7
    val mpp = currentPriv << 11 // 0 = U, 3 = M
    mstatus = (mstatus & ~(MSTATUS_MIE | MSTATUS_MPIE | MSTATUS_MPP)) | mpie | mpp
    currentPriv = 3 // trap always enters M-mode

    val base = mtvec & ~0x3
    val mode = mtvec & 0x3
    pc =
      if (mode == 1 && (cause & 0x80000000) != 0)
        base + 4 * (cause & 0x7fffffff)
      else base
  }

  var cont = true
  while (cont) {
    updateMip() // update pending interrupts before each instruction
    pendingInterruptCause() match {
      case Some(c) => takeTrap(c, pc, 0) // tval = 0 for interrupts
      case None    =>
        try {
          val instr = mem(memIdx(pc))
          cont = execute(instr)
        } catch {
          case e: Throwable =>
            Console.err.println(
              f"\n*** SIM HALTED at pc=0x$pc%08x after $instrCnt steps"
            )
            Console.err.println(
              s"***   reason: ${e.getClass.getSimpleName}: ${e.getMessage}"
            )
            Console.err.println("***   registers:")
            for (i <- 0 until 32) {
              Console.err.print(f"x$i%02d=0x${reg(i)}%08x ")
              if ((i & 3) == 3) Console.err.println()
            }
            cont = false
        }
    }
  }
  Console.err.println(f"Simulation ended. pc=0x$pc%08x, steps=$instrCnt")
}

object SimRV {

  def runSimRV(file: String, linux: Boolean = false) = {
    val (code, start) = Util.getCode(file)

    val (memBase, memSize) =
      if (linux) (0x80000000, 48 * 1024 * 1024) // 48 MB
      else (start, code.length * 4)

    val memWords = memSize / 4
    val maxAddr = memSize - 1

    val mem = new Array[Int](memWords)

    for (i <- 0 until code.length) {
      mem(i) = code(i)
    }

    val stop = memBase + memSize

    // TODO: do we really want ot ba able to start at an arbitrary address?
    // Read in RV spec
    val sim = new SimRV(mem, memBase, stop, linux)
    sim
  }

  def main(args: Array[String]): Unit = {
    val linux = args.contains("--linux")
    val files = args.filter(_ != "--linux") // Args with flags removed i.e. just program file

    if (files.isEmpty) {
      Console.err.println("usage: SimRV [--linux] <program-file>")
      sys.exit(1)
    }

    runSimRV(files(0), linux)
  }
}
