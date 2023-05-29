package wildcat

import chisel3._


object Opcode {
  val AluImm = 0x13
  val Alu = 0x33
  val Branch = 0x63
  val Load = 0x03
  val Store = 0x23
  val Lui = 0x37
  val AuiPc = 0x17
  val Jal = 0x6f
  val JalR = 0x67
  val Fence = 0x0f
  val ECall = 0x73
}

object InstrType extends Enumeration {
  type InstrType = Value
  val R, I, S, SB, U, UJ = Value
}

object InstrTypeChisel {
  val R = 0.U
  val I = 1.U
  val S = 2.U
  val SB = 3.U
  val U = 4.U
  val UJ = 5.U
}

object AluFunct7 {
  val DEFAULT = 0x00
  val SRA_SUB = 0x20
}

object AluFunct {
  val ADD_SUB = 0x00 // no SUB in I-type
  val SLL = 0x01
  val SLT = 0x02
  val SLTU = 0x03
  val XOR = 0x04
  val SRL_SRA = 0x05
  val OR = 0x06
  val AND = 0x07
}

object BranchFunct {
  val BEQ = 0x00
  val BNE = 0x01
  val BLT = 0x04
  val BGE = 0x05
  val BLTU = 0x06
  val BGEU = 0x07
}

object LoadStoreFunct {
  val LSB = 0x00
  val LSH = 0x01
  val LSW = 0x02
  val LBU = 0x04
  val LHU = 0x05
}