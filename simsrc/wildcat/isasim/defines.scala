package wildcat.isasim

// TODO: have named constants shared with the hardware

object Opcode {
  val AluImm = 0x13
  val Alu = 0x33
  val Branch = 0x63
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