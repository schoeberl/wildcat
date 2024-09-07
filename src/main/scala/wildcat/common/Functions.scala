package wildcat.common

import chisel3._
import chisel3.util._
import wildcat.InstrType._
import wildcat.Opcode._


object Functions {

  def getInstrType(opcode: UInt) = {
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
    instrType
  }

  def genImm(instruction: UInt, instrType: UInt): SInt = {

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
