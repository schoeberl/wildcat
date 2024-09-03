package wildcat

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

object AluType extends Enumeration {
  type AluType = Value
  val ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND = Value
}

object AluFunct7 {
  val DEFAULT = 0x00
  val SRA_SUB = 0x20
}

object AluFunct3 {
  val F3_ADD_SUB = 0x00 // no SUB in I-type
  val F3_SLL = 0x01
  val F3_SLT = 0x02
  val F3_SLTU = 0x03
  val F3_XOR = 0x04
  val F3_SRL_SRA = 0x05
  val F3_OR = 0x06
  val F3_AND = 0x07
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