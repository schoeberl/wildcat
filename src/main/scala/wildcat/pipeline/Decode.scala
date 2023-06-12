package wildcat.pipeline

import chisel3._
import chisel3.util._

import wildcat.Opcode._
import wildcat.InstrType._

class Decode extends Module {
  val io = IO(new DecodeIO)


  val instr = io.fedec.instr
  // pipe registers
  val instrReg = RegNext(instr, 0x00000033.U) // nop on reset
  val pcReg = RegNext(io.fedec.pc, 0.U)

  // Register file
  val regMem = SyncReadMem(32, UInt(32.W), SyncReadMem.WriteFirst)

  val rs1 = instr(19, 15)
  val rs2 = instr(24, 20)
  val rd = instr(11, 7)

  val rs1Val = Mux(rs1 =/= 0.U, regMem.read(rs1), 0.U)
  val rs2Val = Mux(rs2 =/= 0.U, regMem.read(rs2), 0.U)
  when (io.wbdec.valid) {
    regMem.write(io.wbdec.regNr, io.wbdec.data)
  }

  // Decode
  val opcode = instrReg(6, 0)
  val func3 = instrReg(14, 12)
  val func7 = instrReg(31, 25)

  val instrType = WireDefault(R.id.U)
  switch (opcode) {
    is (AluImm.U) { instrType := I.id.U }
    is (Alu.U) { instrType := R.id.U }
    is (Branch.U) { instrType := SB.id.U }
    is (Load.U) { instrType := I.id.U }
    is (Store.U) { instrType := S.id.U }
    is (Lui.U) { instrType := U.id.U }
    is (AuiPc.U) { instrType := U.id.U }
    is (Jal.U) { instrType := UJ.id.U }
    is (JalR.U) { instrType := I.id.U }
    is (ECall.U) { instrType := I.id.U }
  }
  // Immediates
  val imm = Wire(SInt(32.W))
  imm := instrReg(31, 20).asSInt

  // Address calculation for load/store (if 3 or 4 stages pipeline)

  io.decex.pc := pcReg
  io.decex.instr := instrReg
  io.decex.rs1 := RegNext(rs1)
  io.decex.rs2 := RegNext(rs2)
  io.decex.rd := RegNext(rd)
  io.decex.imm := imm
  printf("%x: instruction: %x rs1: %x rs2: %x rd: %x imm: %x\n", io.decex.pc, io.decex.instr, io.decex.rs1,
    io.decex.rs2, io.decex.rd, io.decex.imm)
}
