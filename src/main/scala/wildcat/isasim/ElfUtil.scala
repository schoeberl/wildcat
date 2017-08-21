/*
 * Copyright (c) 2017, DTU
 * Simplified BSD License
 */

/*
 * ELF utility functions for the ISA simulators.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * 
 */

package wildcat.isasim

import java.nio.file.{ Files, Paths }

class ElfUtil(fileName: String) {

  val data = Files.readAllBytes(Paths.get(fileName))

  def isElf = {
    data(0) == 127 && data(1) == 'E' &&
      data(2) == 'L' && data(3) == 'F'
  }

  require(isElf, "Not an ELF file")
  require(data(4) == 1, "Only 32-bit supported")
  val littleEndian = data(5) == 1
  
  def getShort(pos: Int): Short = {
    if (littleEndian) {
      ((data(pos).toShort & 0xff) + ((data(pos+1)<<8))).toShort
    } else {
      ((data(pos+1).toShort & 0xff) + ((data(pos)<<8))).toShort      
    }
  }
  println("Type: "+getShort(0x10))
}

object ElfUtil {

  def main(args: Array[String]): Unit = {

    val elf = new ElfUtil(args(0))
    println(elf.isElf)
  }

}
