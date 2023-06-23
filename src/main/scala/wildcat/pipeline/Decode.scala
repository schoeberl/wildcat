package wildcat.pipeline

import chisel3._
import chisel3.util._

import wildcat.Opcode._
import wildcat.InstrType._
import wildcat.AluType._
import wildcat.AluFunct3._

class Decode extends Module {
  val io = IO(new DecodeIO)


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

  val rs1Val = Mux(rs1 =/= 0.U, regMem.read(rs1), 0.U)
  val rs2Val = Mux(rs2 =/= 0.U, regMem.read(rs2), 0.U)
  when(io.wbdec.valid) {
    regMem.write(io.wbdec.regNr, io.wbdec.data)
  }

  // Decode
  val opcode = instrReg(6, 0)
  val func3 = instrReg(14, 12)
  val func7 = instrReg(31, 25)

  val instrType = WireDefault(R.id.U)
  switch(opcode) {
    is(AluImm.U) {
      instrType := I.id.U
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

  // Decode ALU control signals
val aluOp = WireDefault(ADD.id.U)
  switch(func3) {
    is(F3_ADD_SUB.U) {
      when(func7 === 0.U) {
        aluOp := ADD.id.U
      }.otherwise {
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
  // TODO: add stall signal
  val rs1Reg = Reg(UInt(5.W))
  val rs2Reg = Reg(UInt(5.W))
  val rdReg = Reg(UInt(5.W))
  when(!io.stall) {
    rs1Reg := rs1
    rs2Reg := rs2
    rdReg := rd
  }
  io.decex.rs1 := rs1Reg
  io.decex.rs2 := rs2Reg
  io.decex.rd := rdReg
  io.decex.imm := imm
  printf("%x: instruction: %x rs1: %x rs2: %x rd: %x imm: %x\n", io.decex.pc, io.decex.aluOp, io.decex.rs1,
    io.decex.rs2, io.decex.rd, io.decex.imm)
}
