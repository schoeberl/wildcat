/*
 * A simple ISA simulator of RISC-V.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * 
 * TODO: should live in the same world as Chisel source,
 * but compile errors make it less convenient.
 */

package wildcat.isasim

class SimRV(code: Array[Int], mem: Array[Int]) {

  // That's the state of the processor
  // That's it, nothing else (except memory ;-)
  var pc = 0
  var reg = new Array[Int](32)
  reg(0) = 0
  // TODO: maybe initialize other registers with random values

  def execute(instr: Int) {
    printf("instr -> %08xd\n", instr)
    // first quick extraction of decoded fields
    val opcode = instr & 0x7f
    val rd = (instr >> 7) & 0x01f
    val rs1 = (instr >> 15) & 0x01f
    val rs2 = (instr >> 20) & 0x01f
    val funct3 = (instr >> 12) & 0x03
    val funct7 = (instr >> 25) & 0x03f
    // immediate is more tricky
    val immi = (instr & 0xfff00000) >> 20 // is this correctly sign extension?
    val imms = ((instr & 0xfe00000) >> (25 - 5)) | ((opcode & 0x0f80) >> 7)
    val immu = (instr & 0xfffff000) >> 12
    // TODO: there are two additional versions of immediate

    // TODO: this shall be switch/pattern matching
    // TODO: have named constants shared with the hardware
    opcode match {
      case 0x13 => { // I instruction
        if (funct3 == 0x0) { // ADDI
          if (rd != 0) {
            reg(rd) = reg(rs1) + immi
          }
        }
      }
      case _ => {
        println(opcode + " not yet imlemented")
      }
    }
  }

  while (pc < code.length) {
    execute(code(pc))
    for (i <- 0 to 3) {
      print(reg(i) + " ")
    }
    println
    pc += 1
  }

  // TODO: maybe functions to encode instructions, such as:
  // addi(rs, imm, rd) ...

}

object SimRV extends App {
  println("Hello RISC-V World")

  val code = Array(
    Helper.genAddi(0, 0x0f, 0),
    Helper.genAddi(0, 111, 1),
    Helper.genAddi(1, 222, 2), 123)

  val mem = new Array[Int](1024)

  new SimRV(code, mem)
}