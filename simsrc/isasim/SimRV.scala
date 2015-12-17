/*
 * A simple ISA simulator of RISC-V.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * 
 * TODO: should live in the same world as Chisel source,
 * but compile errors make it less convenient.
 */

package isasim

class SimRV(code: Array[Int]) {

  // That's the state of the processor
  // That's it, nothing else (except memory ;-)
  var pc = 0
  var reg = new Array[Int](32)
  reg(0) = 0
  // TODO: maybe initialize other regs with random values

  def execute(instr: Int) {
    println(instr)
    printf("instr -> %08xd\n", instr)
    // fist quick extraction of decoded fields
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

    // TODO: this shall be switch/pattern matching
    // TODO: have named constants shared with the hardware
    if (opcode == 0x13) { // I instruction
      if (funct3 == 0x0) { // ADDI
        if (rd != 0) {
          reg(rd) = reg(rs1) + immi
        }
      }
    }
  }

  while (pc < code.length) {
    execute(code(pc))
    for (i <- 0 to 4) {
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

  def genAddi(rs: Int, imm: Int, rd: Int) = {
    // TODO masking? Better do it... Or live with some fun
    val i = (rs << 15) | (imm << 20) | (rd << 7) | 0x13
    printf("gen -> %08xd\n", i)
    i
  }
  val code = Array(
    genAddi(0, 0x0f, 0),
    genAddi(0, 111, 1),
    genAddi(1, 222, 2))
  println(code(0))
  println(code(1))
  new SimRV(code)
}