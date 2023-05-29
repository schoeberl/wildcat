package wildcat.pipeline

import chisel3._
import chisel3.util._

import wildcat.Opcode._
import wildcat.InstrTypeChisel._

class Decode extends Module {
  val io = IO(new DecodeIO)


  val instr = io.fedec.instr
  val instrReg = RegNext(instr)

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

  // Immediates
  val iimm = Wire(SInt(32.W))
  iimm := instr(31, 20).asSInt

  // Decode
  val opcode = instr(6, 0)
  val func3 = instr(14, 12)
  val func7 = instr(31, 25)

  val instrType = WireDefault(R)
  switch (opcode) {
    is (AluImm.U) { instrType := I }
    is (Alu.U) { instrType := R }
    is (Branch.U) { instrType := SB }
    is (Load.U) { instrType := I }
    is (Store.U) { instrType := S }
    is (Lui.U) { instrType := U }
    is (AuiPc.U) { instrType := U }
    is (Jal.U) { instrType := UJ }
    is (JalR.U) { instrType := I }
    is (ECall.U) { instrType := I }
  }

  // Address calculation for load/store (if 3 or 4 stages pipeline)

  io.decex.pc := RegNext(io.fedec.pc)
  io.decex.instr := RegNext(io.fedec.instr)
  printf("%x instruction: %x %x\n", io.decex.pc, io.decex.instr, iimm)
}
