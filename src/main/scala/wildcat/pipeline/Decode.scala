package wildcat.pipeline

import chisel3._
import chisel3.util._

import wildcat.Opcode._
import wildcat.InstrType._
import wildcat.AluType._
import wildcat.AluFunct3._

import wildcat.common.Functions._

class Decode extends Module {
  val io = IO(new Bundle{
    val fedec = Input(new FeDec())
    val decex = Output(new DecEx())
    val wbdec = Input(new WbDec())
    val stall = Input(Bool())
  })


  val instr = io.fedec.instr
  // pipe registers
  val instrReg = RegInit(0x00000033.U) // nop on reset
  val pcReg = RegInit(0.U)
  when (!io.stall) {
    instrReg := instr
    pcReg := io.fedec.pc
  }

  // Register file
  val regMem = SyncReadMem(32, UInt(32.W), SyncReadMem.WriteFirst)

  val rs1 = instr(19, 15)
  val rs2 = instr(24, 20)
  val rd = instr(11, 7)

  /*
  val rs1Val = Mux(rs1 =/= 0.U, regMem.read(rs1), 0.U)
  val rs2Val = Mux(rs2 =/= 0.U, regMem.read(rs2), 0.U)
  when(io.wbdec.valid) {
    regMem.write(io.wbdec.regNr, io.wbdec.data)
  }
  */

  // The register version needs an input pipe register
  val rs1Reg = RegNext(rs1)
  val rs2Reg = RegNext(rs2)
  val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
  val rs1Val = Mux(rs1Reg =/= 0.U, regs(rs1Reg), 0.U)
  val rs2Val = Mux(rs2Reg =/= 0.U, regs(rs2Reg), 0.U)
  when(io.wbdec.valid) {
    regs(io.wbdec.regNr) := io.wbdec.data
  }
  for (i <- 0 until 5) {
    // printf("reg %d: %x ", i.U, regs(i))
  }
  printf("\n")

  // Decode
  val opcode = instrReg(6, 0)
  val func3 = instrReg(14, 12)
  val func7 = instrReg(31, 25)
  val isImm = WireDefault(false.B)

  val instrType = WireDefault(R.id.U)
  switch(opcode) {
    is(AluImm.U) {
      instrType := I.id.U
      isImm := true.B
    }
    is(Alu.U) {
      instrType := R.id.U
    }
    is(Branch.U) {
      instrType := SB.id.U
    }
    is(Load.U) {
      instrType := I.id.U
    }
    is(Store.U) {
      instrType := S.id.U
    }
    is(Lui.U) {
      instrType := U.id.U
    }
    is(AuiPc.U) {
      instrType := U.id.U
    }
    is(Jal.U) {
      instrType := UJ.id.U
    }
    is(JalR.U) {
      instrType := I.id.U
    }
    is(ECall.U) {
      instrType := I.id.U
    }
  }


  val imm = genImm(instrReg, instrType)
  /*
  // Immediates
  val imm = Wire(SInt(32.W))
  imm := instrReg(31, 20).asSInt

  switch(instrType) {
    is(I.id.U) {
      imm := (Fill(20, instrReg(31)) ## instrReg(31, 20)).asSInt
    }
    is(S.id.U) {
      imm := (Fill(20, instrReg(31)) ## instrReg(31, 25) ## instrReg(11, 7)).asSInt
    }
    is(SB.id.U) {
      imm := (Fill(19, instrReg(31)) ## instrReg(7) ## instrReg(30, 25) ## instrReg(11, 8) ## 0.U).asSInt
    }
    is(U.id.U) {
      imm := (instrReg(31, 12) ## Fill(12, 0.U)).asSInt
    }
    is(UJ.id.U) {
      imm := (Fill(11, instrReg(31)) ## instrReg(19, 12) ## instrReg(20) ## instrReg(30, 21) ## 0.U).asSInt
    }
  }

   */

  // Decode ALU control signals
  // could be done nicer
  val aluOp = WireDefault(ADD.id.U)
  switch(func3) {
    is(F3_ADD_SUB.U) {
      aluOp := ADD.id.U
      when(opcode =/= AluImm.U && func7 =/= 0.U) {
        aluOp := SUB.id.U
      }
    }
    is(F3_SLL.U) {
      aluOp := SLL.id.U
    }
    is(F3_SLT.U) {
      aluOp := SLT.id.U
    }
    is(F3_SLTU.U) {
      aluOp := SLTU.id.U
    }
    is(F3_XOR.U) {
      aluOp := XOR.id.U
    }
    is(F3_SRL_SRA.U) {
      when(func7 === 0.U) {
        aluOp := SRL.id.U
      }.otherwise {
        aluOp := SRA.id.U
      }
    }
    is(F3_OR.U) {
      aluOp := OR.id.U
    }
    is(F3_AND.U) {
      aluOp := AND.id.U
    }
  }


  // Address calculation for load/store (if 3 or 4 stages pipeline)
  io.decex.pc := pcReg
  io.decex.aluOp := aluOp
  io.decex.rs1 := instrReg(19, 15)
  io.decex.rs2 := instrReg(24, 20)
  io.decex.rd := instrReg(11, 7)
  io.decex.rs1Val := rs1Val
  io.decex.rs2Val := rs2Val
  io.decex.imm := imm
  io.decex.isImm := isImm
  //printf("%x: instruction: %x rs1: %x rs2: %x rd: %x imm: %x\n", io.decex.pc, io.decex.aluOp, io.decex.rs1, io.decex.rs2, io.decex.rd, io.decex.imm)
}
