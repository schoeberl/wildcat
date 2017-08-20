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
}

object ElfUtil {

  def main(args: Array[String]): Unit = {

    val elf = new ElfUtil(args(0))
    println(elf.isElf)
  }

}
