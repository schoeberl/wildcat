/*
 * Copyright (c) 2016, DTU
 * Simplified BSD License
 */

/*
 * Utility functions for the ISA simulator of RISC-V.
 * 
 * Author: Martin Schoeberl (martin@jopdesign.com)
 * 
 */

package wildcat.isasim

object Util {

  /**
   * Read a binary file into an array vector
   */
  def readBin(fileName: String): Array[Int] = {

    println("Reading " + fileName)
    // maybe find a more elegant way to read a binary file
    val source = scala.io.Source.fromFile(fileName)(scala.io.Codec.ISO8859)
    val byteArray = source.map(_.toByte).toArray
    source.close()

    // use an array to convert input
    val arr = new Array[Int](math.max(1, byteArray.length / 4))

    if (byteArray.length == 0) {
      arr(0) = 0
    }

    // little endian
    for (i <- 0 until byteArray.length / 4) {
      var word = 0
      for (j <- 0 until 4) {
        word >>>= 8
        word += (byteArray(i * 4 + j).toInt & 0xff) << 24
      }
      // printf("%08x\n", word)
      arr(i) = word
    }

    arr
  }
}
