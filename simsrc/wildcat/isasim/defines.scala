package wildcat.isasim

// TODO: have named constants shared with the hardware

object Opcode {
  val AluImm = 0x13
  val Alu = 0x33
}

//object AluImmFunct {
//  val ADDI = 0x00
//  val SLLI = 0x01
//  val SLTI = 0x02
//  val SLTIU = 0x03
//  val XORI = 0x04
//  val SRLI_SRLA = 0x05
//  val ORI = 0x06
//  val ANDI = 0x07
//}

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