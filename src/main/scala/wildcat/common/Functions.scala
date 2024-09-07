package wildcat.common

import chisel3._
import chisel3.util._
import wildcat.AluFunct3._
import wildcat.AluType._
import wildcat.InstrType._
import wildcat.Opcode._


object Functions {

  def getInstrType(instruction: UInt) = {

    val isImm = WireDefault(false.B)
    val opcode = instruction(6, 0)
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
    (instrType, isImm)
  }

  def getAluOp(instruction: UInt): UInt = {

    val opcode = instruction(6, 0)
    val func3 = instruction(14, 12)
    val func7 = instruction(31, 25)

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
    aluOp
  }
  def getImm(instruction: UInt, instrType: UInt): SInt = {

    val imm = Wire(SInt(32.W))
    imm := instruction(31, 20).asSInt
    switch(instrType) {
      is(I.id.U) {
        imm := (Fill(20, instruction(31)) ## instruction(31, 20)).asSInt
      }
      is(S.id.U) {
        imm := (Fill(20, instruction(31)) ## instruction(31, 25) ## instruction(11, 7)).asSInt
      }
      is(SB.id.U) {
        imm := (Fill(19, instruction(31)) ## instruction(7) ## instruction(30, 25) ## instruction(11, 8) ## 0.U).asSInt
      }
      is(U.id.U) {
        imm := (instruction(31, 12) ## Fill(12, 0.U)).asSInt
      }
      is(UJ.id.U) {
        imm := (Fill(11, instruction(31)) ## instruction(19, 12) ## instruction(20) ## instruction(30, 21) ## 0.U).asSInt
      }
    }

    imm
  }
}
