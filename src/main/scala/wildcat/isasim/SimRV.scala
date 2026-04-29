/*
 * Copyright (c) 2015-2017, DTU
 * Simplified BSD License
 */

/*
 * A simple ISA simulator of RISC-V.
 *
 * Author: Martin Schoeberl (martin@jopdesign.com)
 *
 * Extended for Linux boot:
 *   - RAM relocated to MEM_BASE (0x80000000)
 *   - Minimal CLINT at 0x02000000 (mtime, mtimecmp, msip)
 *   - 8250 UART at 0x10000000 (byte-wise)
 *   - SYSTEM opcode properly decoded (ecall/ebreak/mret/sret/wfi/sfence.vma)
 *   - Top-level exception catch with diagnostic
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

class SimRV(mem: Array[Int], start: Int, stop: Int) {
  import SimRV._

  // Processor state
  var pc = start
  var reg = new Array[Int](32)
  reg(0) = 0

  // LR/SC reservation
  var reservationValid = false
  var reservationAddr = 0

  // CLINT timer state
  private var instCount: Long = 0L
  private var mtimecmp: Long = Long.MaxValue // no pending timer by default

  // CLINT Registers
  private var mstatus: Int = 0
  private var mie: Int = 0
  private var mtvec: Int = 0
  private var mscratch: Int = 0
  private var mepc: Int = 0
  private var mcause: Int = 0
  private var mtval: Int = 0
  private var mip: Int = 0

  def plicHasPending: Boolean = false
  private var currentPriv: Int = 3 // 3 = M-mode at reset

  // MOVE TO DEFINES
  // mstatus bits (RV32)
  val MSTATUS_MIE = 1 << 3
  val MSTATUS_MPIE = 1 << 7
  val MSTATUS_MPP = 3 << 11 // 2 bits; always 0b11 (M-mode) for us

  // mie/mip bits
  val MIP_MSIP = 1 << 3
  val MIP_MTIP = 1 << 7
  val MIP_MEIP = 1 << 11

  // mcause interrupt codes (with MSB=1)
  val CAUSE_M_TIMER = 0x80000007
  val CAUSE_M_EXTERNAL = 0x8000000b
  val CAUSE_M_SOFTWARE = 0x80000003
  // END OF DEFINES

  // halt flag
  var run = true

  // UART RX: background reader pushes stdin bytes into a queue.
  private val rxQueue =
    new java.util.concurrent.ConcurrentLinkedQueue[Integer]()
  private val rxThread = new Thread(() => {
    val in = java.lang.System.in
    var b = in.read()
    while (b >= 0) { rxQueue.offer(b & 0xff); b = in.read() }
  }, "uart-rx")
  rxThread.setDaemon(true)
  rxThread.start()

  // Translate a physical address into an index into `mem` (word array).
  // Throws if the address is outside RAM.
  @inline private def memIdx(addr: Int): Int = {
    // (addr - MEM_BASE) is computed in 2's-complement Int and is correct
    // as an unsigned offset; >>> 2 gives the word index.
    val idx = (addr - MEM_BASE) >>> 2
    if (idx < 0 || idx >= mem.length) {
      throw new RuntimeException(
        f"RAM access out of bounds: addr=0x$addr%08x (idx=$idx, mem.length=${mem.length})"
      )
    }
    idx
  }

  // MMIO region predicates
  @inline private def isUart(addr: Int): Boolean =
    addr >= 0x10000000 && addr < 0x10000100
  @inline private def isClint(addr: Int): Boolean =
    addr >= 0x02000000 && addr < 0x02010000

  // CLINT load
  private def clintLoad(addr: Int): Int = {
    val off = addr - 0x02000000
    off match {
      case 0xbff8 => (instCount & 0xffffffffL).toInt // mtime lo
      case 0xbffc => ((instCount >>> 32) & 0xffffffffL).toInt // mtime hi
      case 0x4000 => (mtimecmp & 0xffffffffL).toInt // mtimecmp lo
      case 0x4004 => ((mtimecmp >>> 32) & 0xffffffffL).toInt // mtimecmp hi
      case 0x0000 => 0 // msip (hart 0)
      case _      => 0
    }
  }

  // CLINT store
  private def clintStore(addr: Int, value: Int): Unit = {
    val off = addr - 0x02000000
    off match {
      case 0x4000 =>
        mtimecmp =
          (mtimecmp & 0xffffffff00000000L) | (value.toLong & 0xffffffffL)
      case 0x4004 =>
        mtimecmp =
          (mtimecmp & 0x00000000ffffffffL) | ((value.toLong & 0xffffffffL) << 32)
      case _ => () // msip + everything else: ignore
    }
  }

  def execute(instr: Int): Boolean = {
    val oldPc = pc

    val opcode = instr & 0x7f
    val rd = (instr >> 7) & 0x01f
    val rs1 = (instr >> 15) & 0x01f
    val rs2 = (instr >> 20) & 0x01f
    val funct3 = (instr >> 12) & 0x07
    val funct7 = (instr >> 25) & 0x07f

    def genImm() = {
      val instrType: InstrType = opcode match {
        case AluImm => I
        case Alu    => R
        case Branch => SBT
        case Load   => I
        case Store  => S
        case Lui    => U
        case AuiPc  => U
        case Jal    => UJ
        case JalR   => I
        case System => I
        case _      => R
      }
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

      val imm0 = instrType match {
        case I => instr20
        case S => instr7
        case _ => 0
      }
      val imm4_1 = instrType match {
        case I  => instr24_21
        case U  => 0
        case UJ => instr24_21
        case _  => instr11_8
      }
      val imm10_5 = if (instrType == U) 0 else instr30_25
      val imm11 = instrType match {
        case SBT => instr7
        case U   => 0
        case UJ  => instr20
        case _   => instr31
      }
      val imm19_12 =
        if (instrType == U || instrType == UJ) instr19_12 else sext8
      val imm31_20 = if (instrType == U) instr31_20 else sext12

      (imm31_20 << 20) | (imm19_12 << 12) | (imm11 << 11) |
        (imm10_5 << 5) | (imm4_1 << 1) | imm0
    }

    val imm = genImm()

    val sraSub =
      funct7 == SRA_SUB && (opcode == Alu || (opcode == AluImm && funct3 == F3_SRL_SRA))

    def alu(funct3: Int, sraSub: Boolean, op1: Int, op2: Int): Int = {
      val shamt = op2 & 0x1f
      funct3 match {
        case F3_ADD_SUB => if (sraSub) op1 - op2 else op1 + op2
        case F3_SLL     => op1 << shamt
        case F3_SLT     => if (op1 < op2) 1 else 0
        case F3_SLTU    => if ((op1 < op2) ^ (op1 < 0) ^ (op2 < 0)) 1 else 0
        case F3_XOR     => op1 ^ op2
        case F3_SRL_SRA => if (sraSub) op1 >> shamt else op1 >>> shamt
        case F3_OR      => op1 | op2
        case F3_AND     => op1 & op2
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

    def compare(funct3: Int, op1: Int, op2: Int): Boolean = funct3 match {
      case BEQ  => op1 == op2
      case BNE  => !(op1 == op2)
      case BLT  => op1 < op2
      case BGE  => op1 >= op2
      case BLTU => (op1 < op2) ^ (op1 < 0) ^ (op2 < 0)
      case BGEU => op1 == op2 || ((op1 > op2) ^ (op1 < 0) ^ (op2 < 0))
    }

    def load(funct3: Int, base: Int, displ: Int): Int = {
      val addr = base + displ

      // MMIO: UART (byte registers; reg-io-width=1, reg-shift=0)
      if (isUart(addr)) {
        val offset = addr - 0x10000000
        return offset match {
          case 0 => // RBR
            val b = rxQueue.poll(); if (b == null) 0 else b.intValue()
          case 5 => // LSR: THRE | TEMT | DR
            0x60 | (if (rxQueue.isEmpty) 0 else 1)
          case _ => 0x00
        }
      }

      // MMIO: CLINT
      if (isClint(addr)) return clintLoad(addr)

      // RAM
      val data = mem(memIdx(addr))
      funct3 match {
        case LB  => (((data >> (8 * (addr & 0x03))) & 0xff) << 24) >> 24
        case LH  => (((data >> (8 * (addr & 0x03))) & 0xffff) << 16) >> 16
        case LW  => data
        case LBU => (data >> (8 * (addr & 0x03))) & 0xff
        case LHU => (data >> (8 * (addr & 0x03))) & 0xffff
      }
    }

    def store(funct3: Int, base: Int, displ: Int, value: Int): Unit = {
      val addr = base + displ

      // MMIO: UART
      if (isUart(addr)) {
        val offset = addr - 0x10000000
        funct3 match {
          case SB if offset == 0 =>
            if (value == 0x0a || value == 0x0d) {
              println()
            } else {
              print((value & 0xff).toChar)
            }
            Console.out.flush()
          case _ =>
          // writes to IER/FCR/LCR/MCR/SCR — no-op
        }
        return
      }

      // MMIO: CLINT
      if (isClint(addr)) { clintStore(addr, value); return }

      // RAM
      val wordAddr = memIdx(addr)

      if (
        reservationValid && wordAddr == (reservationAddr >>> 2) - (MEM_BASE >>> 2)
      ) {
        // Conservative: any store in RAM invalidates a matching reservation.
        reservationValid = false
      }

      funct3 match {
        case SB =>
          val mask = (addr & 0x03) match {
            case 0 => 0xffffff00
            case 1 => 0xffff00ff
            case 2 => 0xff00ffff
            case 3 => 0x00ffffff
          }
          mem(wordAddr) =
            (mem(wordAddr) & mask) | ((value & 0xff) << (8 * (addr & 0x03)))
        case SH =>
          val mask = (addr & 0x03) match {
            case 0 => 0xffff0000
            case 2 => 0x0000ffff
            case o =>
              throw new RuntimeException(
                f"Misaligned SH at 0x$addr%08x (off=$o)"
              )
          }
          mem(wordAddr) =
            (mem(wordAddr) & mask) | ((value & 0xffff) << (8 * (addr & 0x03)))
        case SW =>
          mem(wordAddr) = value
      }
    }
    // Decoded once per instruction
    val csrAddr: Int = (instr >>> 20) & 0xfff
    val rs1Idx: Int = (instr >>> 15) & 0x1f
    val isImm: Boolean = (funct3 & 0x4) != 0 // CSRR*I have bit 2 set
    val src: Int =
      if (isImm) rs1Idx else reg(rs1Idx) // 5-bit zero-ext imm OR reg

    // SYSTEM opcode: separate priv instructions (funct3=0) from CSR ops (funct3!=0)
    def systemOp(): (Int, Boolean, Int) = {
      val pcNext = pc + 4
      if (funct3 == 0) {
        val f7 = (instr >>> 25) & 0x7f
        val imm12 = (instr >>> 20) & 0xfff
        if (f7 == 0x09 || f7 == 0x0b) {
          // sfence.vma / sinval.vma: no-op (no MMU)
          (0, false, pcNext)
        } else
          imm12 match {
            case 0x000 =>
              // ECALL. Linux nommu without SBI shouldn't execute these.
              // Log and continue instead of halting.
              // Console.err.println(f"ECALL at pc=0x$pc%08x")
              val fromM = ((mstatus >>> 11) & 0x3) == 3
              val cause = if (currentPriv == 3) 11 else 8
              takeTrap(cause, pc, 0)
              (0, false, pc) // takeTrap set pc = mtvec base
            case 0x001 =>
              // EBREAK — treat as fatal with diagnostic
              // Console.err.println(f"EBREAK at pc=0x$pc%08x")
              takeTrap(cause = 3, epc = pc, tval = pc)
              (0, false, pc)
            case 0x105 =>
              // WFI — no-op (we don't deliver interrupts anyway)
              (0, false, pcNext)
            case 0x102 | 0x302 =>
              val mpie = (mstatus & MSTATUS_MPIE) >>> 4 // bit 7 -> bit 3
              mstatus = (mstatus & ~MSTATUS_MIE) | mpie // MIE <- MPIE
              mstatus |= MSTATUS_MPIE // MPIE <- 1
              currentPriv = (mstatus >>> 11) & 0x3 // priv <- MPP
              // Spec also says set MPP <- U (least privileged); harmless to leave for now.
              (0, false, mepc)
            case _ =>
              Console.err.println(
                f"Unknown SYSTEM f3=0 imm12=0x$imm12%03x at pc=0x$pc%08x — treating as nop"
              )
              (0, false, pcNext)
          }
      } else {
        // CSR read/write — return 0 / MARCHID, silently accept writes
        (csrOp(), true, pcNext)
      }
    }

    def applyCsrWrite(old: Int, src: Int, funct3: Int): Int = funct3 match {
      case CSRRW | CSRRWI => src // write:  new = src
      case CSRRS | CSRRSI => old | src // set:    new = old | src
      case CSRRC | CSRRCI => old & ~src // clear:  new = old & ~src
      case _              => old // shouldn't happen
    }

    def csrRead(addr: Int): Int = addr match {
      case 0x300 => mstatus
      case 0x301 => 0x40001100 // misa: MXL=1, I|M|A
      case 0x304 => mie
      case 0x305 => mtvec
      case 0x340 => mscratch
      case 0x341 => mepc
      case 0x342 => mcause
      case 0x343 => mtval
      case 0x344 => mip
      case 0xf11 => 0 // mvendorid
      case 0xf12 => WILDCAT_MARCHID // marchid
      case 0xf13 => 0 // mimpid
      case 0xf14 => 0 // mhartid
      case _     => 0
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
      case _     => () // RO CSRs + unknowns: drop
    }

    def csrOp(): Int = {
      // Per spec: csrrs/csrrc with rs1 == x0, and csrrsi/csrrci with uimm == 0,
      // must NOT perform the write (to avoid read side-effects on some CSRs).
      val doWrite: Boolean = funct3 match {
        case CSRRW | CSRRWI                  => true
        case CSRRS | CSRRSI | CSRRC | CSRRCI => src != 0
        case _                               => false
      }
      val old = csrRead(csrAddr)
      if (doWrite) csrWrite(csrAddr, applyCsrWrite(old, src, funct3))
      old
    }

    def atomic(funct5: Int, addr: Int, rs2Val: Int): (Int, Boolean) = {
      if ((addr & 0x3) != 0) {
        throw new RuntimeException(f"Misaligned atomic address: 0x$addr%08x")
      }
      val wordAddr = memIdx(addr)
      val oldValue = mem(wordAddr)

      funct5 match {
        case 0x02 => // LR.W
          reservationValid = true
          reservationAddr = addr
          (oldValue, true)
        case 0x03 => // SC.W
          if (reservationValid && reservationAddr == addr) {
            mem(wordAddr) = rs2Val
            reservationValid = false
            (0, true)
          } else (1, true)
        case 0x01 => // AMOSWAP
          mem(wordAddr) = rs2Val; (oldValue, true)
        case 0x00 => // AMOADD
          mem(wordAddr) = oldValue + rs2Val; (oldValue, true)
        case 0x04 => // AMOXOR
          mem(wordAddr) = oldValue ^ rs2Val; (oldValue, true)
        case 0x0c => // AMOAND
          mem(wordAddr) = oldValue & rs2Val; (oldValue, true)
        case 0x08 => // AMOOR
          mem(wordAddr) = oldValue | rs2Val; (oldValue, true)
        case 0x10 => // AMOMIN
          mem(wordAddr) = math.min(oldValue, rs2Val); (oldValue, true)
        case 0x14 => // AMOMAX
          mem(wordAddr) = math.max(oldValue, rs2Val); (oldValue, true)
        case 0x18 => // AMOMINU
          val a = oldValue.toLong & 0xffffffffL
          val b = rs2Val.toLong & 0xffffffffL
          mem(wordAddr) = (if (a < b) a else b).toInt
          (oldValue, true)
        case 0x1c => // AMOMAXU
          val a = oldValue.toLong & 0xffffffffL
          val b = rs2Val.toLong & 0xffffffffL
          mem(wordAddr) = (if (a > b) a else b).toInt
          (oldValue, true)
        case _ =>
          Console.err.println(
            f"Unknown AMO funct5=0x$funct5%02x at pc=0x$pc%08x"
          )
          (0, false)
      }
    }

    val rs1Val = reg(rs1)
    val rs2Val = reg(rs2)
    val pcNext = pc + 4

    // Debug output for atomic instructions
    if (opcode == 0x2f) {
      // println(f"Atomic instruction at pc=0x${pc}%08x: rs1=x${rs1}%d(0x${rs1Val}%08x) rs2=x${rs2}%d(0x${rs2Val}%08x) rd=x${rd}%d funct7=0x${funct7}%02x")
    }

    // Execute the instruction and return a tuple for the result:
    //   (ALU result, writeBack, next PC)
    val result = opcode match {
      case 0x2f => // AMO
        val addr = rs1Val
        if (funct3 != 0x2) {
          throw new RuntimeException(
            f"Invalid funct3=0x$funct3%x for AMO at pc=0x$pc%08x"
          )
        }
        val funct5 = (funct7 >> 2) & 0x1f
        val (value, success) = atomic(funct5, addr, rs2Val)
        (value, success, pcNext)

      case AluImm => (alu(funct3, sraSub, rs1Val, imm), true, pcNext)
      case Alu if funct7 == 0x01 =>
        (mulDiv(funct3, rs1Val, rs2Val), true, pcNext)
      case Alu    => (alu(funct3, sraSub, rs1Val, rs2Val), true, pcNext)
      case Branch =>
        (0, false, if (compare(funct3, rs1Val, rs2Val)) pc + imm else pcNext)
      case Load   => (load(funct3, rs1Val, imm), true, pcNext)
      case Store  => store(funct3, rs1Val, imm, rs2Val); (0, false, pcNext)
      case Lui    => (imm, true, pcNext)
      case AuiPc  => (pc + imm, true, pcNext)
      case Jal    => (pc + 4, true, pc + imm)
      case JalR   => (pc + 4, true, (rs1Val + imm) & 0xfffffffe)
      case Fence  => (0, false, pcNext) // fence / fence.i: no-op
      case System => systemOp()
      case _      =>
        Console.err.println(
          f"Unknown opcode 0x$opcode%02x at pc=0x$pc%08x instr=0x$instr%08x — treating as nop"
        )
        (0, false, pcNext)
    }

    if (rd != 0 && result._2) reg(rd) = result._1

    pc = result._3

    // Keep running while:
    //  - execution flag is still set, AND
    //  - PC advanced (guards against a single-instruction self-loop)
    // Note: `pc < stop` using signed comparison is fine because both pc and stop
    // are in the same wrap-around region near 0x80000000.
    pc != oldPc && run && pc < stop
  }

  // Trap Handler
  def updateMip(): Unit = {
    // Timer: set MTIP whenever mtime >= mtimecmp
    if (instCount >= mtimecmp) mip |= MIP_MTIP else mip &= ~MIP_MTIP
    // External: set MEIP whenever any PLIC source is pending+enabled
    if (plicHasPending) mip |= MIP_MEIP else mip &= ~MIP_MEIP
    // MSIP from CLINT msip reg (you currently ignore writes to it — fine for now)
  }

  def pendingInterruptCause(): Option[Int] = {
    if ((mstatus & MSTATUS_MIE) == 0) return None
    val active = mip & mie
    if ((active & MIP_MEIP) != 0) Some(CAUSE_M_EXTERNAL)
    else if ((active & MIP_MSIP) != 0) Some(CAUSE_M_SOFTWARE)
    else if ((active & MIP_MTIP) != 0) Some(CAUSE_M_TIMER)
    else None
  }

  def takeTrap(cause: Int, epc: Int, tval: Int): Unit = {
    mepc = epc
    mcause = cause
    mtval = tval
    // Save current privilege into MPP, save MIE into MPIE, clear MIE.
    val mpie = (mstatus & MSTATUS_MIE) << 4 // bit 3 -> bit 7
    val mpp = currentPriv << 11 // 0 = U, 3 = M
    mstatus =
      (mstatus & ~(MSTATUS_MIE | MSTATUS_MPIE | MSTATUS_MPP)) | mpie | mpp
    currentPriv = 3 // trap always enters M
    val base = mtvec & ~0x3
    val mode = mtvec & 0x3
    pc =
      if (mode == 1 && (cause & 0x80000000) != 0)
        base + 4 * (cause & 0x7fffffff)
      else base
  }

  // -------------------------------------------------------------------------
  // Main execution loop with exception catching so problems are visible.
  // -------------------------------------------------------------------------
  var cont = true
  var steps: Long = 0L
  while (cont) {
    updateMip() // update pending interrupts before each instruction
    pendingInterruptCause() match {
      case Some(c) => takeTrap(c, pc, 0) // tval = 0 for interrupts
      case None    =>
        try {
          val instr = mem(memIdx(pc))
          cont = execute(instr)
          instCount += 1
          steps += 1
        } catch {
          case e: Throwable =>
            Console.err.println(
              f"\n*** SIM HALTED at pc=0x$pc%08x after $steps steps"
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
  Console.err.println(f"Simulation ended. pc=0x$pc%08x, steps=$steps")
}

object SimRV {

  // Physical base of RAM. Must match the kernel's CONFIG_PHYS_RAM_BASE and
  // the `. = 0x80000000;` in boot.ld and the memory@80000000 node in wildcat.dts.
  val MEM_BASE = 0x80000000

  val memSize = 32 // MB
  val memWords = memSize * 1024 * 1024 / 4
  val maxAddr = memSize * 1024 * 1024 - 1

  def runSimRV(file: String) = {
    val mem = new Array[Int](memWords)

    val (code, startOrig) = Util.getCode(file)

    for (i <- 0 until code.length) mem(i) = code(i)

    // boot.bin is a raw binary whose first byte is the bootloader entry
    // (`_start`), linked at MEM_BASE. For ELF inputs the entry comes from
    // the file itself; otherwise we default to MEM_BASE.
    val start =
      if (startOrig == 0 || startOrig == MEM_BASE) MEM_BASE
      else startOrig

    // Allow execution anywhere in RAM (the kernel branches all over 8 MB).
    val stop = MEM_BASE + memSize * 1024 * 1024

    new SimRV(mem, start, stop)
  }

  def main(args: Array[String]): Unit = {
    runSimRV(args(0))
  }
}
